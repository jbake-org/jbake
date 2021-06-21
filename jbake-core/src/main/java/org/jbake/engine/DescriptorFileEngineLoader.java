package org.jbake.engine;

import org.jbake.app.ContentStore;
import org.jbake.app.configuration.JBakeConfiguration;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

public abstract class DescriptorFileEngineLoader<T> {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass().getCanonicalName());
    private final Map<String, T> classes;
    protected final Context context;

    protected DescriptorFileEngineLoader() {
        classes = new TreeMap<>();
        context = new Context();
    }

    /**
     * This method is used to search for a specific class, telling if loading the engine would succeed. This is
     * typically used to avoid loading optional modules.
     *
     * @param context The context to construct an engine class, used both as a hint to find it and to create the engine itself.
     */
    protected T tryLoadEngine(Context context) {
        try {
            Class<?> engineClass = Class.forName(context.className(), false, DescriptorFileEngineLoader.class.getClassLoader());
            context.addEngineClass(engineClass);
            return createInstance(context);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoClassDefFoundError | NoSuchMethodException | InvocationTargetException e) {
            return getErrorEngine(context.className());
        } // a dependency of the engine may not be found on classpath

    }

    @NotNull
    protected abstract T createInstance(Context context) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException;

    protected abstract T getErrorEngine(String engineClassName);

    protected void registerEngine(String key, T engine) {
        T old = classes.put(key, engine);
        if (old != null) {
            logger.warn("Override detected. Registered an engine [{}] for key [{}] but another one was already defined: [{}]", engine.getClass().getName(), key, old);
        }
    }

    /**
     * This method is used internally to load engines. Engines are found using descriptor files on
     * classpath, so adding an engine is as easy as adding a jar on classpath with the descriptor file included.
     */
    protected void loadEngines() {
        try {
            ClassLoader cl = DescriptorFileEngineLoader.class.getClassLoader();
            Enumeration<URL> resources = cl.getResources(descriptorFile());
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                try (InputStream is = url.openStream()) {
                    Properties props = new Properties();
                    props.load(is);
                    for (Map.Entry<Object, Object> entry : props.entrySet()) {
                        String className = (String) entry.getKey();
                        String[] extensions = ((String) entry.getValue()).split(",");
                        context.addClassName(className);
                        context.addExtensions(extensions);
                        loadAndRegisterEngine(context);
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Error loading engines: {}", e.getMessage(), e);
        }
    }

    protected void loadAndRegisterEngine(Context context) {
        T engine = tryLoadEngine(context);
        if (engine != null) {
            for (String extension : context.extensions()) {
                registerEngine(extension, engine);
            }
        }
    }

    protected abstract String descriptorFile();

    /**
     * @param key A key an Engine is registered with
     * @return true if key is registered
     * @see Map#containsKey(Object)
     */
    public boolean supportsExtension(String key) {
        return classes.containsKey(key);
    }

    /**
     * @return A @{@link Set} of all known keys an Engine is registered with
     * @see Map#keySet()
     */
    public Set<String> keySet() {
        return classes.keySet();
    }

    protected T get(String extension) {
        return classes.get(extension);
    }

    public void reset() {
        classes.clear();
        loadEngines();
    }

    protected class Context {
        private String className;
        private String[] extensions;
        private Class<?> engingeClass;
        private JBakeConfiguration configuration;
        private ContentStore db;

        public void addClassName(String className) {
            this.className = className;
        }

        public String className() {
            return className;
        }

        public void addExtensions(String[] extensions) {
            this.extensions = extensions;
        }

        public String[] extensions() {
            return extensions;
        }

        public Class<?> engineClass() {
            return engingeClass;
        }

        public void addEngineClass(Class<?> engineClass) {
            this.engingeClass = engineClass;
        }

        public ContentStore db() {
            return db;
        }

        public JBakeConfiguration config() {
            return configuration;
        }

        public void addConfig(JBakeConfiguration config) {
            this.configuration = config;
        }

        public void addDb(ContentStore db) {
            this.db = db;
        }
    }
}
