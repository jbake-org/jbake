package org.jbake.parser;

import org.apache.commons.configuration.Configuration;
import org.jbake.app.configuration.JBakeConfiguration;

import java.io.File;
import java.util.Map;

public interface ParserEngine {

	/**
	 * Parse a given file and transform to a model representation used by {@link MarkdownEngine} implementations
	 * to render the file content.
	 * @param config The project configuration
	 * @param file The file to be parsed
	 * @return A model representation of the given file
	 */
	Map<String, Object> parse(JBakeConfiguration config, File file);

	/**
	 * @param config The project configuration
	 * @param file The file to be parsed
	 * @param contentPath unknown
	 * @return A model representation of the given file
	 * @deprecated use {@link #parse(JBakeConfiguration, File)} instead
	 */
	@Deprecated
	Map<String, Object> parse(Configuration config, File file, String contentPath);

}
