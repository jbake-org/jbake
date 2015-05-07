package org.jbake.app;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.io.IOUtils;
import org.jbake.app.ConfigUtil.Keys;
import org.jbake.parser.Engines;
import org.jbake.parser.MarkupEngine;
import org.jbake.parser.ParserContext;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Parses a File for content.
 *
 * @author Jonathan Bullock <jonbullock@gmail.com>
 */
public class Parser {
    private final static Logger LOGGER = LoggerFactory.getLogger(Parser.class);

    private CompositeConfiguration config;
    private String contentPath;

    /**
     * Creates a new instance of Parser.
     */
    public Parser(CompositeConfiguration config, String contentPath) {
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
        Map<String,Object> content = new HashMap<String, Object>();
        InputStream is = null;
        List<String> fileContents = null;
        try {
            is = new FileInputStream(file);
            fileContents = IOUtils.readLines(is, config.getString(Keys.RENDER_ENCODING));
        } catch (IOException e) {
            LOGGER.error("Error while opening file {}: {}", file, e);

            return null;
        } finally {
          IOUtils.closeQuietly(is);
        }

        boolean hasHeader = hasHeader(fileContents);
        ParserContext context = new ParserContext(
                file,
                fileContents,
                config,
                contentPath,
                hasHeader,
                content
        );

        MarkupEngine engine = Engines.get(FileUtil.fileExt(file));
        if (engine==null) {
            LOGGER.warn("Unable to find suitable markup engine for {}",file);
            return null;
        }

        if (hasHeader) {
            // read header from file
            processHeader(fileContents, content);
        }
        // then read engine specific headers
        engine.processHeader(context);
        
        if (config.getString(Keys.DEFAULT_STATUS) != null) {
        	// default status has been set
        	if (content.get("status") == null) {
        		// file hasn't got status so use default
        		content.put("status", config.getString(Keys.DEFAULT_STATUS));
        	}
        }

        if (content.get("type")==null||content.get("status")==null) {
            // output error
            LOGGER.warn("Error parsing meta data from header (missing type or status value) for file {}!", file);
            return null;
        }

        // generate default body
        processBody(fileContents, content);

        // eventually process body using specific engine
        if (engine.validate(context)) {
            engine.processBody(context);
        } else {
            LOGGER.warn("Incomplete source file ({}) for markup engine:", file, engine.getClass().getSimpleName());
            return null;
        }

        if (content.get("tags") != null) {
        	String[] tags = (String[]) content.get("tags");
            for( int i=0; i<tags.length; i++ ) {
                tags[i]=tags[i].trim();
                if (config.getBoolean(Keys.TAG_SANITIZE)) {
                	tags[i]=tags[i].replace(" ", "-");
                }
            }
            content.put("tags", tags);
        }
        
        // TODO: post parsing plugins to hook in here?
        
        return content;
    }

    /**
     * Checks if the file has a meta-data header.
     *
     * @param contents Contents of file
     * @return true if header exists, false if not
     */
    private boolean hasHeader(List<String> contents) {
        boolean headerValid = false;
        boolean headerSeparatorFound = false;
        boolean statusFound = false;
        boolean typeFound = false;

        List<String> header = new ArrayList<String>();

        for (String line : contents) {
            header.add(line);
            if (line.contains("=")) {
                if (line.startsWith("type=")) {
                    typeFound = true;
                }
                if (line.startsWith("status=")) {
                    statusFound = true;
                }
            }
            if (line.equals("~~~~~~")) {
                headerSeparatorFound = true;
                header.remove(line);
                break;
            }
        }

        if (headerSeparatorFound) {
            headerValid = true;
            for (String headerLine : header) {
                if (!headerLine.contains("=")) {
                    headerValid = false;
                    break;
                }
            }
        }

        if (!headerValid || !statusFound || !typeFound) {
            return false;
        }
        return true;
    }

    /**
     * Process the header of the file.
     *
     * @param contents Contents of file
     * @param content
     */
    private void processHeader(List<String> contents, final Map<String, Object> content) {
        for (String line : contents) {
            if (line.equals("~~~~~~")) {
                break;
            } else {
                String[] parts = line.split("=",2);
                if (parts.length == 2) {
                    if (parts[0].equalsIgnoreCase("date")) {
                        DateFormat df = new SimpleDateFormat(config.getString(Keys.DATE_FORMAT));
                        Date date = null;
                        try {
                            date = df.parse(parts[1]);
                            content.put(parts[0], date);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    } else if (parts[0].equalsIgnoreCase("tags")) {
                        String[] tags = parts[1].split(",");
                        content.put(parts[0], tags);
                    } else if (parts[1].startsWith("{") && parts[1].endsWith("}")) {
                        // Json type
                        content.put(parts[0], JSONValue.parse(parts[1]));
                    } else {
                        content.put(parts[0], parts[1]);
                    }
                }
            }
        }
    }

    /**
     * Process the body of the file.
     *
     * @param contents Contents of file
     * @param content
     */
    private void processBody(List<String> contents, final Map<String, Object> content) {
        StringBuilder body = new StringBuilder();
        boolean inBody = false;
        for (String line : contents) {
            if (inBody) {
                body.append(line).append("\n");
            }
            if (line.equals("~~~~~~")) {
                inBody = true;
            }
        }

        if (body.length() == 0) {
            for (String line : contents) {
                body.append(line).append("\n");
            }
        }
        
        content.put("body", body.toString());
    }

}
