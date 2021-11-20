package org.jbake.app;

import org.jbake.app.configuration.JBakeConfiguration;
import org.jbake.model.DocumentModel;
import org.jbake.parser.Engines;
import org.jbake.parser.ParserEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Parses a File for content.
 *
 * @author Jonathan Bullock <a href="mailto:jonbullock@gmail.com">jonbullock@gmail.com</a>
 */
public class Parser {
    private static final Logger logger = LoggerFactory.getLogger(Parser.class);

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
     * @param file File input for parsing
     * @return The contents of the file
     */
    public DocumentModel processFile(File file) {
        ParserEngine engine = Engines.get(FileUtil.fileExt(file));
        if (engine == null) {
            logger.error("Unable to find suitable markup engine for {}", file);
            return null;
        }

        return engine.parse(config, file);
    }

    public String buildHash(File file) {
        try {
            return FileUtil.sha1(file);
        } catch (Exception e) {
            logger.error("unable to build sha1 hash for source file '{}'", file);
            return "";
        }
    }

    public String buildURI(File file) {
        ParserEngine engine = Engines.get(FileUtil.fileExt(file));
        if (engine == null) {
            logger.error("Unable to find suitable markup engine for {}", file);
            return null;
        }

        return engine.buildURI(config, file);
    }
}
