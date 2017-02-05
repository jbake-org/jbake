package org.jbake.app;

import org.jbake.app.configuration.JBakeConfiguration;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(Parser.class);

    private JBakeConfiguration config;

    /**
     * Creates a new instance of Parser.
     *
     * @param config Project configuration
     */
    public Parser(JBakeConfiguration config) {
        this.config = config;
    }

    /**
     * Process the file by parsing the contents.
     *
     * @param    file File input for parsing
     * @return				The contents of the file
     */
    public Map<String, Object> processFile(File file) {
    	ParserEngine engine = Engines.get(FileUtil.fileExt(file));
    	if (engine==null) {
    		LOGGER.error("Unable to find suitable markup engine for {}",file);
    		return null;
    	}
    	
    	return engine.parse(config, file);
    }
}
