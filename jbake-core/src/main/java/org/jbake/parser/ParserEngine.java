package org.jbake.parser;

import java.io.File;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.jbake.app.configuration.JBakeConfiguration;

public interface ParserEngine {

	Map<String, Object> parse(JBakeConfiguration config, File file);

	/**
	 * @deprecated use {@link #parse(JBakeConfiguration, File)} instead
	 * @param config
	 * @param file
	 * @param contentPath
	 * @return
	 */
	@Deprecated
	Map<String, Object> parse(Configuration config, File file, String contentPath);

}
