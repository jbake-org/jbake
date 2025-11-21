package org.jbake.parser

import org.apache.commons.configuration2.Configuration
import org.jbake.app.configuration.JBakeConfiguration
import org.jbake.model.DocumentModel
import java.io.File

interface ParserEngine {
    /**
     * Parse a given file and transform to a model used by [MarkdownEngine] to render the file content.
     *
     * @return A model representation of the given file. NULLABLE. TBD: Have a NullDocumentModel to avoid nulls?
     */
    fun parse(config: JBakeConfiguration, file: File): DocumentModel?

    /**
     * @param config      The project configuration
     * @param fileToParse        The file to be parsed
     * @param contentPath unknown
     * @return A model representation of the given file
     */
    @Deprecated("use {@link #parse(JBakeConfiguration, File)} instead")
    fun parse(config: Configuration, fileToParse: File, contentPath: String): MutableMap<String,  Any>
}
