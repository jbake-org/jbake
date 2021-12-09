package org.jbake.engine;

import org.jbake.app.ContentStore;
import org.jbake.app.configuration.JBakeConfiguration;
import org.jbake.template.AbstractTemplateEngine;
import org.jbake.template.TemplateEngine;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;


/**
 * <p>
 * A singleton class giving access to rendering engines. Rendering engines are loaded based on classpath. New
 * rendering may be registered either at runtime (not recommanded) or by putting a descriptor file on classpath
 * (recommanded).</p>
 * <p>The descriptor file must be found in <i>META-INF</i> directory and named
 * <i>org.jbake.engine.TemplateEngines.properties</i>. The format of the file is easy:</p>
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
public class TemplateEngines extends DescriptorFileEngineLoader<TemplateEngine> {

    public static final String PROPERTIES = "META-INF/org.jbake.engine.TemplateEngines.properties";

    public TemplateEngines(final JBakeConfiguration config, final ContentStore db) {
        context.addConfig(config);
        context.addDb(db);
        loadEngines();
    }

    public Set<String> getRecognizedExtensions() {
        return keySet();
    }

    public TemplateEngine getEngine(String fileExtension) {
        return get(fileExtension);
    }

    @Override
    protected @NotNull TemplateEngine createInstance(Context context) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Constructor<?> ctor = context.engineClass().getConstructor(JBakeConfiguration.class, ContentStore.class);
        return (TemplateEngine) ctor.newInstance(context.config(), context.db());
    }

    @Override
    protected TemplateEngine getErrorEngine(String engineClassName) {
        return null;
    }

    @Override
    protected String descriptorFile() {
        return PROPERTIES;
    }
}
