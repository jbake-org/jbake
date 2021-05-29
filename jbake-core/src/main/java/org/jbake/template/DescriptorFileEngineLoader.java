package org.jbake.template;

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
    protected static final Logger LOGGER = LoggerFactory.getLogger(DescriptorFileEngineLoader.class);
    private final Map<String, T> classes;

    protected DescriptorFileEngineLoader() {
        classes = new TreeMap<>();
    }

    /**
     * This method is used to search for a specific class, telling if loading the engine would succeed. This is
     * typically used to avoid loading optional modules.
     *
     * @param engineClassName engine class, used both as a hint to find it and to create the engine itself.  @return null if the engine is not available, an instance of the engine otherwise
     */
    @SuppressWarnings("unchecked")
    protected T tryLoadEngine(String engineClassName) {
        try {
            Class<?> engineClass = Class.forName(engineClassName, false, DescriptorFileEngineLoader.class.getClassLoader());
            return (T) engineClass.getDeclaredConstructor().newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoClassDefFoundError | NoSuchMethodException | InvocationTargetException e) {
            return null;
        } // a dependency of the engine may not be found on classpath

    }

    protected void registerEngine(String key, T extractor) {
        T old = classes.put(key, extractor);
        if (old != null) {
            LOGGER.warn("Override detected. Registered a model extractor for key [.{}] but another one was already defined: {}", key, old);
        }
    }

    /**
     * This method is used internally to load markup engines. Markup engines are found using descriptor files on
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
                        loadAndRegisterEngine(className, extensions);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void loadAndRegisterEngine(String className, String... extensions) {
        T engine = tryLoadEngine(className);
        if (engine != null) {
            for (String extension : extensions) {
                registerEngine(extension, engine);
            }
        }
    }

    protected abstract String descriptorFile();

    /**
     * @param key A key a {@link ModelExtractor} is registered with
     * @return true if key is registered
     * @see Map#containsKey(Object)
     */
    public boolean supportsExtension(String key) {
        return classes.containsKey(key);
    }

    /**
     * @return A @{@link Set} of all known keys a @{@link ModelExtractor} is registered with
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
}
