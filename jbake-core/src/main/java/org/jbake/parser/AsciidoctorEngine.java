package org.jbake.parser;

import org.apache.commons.configuration.Configuration;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.AttributesBuilder;
import org.asciidoctor.Options;
import org.asciidoctor.ast.DocumentHeader;
import org.jbake.app.ConfigUtil.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static org.apache.commons.lang.BooleanUtils.toBooleanObject;
import static org.apache.commons.lang.math.NumberUtils.isNumber;
import static org.apache.commons.lang.math.NumberUtils.toInt;
import static org.asciidoctor.AttributesBuilder.attributes;
import static org.asciidoctor.OptionsBuilder.options;
import static org.asciidoctor.SafeMode.UNSAFE;

/**
 * Renders documents in the asciidoc format using the Asciidoctor engine.
 *
 * @author Cédric Champeau
 */
public class AsciidoctorEngine extends MarkupEngine {
    private final static Logger LOGGER = LoggerFactory.getLogger(AsciidoctorEngine.class);

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private Asciidoctor engine;
    /* comma separated file paths to additional gems */
    private static final String OPT_GEM_PATH = "gemPath";
    /* comma separated gem names */
    private static final String OPT_REQUIRES = "requires";

    public AsciidoctorEngine() {
        Class engineClass = Asciidoctor.class;
        assert engineClass!=null;
    }

    private Asciidoctor getEngine(Options options) {
        try {
            lock.readLock().lock();
            if (engine==null) {
                lock.readLock().unlock();
                try {
                    lock.writeLock().lock();
                    if (engine==null) {
                        LOGGER.info("Initializing Asciidoctor engine...");
                        if (options.map().containsKey(OPT_GEM_PATH)) {
                            engine = Asciidoctor.Factory.create(String.valueOf(options.map().get(OPT_GEM_PATH)));
                        } else {
                            engine = Asciidoctor.Factory.create();
                        }

                        if (options.map().containsKey(OPT_REQUIRES)) {
                            String[] requires = String.valueOf(options.map().get(OPT_REQUIRES)).split(",");
                            if (requires.length != 0) {
                                for (String require : requires) {
                                    engine.requireLibrary(require);
                                }
                            }
                        }

                        LOGGER.info("Asciidoctor engine initialized.");
                    }
                } finally {
                    lock.readLock().lock();
                    lock.writeLock().unlock();
                }
            }
        } finally {
            lock.readLock().unlock();
        }
        return engine;
    }

    @Override
    public void processHeader(final ParserContext context) {
        Options options = getAsciiDocOptionsAndAttributes(context);
        final Asciidoctor asciidoctor = getEngine(options);
        DocumentHeader header = asciidoctor.readDocumentHeader(context.getFile());
        Map<String, Object> contents = context.getContents();
        if (header.getDocumentTitle() != null) {
        	contents.put("title", header.getDocumentTitle().getCombined());
        }
        Map<String, Object> attributes = header.getAttributes();
        for (String key : attributes.keySet()) {
            if (key.startsWith("jbake-")) {
                Object val = attributes.get(key);
                if (val!=null) {
                    String pKey = key.substring(6);
                    contents.put(pKey, val);
                }
            }
            if (key.equals("revdate")) {
                if (attributes.get(key) != null && attributes.get(key) instanceof String) {

                    DateFormat df = new SimpleDateFormat(context.getConfig().getString(Keys.DATE_FORMAT));
                    Date date = null;
                    try {
                        date = df.parse((String)attributes.get(key));
                        contents.put("date", date);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (key.equals("jbake-tags")) {
                if (attributes.get(key) != null && attributes.get(key) instanceof String) {
                    contents.put("tags", ((String) attributes.get(key)).split(","));
                }
            } else {
                contents.put(key, attributes.get(key));
            }
        }
    }

    @Override
    public void processBody(ParserContext context) {
        StringBuilder body = new StringBuilder(context.getBody().length());
        if (!context.hasHeader()) {
            for (String line : context.getFileLines()) {
                body.append(line).append("\n");
            }
            context.setBody(body.toString());
        }
        processAsciiDoc(context);
    }

    private void processAsciiDoc(ParserContext context) {
        Options options = getAsciiDocOptionsAndAttributes(context);
        final Asciidoctor asciidoctor = getEngine(options);
        context.setBody(asciidoctor.render(context.getBody(), options));
    }

    private Options getAsciiDocOptionsAndAttributes(ParserContext context) {
        Configuration config = context.getConfig();
        final AttributesBuilder attributes = attributes(config.getStringArray(Keys.ASCIIDOCTOR_ATTRIBUTES));
        if (config.getBoolean(Keys.ASCIIDOCTOR_ATTRIBUTES_EXPORT, false)) {
            final String prefix = config.getString(  Keys.ASCIIDOCTOR_ATTRIBUTES_EXPORT_PREFIX, "");
            for (final Iterator<String> it = config.getKeys(); it.hasNext();) {
                final String key = it.next();
                if (!key.startsWith("asciidoctor")) {
                    attributes.attribute(prefix + key.replace(".", "_"), config.getProperty(key));
                }
            }
        }
        final Configuration optionsSubset = config.subset(Keys.ASCIIDOCTOR_OPTION);
        final Options options = options().attributes(attributes.get()).get();
        for (final Iterator<String> iterator = optionsSubset.getKeys(); iterator.hasNext();) {
            final String name = iterator.next();
            if (name.equals(Options.TEMPLATE_DIRS)) {
            	options.setTemplateDirs(optionsSubset.getString(name));
            } else {
            	options.setOption(name,  guessTypeByContent(optionsSubset.getString(name)));
            }
        }
        options.setBaseDir(context.getFile().getParentFile().getAbsolutePath());
        options.setSafe(UNSAFE);
        return options;
    }

    /**
     * Guess the type by content it has.
     * @param value
     * @return boolean,integer of string as fallback
     */
    private static Object guessTypeByContent(String value){
        if (toBooleanObject(value)!=null)
            return toBooleanObject(value);
        if(isNumber(value))
            return toInt(value);
        return value;
    }
}
