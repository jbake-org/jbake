package org.jbake.template

import org.apache.commons.configuration2.CompositeConfiguration
import org.apache.commons.configuration2.Configuration
import org.jbake.app.ContentStore
import org.jbake.app.configuration.JBakeConfiguration
import org.jbake.app.configuration.JBakeConfigurationFactory
import org.jbake.template.model.TemplateModel
import java.io.File
import java.io.Writer

/**
 * A template is responsible for converting a model into a rendered document. The model
 * consists of key/value pairs, some of them potentially converted from a markup language
 * to HTML already.
 *
 *
 * An appropriate rendering engine will be chosen by JBake based on the template suffix. If
 * contents is not available in the supplied model, a template has access to the document
 * database in order to complete the model. It is in particular interesting to optimize
 * data access based on the underlying template engine capabilities.
 *
 *
 * Note that some rendering engines may rely on a different rendering model than the one
 * provided by the first argument of [.renderDocument].
 * In this case, it is the responsibility of the engine to convert it.
 *
 * @author Cédric Champeau
 */
abstract class AbstractTemplateEngine protected constructor(
    protected val config: JBakeConfiguration?,
    protected val db: ContentStore?
) {
    @Deprecated("use {@link AbstractTemplateEngine(JBakeConfiguration,ContentStore)} instead")
    protected constructor(config: Configuration?, db: ContentStore?, destination: File?, templatesPath: File) : this(
        JBakeConfigurationFactory().createDefaultJbakeConfiguration(templatesPath.getParentFile(), destination, config as CompositeConfiguration?),
        db
    )

    @Throws(RenderingException::class)
    abstract fun renderDocument(model: TemplateModel?, templateName: String?, writer: Writer?)

    companion object {
        protected var extractors: ModelExtractors? = ModelExtractors.instance
    }
}
