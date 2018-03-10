package org.jbake.template;

import org.jbake.app.ContentStore;
import org.jbake.app.configuration.JBakeConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;


/**
 * <p>
 * A singleton class giving access to rendering engines. Rendering engines are loaded based on classpath. New
 * rendering may be registered either at runtime (not recommanded) or by putting a descriptor file on classpath
 * (recommanded).</p>
 * <p>The descriptor file must be found in <i>META-INF</i> directory and named
 * <i>org.jbake.parser.TemplateEngines.properties</i>. The format of the file is easy:</p>
 * <code>org.jbake.parser.FreeMarkerRenderer=ftl<br> org.jbake.parser.GroovyRenderer=groovy,gsp<br> </code>
 * <p>where the key is the class of the engine (must extend {@link AbstractTemplateEngine} and have
 * a 4-arg constructor and the value is a comma-separated list of file extensions that this engine is capable
 * of proceeding.</p>
 * <p>Rendering engines are singletons, so are typically used to initialize the underlying template engines.
 * <p>
 * This class loads the engines only if they are found on classpath. If not, the engine is not registered. This allows
 * JBake to support multiple rendering engines without the explicit need to have them on classpath. This is a better fit
 * for embedding.
 * </p>
 *
 * @author Cédric Champeau
 */
public class TemplateEngines {

    private static final Logger LOGGER = LoggerFactory.getLogger(TemplateEngines.class);

    private final Map<String, AbstractTemplateEngine> engines;

    public Set<String> getRecognizedExtensions() {
        return Collections.unmodifiableSet(engines.keySet());
    }

    public TemplateEngines(final JBakeConfiguration config, final ContentStore db) {
        engines = new HashMap<>();
        loadEngines(config, db);
    }

    private void registerEngine(String fileExtension, AbstractTemplateEngine templateEngine) {
        AbstractTemplateEngine old = engines.put(fileExtension, templateEngine);
        if (old != null) {
            LOGGER.warn("Registered a template engine for extension [.{}] but another one was already defined: {}", fileExtension, old);
        }
    }

    public AbstractTemplateEngine getEngine(String fileExtension) {
        return engines.get(fileExtension);
    }

    /**
     * This method is used to search for a specific class, telling if loading the engine would succeed. This is
     * typically used to avoid loading optional modules.
     *
     *
     * @param config the configuration
     * @param db database instance
     * @param engineClassName engine class, used both as a hint to find it and to create the engine itself.  @return null if the engine is not available, an instance of the engine otherwise
     */
    private static AbstractTemplateEngine tryLoadEngine(final JBakeConfiguration config, final ContentStore db, String engineClassName) {
        try {
            @SuppressWarnings("unchecked")
            Class<? extends AbstractTemplateEngine> engineClass = (Class<? extends AbstractTemplateEngine>) Class.forName(engineClassName, false, TemplateEngines.class.getClassLoader());
            Constructor<? extends AbstractTemplateEngine> ctor = engineClass.getConstructor(JBakeConfiguration.class, ContentStore.class);
            return ctor.newInstance(config, db);
        } catch (Exception e) {
            LOGGER.error("unable to load engine", e);
            return null;
        }
    }

    /**
     * This method is used internally to load markup engines. Markup engines are found using descriptor files on
     * classpath, so adding an engine is as easy as adding a jar on classpath with the descriptor file included.
     */
    private void loadEngines(final JBakeConfiguration config, final ContentStore db) {
        try {
            ClassLoader cl = TemplateEngines.class.getClassLoader();
            Enumeration<URL> resources = cl.getResources("META-INF/org.jbake.parser.TemplateEngines.properties");
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                Properties props = new Properties();
                props.load(url.openStream());
                for (Map.Entry<Object, Object> entry : props.entrySet()) {
                    String className = (String) entry.getKey();
                    String[] extensions = ((String) entry.getValue()).split(",");
                    registerEngine(config, db, className, extensions);
                }
            }
        } catch (IOException e) {
            LOGGER.error("Error loading engines", e);
        }
    }

    private void registerEngine(final JBakeConfiguration config, final ContentStore db, String className, String... extensions) {
        AbstractTemplateEngine engine = tryLoadEngine(config, db, className);
        if (engine != null) {
            for (String extension : extensions) {
                registerEngine(extension, engine);
            }
        }
    }
}
