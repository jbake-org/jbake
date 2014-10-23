package org.jbake.template;

/**
 * Adapts model extractor output to used template engine.
 * This method typiocally wraps results of model extractions into data types suited to template engine
 * @author ndx
 *
 */
public interface TemplateEngineAdapter<Type> {
	public static class NoopAdapter implements TemplateEngineAdapter<Object> {

		@Override
		public Object adapt(String key, Object extractedValue) {
			return extractedValue;
		}
		
	}

	/**
	 * Adapt value to expected output
	 * @param key
	 * @param extractedValue
	 * @return
	 */
	Type adapt(String key, Object extractedValue);

}
