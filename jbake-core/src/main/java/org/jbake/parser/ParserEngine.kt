package org.jbake.parser

import org.apache.commons.configuration2.Configuration
import org.jbake.app.configuration.JBakeConfiguration
import org.jbake.model.DocumentModel
import java.io.File

interface ParserEngine {
    /**
     * Parse a given file and transform to a model representation used by [MarkdownEngine] implementations
     * to render the file content.
     *
     * @param config The project configuration
     * @param file   The file to be parsed
     * @return A model representation of the given file
     */
    fun parse(config: JBakeConfiguration, file: File): DocumentModel?

    /**
     * @param config      The project configuration
     * @param file        The file to be parsed
     * @param contentPath unknown
     * @return A model representation of the given file
     */
    @Deprecated("use {@link #parse(JBakeConfiguration, File)} instead")
    fun parse(config: Configuration?, file: File?, contentPath: String?): MutableMap<String?, Any?>?
}
