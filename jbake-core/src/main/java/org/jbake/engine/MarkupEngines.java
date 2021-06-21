package org.jbake.engine;

import org.jbake.parser.ErrorEngine;
import org.jbake.parser.ParserContext;
import org.jbake.parser.ParserEngine;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;

/**
 * <p>A singleton class giving access to markup engines. Markup engines are loaded based on classpath.
 * New engines may be registered either at runtime (not recommanded) or by putting a descriptor file
 * on classpath (recommanded).</p>
 *
 * <p>The descriptor file must be found in <i>META-INF</i> directory and named
 * <i>org.jbake.engine.MarkupEngines.properties</i>. The format of the file is easy:</p>
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
 * <p>
 * This class loads the engines only if they are found on classpath. If not, the engine is not registered. This allows
 * JBake to support multiple rendering engines without the explicit need to have them on classpath. This is a better
 * fit for embedding.
 *
 * @author Cédric Champeau
 */
public class MarkupEngines extends DescriptorFileEngineLoader<ParserEngine> {
    public static final String PROPERTIES = "META-INF/org.jbake.engine.MarkupEngines.properties";

    private static class Loader {

        private static final MarkupEngines INSTANCE = new MarkupEngines();
    }
    public static MarkupEngines getInstance() {
        return MarkupEngines.Loader.INSTANCE;
    }

    private MarkupEngines() {
        super();
        loadEngines();
    }

    @Override
    protected @NotNull ParserEngine createInstance(Context context) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        return (ParserEngine) context.engineClass().getDeclaredConstructor().newInstance();
    }

    @Override
    protected ParserEngine getErrorEngine(String engineClassName) {
        return new ErrorEngine(engineClassName);
    }

    public static ParserEngine fromExtension(String fileExt) {
        return getInstance().get(fileExt);
    }

    @Override
    protected String descriptorFile() {
        return PROPERTIES;
    }

}
