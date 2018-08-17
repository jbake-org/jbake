package org.jbake.parser;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.AttributesBuilder;
import org.asciidoctor.Options;
import org.asciidoctor.ast.DocumentHeader;
import org.jbake.app.configuration.JBakeConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static org.asciidoctor.AttributesBuilder.attributes;
import static org.asciidoctor.OptionsBuilder.options;
import static org.asciidoctor.SafeMode.UNSAFE;

/**
 * Renders documents in the asciidoc format using the Asciidoctor engine.
 *
 * @author Cédric Champeau
 */
public class AsciidoctorEngine extends MarkupEngine {
    private static final Logger LOGGER = LoggerFactory.getLogger(AsciidoctorEngine.class);
    public static final String JBAKE_PREFIX = "jbake-";
    public static final String REVDATE_KEY = "revdate";

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private Asciidoctor engine;
    /* comma separated file paths to additional gems */
    private static final String OPT_GEM_PATH = "gemPath";
    /* comma separated gem names */
    private static final String OPT_REQUIRES = "requires";

    public AsciidoctorEngine() {
        Class engineClass = Asciidoctor.class;
        assert engineClass != null;
    }

    private Asciidoctor getEngine(Options options) {
        try {
            lock.readLock().lock();
            if (engine == null) {
                lock.readLock().unlock();
                try {
                    lock.writeLock().lock();
                    if (engine == null) {
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
        Map<String, Object> documentModel = context.getDocumentModel();
        if (header.getDocumentTitle() != null) {
            documentModel.put("title", header.getDocumentTitle().getCombined());
        }
        Map<String, Object> attributes = header.getAttributes();
        for (Map.Entry<String, Object> attribute : attributes.entrySet()) {
            String key = attribute.getKey();
            Object value = attribute.getValue();

            if (hasJBakePrefix(key)) {
                String pKey = key.substring(6);
                documentModel.put(pKey, value);
            }
            if (hasRevdate(key) && canCastToString(value)) {

                String dateFormat = context.getConfig().getDateFormat();
                DateFormat df = new SimpleDateFormat(dateFormat);
                try {
                    Date date = df.parse((String) value);
                    context.setDate(date);
                } catch (ParseException e) {
                    LOGGER.error("Unable to parse revdate. Expected {}", dateFormat, e);
                }
            }
            if (key.equals("jbake-tags")) {
                if (canCastToString(value)) {
                    context.setTags(((String) value).split(","));
                } else {
                    LOGGER.error("Wrong value of 'jbake-tags'. Expected a String got '{}'", getValueClassName(value));
                }
            } else {
                documentModel.put(key, attributes.get(key));
            }
        }
    }

    private boolean canCastToString(Object value) {
        return value != null && value instanceof String;
    }

    private String getValueClassName(Object value) {
        return (value == null) ? "null" : value.getClass().getCanonicalName();
    }

    private boolean hasRevdate(String key) {
        return key.equals(REVDATE_KEY);
    }

    private boolean hasJBakePrefix(String key) {
        return key.startsWith(JBAKE_PREFIX);
    }

    // TODO: write tests with options and attributes
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
        JBakeConfiguration config = context.getConfig();
        List<String> asciidoctorAttributes = config.getAsciidoctorAttributes();
        final AttributesBuilder attributes = attributes(asciidoctorAttributes.toArray(new String[asciidoctorAttributes.size()]));
        if (config.getExportAsciidoctorAttributes()) {
            final String prefix = config.getAttributesExportPrefixForAsciidoctor();

            for (final Iterator<String> it = config.getKeys(); it.hasNext(); ) {
                final String key = it.next();
                if (!key.startsWith("asciidoctor")) {
                    attributes.attribute(prefix + key.replace(".", "_"), config.get(key));
                }
            }
        }

        final List<String> optionsSubset = config.getAsciidoctorOptionKeys();
        final Options options = options().attributes(attributes.get()).get();
        for (final String optionKey : optionsSubset) {

            Object optionValue = config.getAsciidoctorOption(optionKey);
            if (optionKey.equals(Options.TEMPLATE_DIRS)) {
                List<String> dirs = getAsList(optionValue);
                if (!dirs.isEmpty()) {
                    options.setTemplateDirs(String.valueOf(dirs));
                }
            } else {
                options.setOption(optionKey, optionValue);
            }

        }
        options.setBaseDir(context.getFile().getParentFile().getAbsolutePath());
        options.setSafe(UNSAFE);
        return options;
    }

    private List<String> getAsList(Object asciidoctorOption) {
        List<String> values = new ArrayList<>();

        if (asciidoctorOption instanceof List) {
            values.addAll((List<String>) asciidoctorOption);
        } else if (asciidoctorOption instanceof String) {
            values.add(String.valueOf(asciidoctorOption));
        }
        return values;
    }

}
