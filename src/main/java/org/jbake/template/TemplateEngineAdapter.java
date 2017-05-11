package org.jbake.template;

/**
 * Adapts model extractor output to used template engine.
 * This method typically wraps results of model extractions into data types suited to template engine
 *
 * @author ndx
 */
public interface TemplateEngineAdapter<Type> {

    class NoopAdapter implements TemplateEngineAdapter<Object> {

        @Override
        public Object adapt(String key, Object extractedValue) {
            return extractedValue;
        }

    }

    /**
     * Adapt value to expected output
     *
     * @param key            Template key
     * @param extractedValue Value to be used in template model
     * @return Value adapted for use in template
     */
    Type adapt(String key, Object extractedValue);

}
