package org.jbake.parser;

import org.apache.commons.configuration2.Configuration;
import org.jbake.app.configuration.JBakeConfiguration;
import org.jbake.model.DocumentModel;

import java.io.File;
import java.util.Map;

public interface ParserEngine {

    /**
     * Parse a given file and transform to a model representation used by {@link MarkdownEngine} implementations
     * to render the file content.
     *
     * @param config The project configuration
     * @param file   The file to be parsed
     * @return A model representation of the given file
     */
    DocumentModel parse(JBakeConfiguration config, File file);

    /**
     * @param config      The project configuration
     * @param file        The file to be parsed
     * @param contentPath unknown
     * @return A model representation of the given file
     * @deprecated use {@link #parse(JBakeConfiguration, File)} instead
     */
    @Deprecated
    Map<String, Object> parse(Configuration config, File file, String contentPath);

    /**
     * Build the engine specific URI for the given file
     * @param config The project configuration
     * @param file The file the URI should be build for
     * @return The engine specific URI
     */
    String buildURI(JBakeConfiguration config, File file);
}
