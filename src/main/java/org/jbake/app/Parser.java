package org.jbake.app;

import org.apache.commons.configuration.Configuration;
import org.jbake.parser.Engines;
import org.jbake.parser.ParserEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;

/**
 * Parses a File for content.
 *
 * @author Jonathan Bullock <a href="mailto:jonbullock@gmail.com">jonbullock@gmail.com</a>
 */
public class Parser {
    private final static Logger LOGGER = LoggerFactory.getLogger(Parser.class);

    private Configuration config;
    private String contentPath;

    /**
     * Creates a new instance of Parser.
     *
     * @param config         Project configuration
     * @param contentPath    Content location
     */
    public Parser(Configuration config, String contentPath) {
        this.config = config;
        this.contentPath = contentPath;
    }

    /**
     * Process the file by parsing the contents.
     *
     * @param    file File input for parsing
     * @return   The contents of the file as a @{@link Map}
     */
    public Map<String, Object> processFile(File file) {
    	ParserEngine engine = Engines.get(FileUtil.fileExt(file));
    	if (engine==null) {
    		LOGGER.error("Unable to find suitable markup engine for {}",file);
    		return null;
    	}
    	
    	return engine.parse(config, file, contentPath);
    }
}
