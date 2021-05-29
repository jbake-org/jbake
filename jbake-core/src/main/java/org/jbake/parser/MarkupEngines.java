package org.jbake.parser;

import org.jbake.template.DescriptorFileEngineLoader;

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
 * <p>
 * This class loads the engines only if they are found on classpath. If not, the engine is not registered. This allows
 * JBake to support multiple rendering engines without the explicit need to have them on classpath. This is a better
 * fit for embedding.
 *
 * @author Cédric Champeau
 */
public class MarkupEngines extends DescriptorFileEngineLoader<ParserEngine> {
    public static final String PROPERTIES = "META-INF/org.jbake.parser.MarkupEngines.properties";

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

    public static Set<String> recognizedExtensions() {
        return getInstance().keySet();
    }

    public static ParserEngine fromExtension(String fileExt) {
        return getInstance().get(fileExt);
    }

    @Override
    protected String descriptorFile() {
        return PROPERTIES;
    }

    @Override
    protected void loadAndRegisterEngine(String className, String... extensions) {
        ParserEngine engine = tryLoadEngine(className);
        if (engine != null) {
            for (String extension : extensions) {
                registerEngine(extension, engine);
            }
            if (engine instanceof ErrorEngine) {
                LOGGER.warn("Unable to load a suitable rendering engine for extensions {}", (Object) extensions);
            }
        }
    }
}
