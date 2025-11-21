package org.jbake.parser

import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.parser.PegdownExtensions
import com.vladsch.flexmark.profile.pegdown.PegdownOptionsAdapter
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Renders documents in the Markdown format.
 *
 * @author Cédric Champeau
 */
class MarkdownEngine : MarkupEngine() {
    override fun processBody(parserContext: ParserContext) {
        val mdExts: MutableList<String> = parserContext.config.markdownExtensions

        var extensions = PegdownExtensions.NONE

        for (ext in mdExts) {
            var ext = ext
            if (ext.startsWith("-")) {
                ext = ext.substring(1)
                extensions = removeExtension(extensions, extensionFor(ext))
            } else {
                if (ext.startsWith("+")) {
                    ext = ext.substring(1)
                }
                extensions = addExtension(extensions, extensionFor(ext))
            }
        }

        val options = PegdownOptionsAdapter.flexmarkOptions(extensions)

        val parser = Parser.builder(options).build()
        val renderer = HtmlRenderer.builder(options).build()

        val document = parser.parse(parserContext.body)
        parserContext.body = (renderer.render(document))
    }

    private fun extensionFor(name: String): Int {
        var extension = PegdownExtensions.NONE

        try {
            val extField = PegdownExtensions::class.java.getDeclaredField(name)
            extension = extField.getInt(null)
        } catch (e: NoSuchFieldException) {
            logger.debug("Undeclared extension field '{}', fallback to NONE", name)
        } catch (e: IllegalAccessException) {
            logger.debug("Undeclared extension field '{}', fallback to NONE", name)
        }
        return extension
    }

    private fun addExtension(previousExtensions: Int, additionalExtension: Int): Int {
        return previousExtensions or additionalExtension
    }

    private fun removeExtension(previousExtensions: Int, unwantedExtension: Int): Int {
        return previousExtensions and (unwantedExtension.inv())
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(MarkdownEngine::class.java)
    }
}
