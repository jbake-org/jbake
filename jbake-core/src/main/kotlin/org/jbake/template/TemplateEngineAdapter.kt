package org.jbake.template

/**
 * Adapts model extractor output to used template engine.
 * This method typically wraps results of model extractions into data types suited to template engine
 */
interface TemplateEngineAdapter<Type> {

    /**
     * Adapt value to expected output.
     *
     * @return Value adapted for use in template
     */
    fun adapt(key: String, extractedValue: Any): Type


    class NoopAdapter : TemplateEngineAdapter<Any> {
        override fun adapt(key: String, extractedValue: Any): Any {
            return extractedValue
        }
    }
}
