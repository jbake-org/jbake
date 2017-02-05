package org.jbake.parser;

import java.io.File;
import java.util.Map;

import org.jbake.app.configuration.JBakeConfiguration;

public interface ParserEngine {

	Map<String, Object> parse(JBakeConfiguration config, File file);

}
