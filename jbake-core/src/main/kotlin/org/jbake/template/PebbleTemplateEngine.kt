package org.jbake.template

import io.pebbletemplates.pebble.PebbleEngine
import io.pebbletemplates.pebble.error.PebbleException
import io.pebbletemplates.pebble.extension.escaper.EscaperExtension
import io.pebbletemplates.pebble.loader.FileLoader
import io.pebbletemplates.pebble.loader.Loader
import io.pebbletemplates.pebble.template.PebbleTemplate
import org.jbake.app.ContentStore
import org.jbake.app.NoModelExtractorException
import org.jbake.app.RenderingException
import org.jbake.app.configuration.JBakeConfiguration
import org.jbake.template.model.TemplateModel
import java.io.IOException
import java.io.Writer

/** Renders pages using the [Pebble](https://pebbletemplates.io/) template engine. */
class PebbleTemplateEngine(config: JBakeConfiguration, db: ContentStore) : AbstractTemplateEngine(config, db) {
    private var engine: PebbleEngine? = null

    init {
        initializeTemplateEngine()
    }

    private fun initializeTemplateEngine() {
        val loader: Loader<*> = FileLoader()
        loader.setPrefix(config.templateDir.absolutePath)

        /** Turn off the auto-escaper because I believe that we can assume all data is safe considering it is all statically generated. */
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
            throw RenderingException("Templating engine Pebble complains: ${e.message}", e)
        } catch (e: IOException) {
            throw RenderingException("I/O error: ${e.message}", e)
        }
    }

    private fun wrap(model: TemplateModel) = object : TemplateModel(model) {

        override fun get(key: String): Any? {
            return try {
                extractors.extractAndTransform(db, key, this, TemplateEngineAdapter.NoopAdapter())
            }
            catch (e: NoModelExtractorException) { model[key] }
        }
    }
}
