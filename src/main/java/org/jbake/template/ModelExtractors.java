package org.jbake.template;

import org.jbake.app.ContentStore;
import org.jbake.model.DocumentTypeUtils;
import org.jbake.template.model.PublishedCustomExtractor;
import org.jbake.template.model.TypedDocumentsExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;


/**
 * <p>A singleton class giving access to model extractors. Model extractors are loaded based on classpath. New
 * rendering may be registered either at runtime (not recommanded) or by putting a descriptor file on classpath
 * (recommanded).</p>
 * <p>The descriptor file must be found in <i>META-INF</i> directory and named
 * <i>org.jbake.template.ModelExtractors.properties</i>. The format of the file is easy:</p>
 * <code>org.jbake.template.model.AllPosts=all_posts<br> org.jbake.template.model.AllContent=all_content<br> </code>
 * <p>where the key is the class of the extractor (must implement {@link ModelExtractor}  and the value is the key
 * by which values are to be accessed in model.</p>
 * <p>
 * This class loads the engines only if they are found on classpath. If not, the engine is not registered. This allows
 * JBake to support multiple rendering engines without the explicit need to have them on classpath. This is a better fit
 * for embedding.
 * </p>
 *
 * @author ndx
 * @author Cédric Champeau
 */
public class ModelExtractors {
    private static final String PROPERTIES = "META-INF/org.jbake.template.ModelExtractors.properties";

    private final static Logger LOGGER = LoggerFactory.getLogger(ModelExtractors.class);

    private final Map<String, ModelExtractor> extractors;

    private static class Loader {
        private static final ModelExtractors INSTANCE = new ModelExtractors();
    }

    public static ModelExtractors getInstance() {
        return Loader.INSTANCE;
    }

    private ModelExtractors() {
        extractors = new TreeMap<String, ModelExtractor>();
        loadEngines();
    }

    public void registerEngine(String key, ModelExtractor extractor) {
        ModelExtractor old = extractors.put(key, extractor);
        if (old != null) {
            LOGGER.warn("Registered a model extractor for key [.{}] but another one was already defined: {}", key, old);
        }
    }

    /**
     * This method is used to search for a specific class, telling if loading the engine would succeed. This is
     * typically used to avoid loading optional modules.
     *
     * @param engineClassName engine class, used both as a hint to find it and to create the engine itself.  @return null if the engine is not available, an instance of the engine otherwise
     */
    private static ModelExtractor tryLoadEngine(String engineClassName) {
        try {
            @SuppressWarnings("unchecked")
            Class<? extends ModelExtractor> engineClass = (Class<? extends ModelExtractor>) Class.forName(engineClassName, false, ModelExtractors.class.getClassLoader());
            return engineClass.newInstance();
        } catch (ClassNotFoundException e) {
            return null;
        } catch (InstantiationException e) {
            return null;
        } catch (IllegalAccessException e) {
            return null;
        } catch (NoClassDefFoundError e) {
            // a dependency of the engine may not be found on classpath
            return null;
        }
    }

    /**
     * This method is used internally to load markup engines. Markup engines are found using descriptor files on
     * classpath, so adding an engine is as easy as adding a jar on classpath with the descriptor file included.
     */
    private void loadEngines() {
        try {
            ClassLoader cl = ModelExtractors.class.getClassLoader();
            Enumeration<URL> resources = cl.getResources(PROPERTIES);
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                Properties props = new Properties();
                props.load(url.openStream());
                for (Map.Entry<Object, Object> entry : props.entrySet()) {
                    String className = (String) entry.getKey();
                    String[] extensions = ((String) entry.getValue()).split(",");
                    loadAndRegisterEngine(className, extensions);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadAndRegisterEngine(String className, String... extensions) {
        ModelExtractor engine = tryLoadEngine(className);
        if (engine != null) {
            for (String extension : extensions) {
                registerEngine(extension, engine);
            }
        }
    }

    public <Type> Type extractAndTransform(ContentStore db, String key, Map map, TemplateEngineAdapter<Type> adapter) throws NoModelExtractorException {
        if (extractors.containsKey(key)) {
            Object extractedValue = extractors.get(key).get(db, map, key);
            return adapter.adapt(key, extractedValue);
        } else {
            throw new NoModelExtractorException("no model extractor for key \"" + key + "\"");
        }
    }

    /**
     * @see java.util.Map#containsKey(java.lang.Object)
     * @param key A key a {@link ModelExtractor} is registered with
     * @return true if key is registered
     */
    public boolean containsKey(Object key) {
        return extractors.containsKey(key);
    }

    /**
     * @return  A @{@link Set} of all known keys a @{@link ModelExtractor} is registered with
     * @see java.util.Map#keySet()
     */
    public Set<String> keySet() {
        return extractors.keySet();
    }

    public void registerExtractorsForCustomTypes(String docType) {
        String pluralizedDoctype = DocumentTypeUtils.pluralize(docType);
        if (!containsKey(pluralizedDoctype)) {
            LOGGER.info("register new extractors for document type: " + docType);
            registerEngine(pluralizedDoctype, new TypedDocumentsExtractor());
            registerEngine("published_" + pluralizedDoctype, new PublishedCustomExtractor(docType));
        }
    }

}