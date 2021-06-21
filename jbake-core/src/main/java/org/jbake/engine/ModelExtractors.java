package org.jbake.engine;

import org.jbake.app.ContentStore;
import org.jbake.exception.NoModelExtractorException;
import org.jbake.model.DocumentTypeUtils;
import org.jbake.model.TemplateModel;
import org.jbake.template.TemplateEngineAdapter;
import org.jbake.template.model.ModelExtractor;
import org.jbake.template.model.PublishedCustomExtractor;
import org.jbake.template.model.TypedDocumentsExtractor;
import org.jbake.template.model.UnknownModelExtractor;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;


/**
 * <p>A singleton class giving access to model extractors. Model extractors are loaded based on classpath. New
 * rendering may be registered either at runtime (not recommanded) or by putting a descriptor file on classpath
 * (recommanded).</p>
 * <p>The descriptor file must be found in <i>META-INF</i> directory and named
 * <i>org.jbake.engine.ModelExtractors.properties</i>. The format of the file is easy:</p>
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
public class ModelExtractors extends DescriptorFileEngineLoader<ModelExtractor<?>> {

    private static final String PROPERTIES = "META-INF/org.jbake.engine.ModelExtractors.properties";

    private static class Loader {
        private static final ModelExtractors INSTANCE = new ModelExtractors();
    }

    public static ModelExtractors getInstance() {
        return Loader.INSTANCE;
    }

    private ModelExtractors() {
        super();
        loadEngines();
    }

    @Override
    protected ModelExtractor<?> getErrorEngine(String engineClassName) {
        return new UnknownModelExtractor(engineClassName);
    }

    @Override
    protected String descriptorFile() {
        return PROPERTIES;
    }

    @Override
    protected @NotNull ModelExtractor<?> createInstance(Context context) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        return (ModelExtractor<?>) context.engineClass().getDeclaredConstructor().newInstance();
    }

    public <T> T extractAndTransform(ContentStore db, String key, TemplateModel map, TemplateEngineAdapter<T> adapter) throws NoModelExtractorException {
        if (supportsExtension(key)) {
            Object extractedValue = get(key).get(db, map, key);
            return adapter.adapt(key, extractedValue);
        } else {
            throw new NoModelExtractorException("no model extractor for key \"" + key + "\"");
        }
    }

    public void registerExtractorsForCustomTypes(String docType) {
        String pluralizedDoctype = DocumentTypeUtils.pluralize(docType);
        if (!supportsExtension(pluralizedDoctype)) {
            logger.info("register new extractors for document type: {}", docType);
            registerEngine(pluralizedDoctype, new TypedDocumentsExtractor());
            registerEngine("published_" + pluralizedDoctype, new PublishedCustomExtractor(docType));
        }
    }

}
