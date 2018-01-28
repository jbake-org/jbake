package org.jbake.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * <p>A singleton class giving access to markup engines. Markup engines are loaded based on classpath.
 * New engines may be registered either at runtime (not recommanded) or by putting a descriptor file
 * on classpath (recommanded).</p>
 *
 * <p>The descriptor file must be found in <i>META-INF</i> directory and named
 * <i>org.jbake.parser.MarkupEngines.properties</i>. The format of the file is easy:</p>
 * <code>
 * org.jbake.parser.RawMarkupEngine=html<br>
 * org.jbake.parser.AsciidoctorEngine=ad,adoc,asciidoc<br>
 * org.jbake.parser.MarkdownEngine=md<br>
 * </code>
 * <p>where the key is the class of the engine (must extend {@link org.jbake.parser.MarkupEngine} and have a no-arg
 * constructor and the value is a comma-separated list of file extensions that this engine is capable of proceeding.</p>
 *
 * <p>Markup engines are singletons, so are typically used to initialize the underlying renderning engines. They
 * <b>must not</b> store specific information of a currently processed file (use {@link ParserContext the parser context}
 * for that).</p>
 *
 * This class loads the engines only if they are found on classpath. If not, the engine is not registered. This allows
 * JBake to support multiple rendering engines without the explicit need to have them on classpath. This is a better
 * fit for embedding.
 *
 * @author Cédric Champeau
 *
 */
public class Engines {
    private final static Logger LOGGER = LoggerFactory.getLogger(Engines.class);

    private final static Engines INSTANCE;

    static {
        INSTANCE = new Engines();
        loadEngines();
    }

    private final Map<String, ParserEngine> parsers;

    public static ParserEngine get(String fileExtension) {
        return INSTANCE.getEngine(fileExtension);
    }

    public static void register(String fileExtension, ParserEngine engine) {
        INSTANCE.registerEngine(fileExtension, engine);
    }

    public static Set<String> getRecognizedExtensions() {
        return Collections.unmodifiableSet(INSTANCE.parsers.keySet());
    }

    private Engines() {
        parsers = new HashMap<String, ParserEngine>();
    }

    private void registerEngine(String fileExtension, ParserEngine markupEngine) {
    	ParserEngine old = parsers.put(fileExtension, markupEngine);
        if (old != null) {
            LOGGER.warn("Registered a markup engine for extension [.{}] but another one was already defined: {}", fileExtension, old);
        }
    }

    private ParserEngine getEngine(String fileExtension) {
        return parsers.get(fileExtension);
    }

    /**
     * This method is used to search for a specific class, telling if loading the engine would succeed. This is
     * typically used to avoid loading optional modules.
     *
     * @param engineClassName engine class, used both as a hint to find it and to create the engine itself.
     * @return null if the engine is not available, an instance of the engine otherwise
     */
    private static ParserEngine tryLoadEngine(String engineClassName) {
        try {
            @SuppressWarnings("unchecked")
            Class<? extends ParserEngine> engineClass = (Class<? extends ParserEngine>) Class.forName(engineClassName, false, Engines.class.getClassLoader());
            return engineClass.newInstance();
        } catch (ClassNotFoundException e) {
            return new ErrorEngine(engineClassName);
        } catch (InstantiationException e) {
            return new ErrorEngine(engineClassName);
        } catch (IllegalAccessException e) {
            return new ErrorEngine(engineClassName);
        } catch (NoClassDefFoundError e) {
            // a dependency of the engine may not be found on classpath
            return new ErrorEngine(engineClassName);
        }
    }

    /**
     * This method is used internally to load markup engines. Markup engines are found using descriptor files on classpath, so
     * adding an engine is as easy as adding a jar on classpath with the descriptor file included.
     */
    private static void loadEngines() {
        try {
            ClassLoader cl = Engines.class.getClassLoader();
            Enumeration<URL> resources = cl.getResources("META-INF/org.jbake.parser.MarkupEngines.properties");
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                Properties props = new Properties();
                props.load(url.openStream());
                for (Map.Entry<Object, Object> entry : props.entrySet()) {
                    String className = (String) entry.getKey();
                    String[] extensions = ((String)entry.getValue()).split(",");
                    registerEngine(className, extensions);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void registerEngine(String className, String... extensions) {
    	ParserEngine engine = tryLoadEngine(className);
        if (engine != null) {
            for (String extension : extensions) {
                register(extension, engine);
            }
            if (engine instanceof ErrorEngine) {
                LOGGER.warn("Unable to load a suitable rendering engine for extensions {}", Arrays.toString(extensions));
            }
        }
    }
}
