package org.jbake.template

import org.apache.commons.configuration2.CompositeConfiguration
import org.jbake.app.ContentStore
import org.jbake.app.FileUtil
import org.jbake.app.configuration.JBakeConfiguration
import org.jbake.template.model.TemplateModel
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.Writer

/**
 * A template which is responsible for delegating to a supported template engine,
 * based on the file extension.
 */
class DelegatingTemplateEngine : AbstractTemplateEngine {
    private val renderers: TemplateEngines

    /**
     * @param templatesPath the templates path
     */
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
        model.version = (config.version)
        model.config = run {
            val m: MutableMap<String, Any> = HashMap()
            val it = config.keys
            while (it.hasNext()) {
                val k = it.next()
                val v = config.get(k)
                if (v != null) m[k] = v
            }
            m
        }
        // Also keep a reference to the original configuration object so typed extractors can access
        // the configuration with its original dotted keys. ModelExtractorAdapter will prefer this
        // when reconstructing a RenderContext.
        model.put("jbake_config", config)

        // if default template exists we will use it
        val templateFolder = config.templateFolder
        var templateFile = File(templateFolder, templateName)
        var theTemplateName = templateName
        if (!templateFile.exists()) {
            log.info("Default template: {} was not found, searching for others...", templateName)
            // if default template does not exist then check if any alternative engine templates exist
            val templateNameWithoutExt = templateName.dropLast(4)
            for (extension in renderers.recognizedExtensions) {
                templateFile = File(templateFolder, "$templateNameWithoutExt.$extension")
                if (templateFile.exists()) {
                    log.info("Found alternative template file: {} using this instead", templateFile.getName())
                    theTemplateName = templateFile.getName()
                    break
                }
            }
        }
        val ext = FileUtil.fileExt(theTemplateName)
        val engine = renderers.getEngine(ext)
        if (engine != null) {
            engine.renderDocument(model, theTemplateName, writer)
        } else {
            log.error("Warning - No template engine found for template: {}", theTemplateName)
        }
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(DelegatingTemplateEngine::class.java)
    }
}
