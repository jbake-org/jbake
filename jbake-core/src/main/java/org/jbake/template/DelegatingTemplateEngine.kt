package org.jbake.template

import org.apache.commons.configuration2.CompositeConfiguration
import org.jbake.app.ContentStore
import org.jbake.app.FileUtil
import org.jbake.app.configuration.JBakeConfiguration
import org.jbake.template.model.TemplateModel
import org.slf4j.Logger
import org.jbake.util.Logging.logger
import java.io.File
import java.io.Writer

/**
 * A template which is responsible for delegating to a supported template engine,
 * based on the file extension.
 */
class DelegatingTemplateEngine : AbstractTemplateEngine {
    private val renderers: TemplateEngines

    @Deprecated("""Use {@link #DelegatingTemplateEngine(ContentStore, JBakeConfiguration)} instead.""")
    constructor(config: CompositeConfiguration, db: ContentStore, destination: File, templatesPath: File)
        : super(config, db, destination, templatesPath)
    {
        this.renderers = TemplateEngines(this.config, db)
    }

    constructor(db: ContentStore, config: JBakeConfiguration) : super(config, db) {
        this.renderers = TemplateEngines(config, db)
    }

    @Throws(RenderingException::class)
    override fun renderDocument(model: TemplateModel, templateName: String, writer: Writer) {
        model.version = (config.jbakeVersion)
        model.config = run {
            // Use configuration's asHashMap which provides underscore-style keys and defaults similar to legacy behavior.
            val base: MutableMap<String, Any> = config.asHashMap()

            // Also expose dotted-key aliases for code that prefers dotted keys
            val m: MutableMap<String, Any> = HashMap(base)
            base.forEach { (k, v) -> m.putIfAbsent(k.replace('_', '.'), v) }
            m
        }
        // Also keep a reference to the original configuration object so typed extractors can access the configuration with its original dotted keys.
        // ModelExtractorAdapter will prefer this when reconstructing a RenderContext.
        model["jbake_config"] = config

        // If default template exists we will use it.
        val templateDir = config.templateDir
        var templateFile = File(templateDir, templateName)
        var theTemplateName = templateName

        if (!templateFile.exists()) {
            log.info("Default template: {} was not found, searching for others...", templateName)
            // if default template does not exist then check if any alternative engine templates exist
            val templateNameWithoutExt = templateName.dropLast(4)
            for (extension in renderers.recognizedExtensions) {
                templateFile = File(templateDir, "$templateNameWithoutExt.$extension")
                if (templateFile.exists()) {
                    log.info("Found alternative template file: {} using this instead", templateFile.getName())
                    theTemplateName = templateFile.getName()
                    break
                }
            }
        }

        val ext = FileUtil.fileExt(theTemplateName)
        val engine = renderers.getEngine(ext)
        if (engine != null)
            engine.renderDocument(model, theTemplateName, writer)
        else log.error("Warning - No template engine found for template: {}", theTemplateName)
    }

    private val log: Logger by logger()
}
