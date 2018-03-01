package org.jbake.parser;

import java.io.File;
import java.util.Map;

import org.apache.commons.configuration.Configuration;

public interface ParserEngine {

	Map<String, Object> parse(Configuration config, File file, String contentPath);

}
