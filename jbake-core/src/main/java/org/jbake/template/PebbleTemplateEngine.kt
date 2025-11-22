package org.jbake.template

import io.pebbletemplates.pebble.PebbleEngine
import io.pebbletemplates.pebble.error.PebbleException
import io.pebbletemplates.pebble.extension.escaper.EscaperExtension
import io.pebbletemplates.pebble.loader.FileLoader
import io.pebbletemplates.pebble.loader.Loader
import io.pebbletemplates.pebble.template.PebbleTemplate
import org.jbake.app.ContentStore
import org.jbake.app.configuration.JBakeConfiguration
import org.jbake.template.TemplateEngineAdapter.NoopAdapter
import org.jbake.template.model.TemplateModel
import java.io.IOException
import java.io.Writer

/**
 * Renders pages using the [Pebble](https://pebbletemplates.io/) template engine.
 *
 * @author Mitchell Bosecke
 */
class PebbleTemplateEngine(config: JBakeConfiguration, db: ContentStore) : AbstractTemplateEngine(config, db) {
    private var engine: PebbleEngine? = null

    init {
        initializeTemplateEngine()
    }

    private fun initializeTemplateEngine() {
        val loader: Loader<*> = FileLoader()
        loader.setPrefix(config.templateFolder.absolutePath)

        /*
         * Turn off the autoescaper because I believe that we can assume all
         * data is safe considering it is all statically generated.
         */
        val escaper = EscaperExtension()
        escaper.setAutoEscaping(false)

        engine = PebbleEngine.Builder().loader(loader).extension(escaper).build()
    }

    @Throws(RenderingException::class)
    override fun renderDocument(model: TemplateModel, templateName: String, writer: Writer) {
        val template: PebbleTemplate
        try {
            template = engine!!.getTemplate(templateName)
            template.evaluate(writer, wrap(model))
        } catch (e: PebbleException) {
            throw RenderingException(e)
        } catch (e: IOException) {
            throw RenderingException(e)
        }
    }

    private fun wrap(model: TemplateModel) = object : TemplateModel(model) {
        override fun get(property: String): Any? {
            try {
                return extractors.extractAndTransform(db, property, this, NoopAdapter())
            } catch (e: NoModelExtractorException) {
                return super.get(property)
            }
        }
    }
}
