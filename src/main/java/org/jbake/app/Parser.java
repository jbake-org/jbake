package org.jbake.app;

import java.io.File;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.jbake.parser.Engines;
import org.jbake.parser.ParserEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parses a File for content.
 *
 * @author Jonathan Bullock <jonbullock@gmail.com>
 */
public class Parser {
    private final static Logger LOGGER = LoggerFactory.getLogger(Parser.class);

    private Configuration config;
    private String contentPath;

    /**
     * Creates a new instance of Parser.
     */
    public Parser(Configuration config, String contentPath) {
        this.config = config;
        this.contentPath = contentPath;
    }

    /**
     * Process the file by parsing the contents.
     *
     * @param    file
     * @return The contents of the file
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
