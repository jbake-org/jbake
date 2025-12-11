package org.jbake.template

import org.jbake.app.ContentStore
import org.jbake.app.RenderingException
import org.jbake.app.configuration.JBakeConfiguration
import org.jbake.template.model.JbakeTemplateModel
import java.io.Writer

/**
 * A template engine is responsible for converting a model into a rendered document. The model
 * consists of key/value pairs, some of them potentially converted from a markup language
 * to HTML already.
 *
 * An appropriate rendering engine will be chosen by JBake based on the template suffix.
 * If contents is not available in the supplied model, a template has access to the document database
 * in order to complete the model. It is in particular interesting to optimize
 * data access based on the underlying template engine capabilities.
 *
 * Note that some rendering engines may rely on a different rendering model than the one provided by the first argument of [.renderDocument].
 * In this case, it is the responsibility of the engine to convert it.
 */
abstract class AbstractTemplateEngine protected constructor(
    protected val config: JBakeConfiguration,
    protected val db: ContentStore
) {
    @Throws(RenderingException::class)
    abstract fun renderDocument(model: JbakeTemplateModel, templateName: String, writer: Writer)

    companion object {
        internal var extractors: ModelExtractorsRegistry = ModelExtractorsRegistry.instance
    }
}



/**
 * Adapts model extractor output to used template engine.
 * This method typically wraps results of model extractions into data types suited to the template engine.
 */
interface TemplateEngineAdapter<Type> {

    /**
     * Adapt value to expected output.
     */
    fun adapt(key: String, extractedValue: Any): Type


    class NoopAdapter : TemplateEngineAdapter<Any> {
        override fun adapt(key: String, extractedValue: Any): Any {
            return extractedValue
        }
    }
}
