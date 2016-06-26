package org.jbake.parser;

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

import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.IOUtils;
import org.jbake.app.ConfigUtil;
import org.jbake.app.ConfigUtil.Keys;
import org.jbake.app.Crawler;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for markup engine wrappers. A markup engine is responsible for rendering
 * markup in a source file and exporting the result into the {@link ParserContext#getContents() contents} map.
 *
 * This specific engine does nothing, meaning that the body is rendered as raw contents.
 *
 * @author Cédric Champeau
 */
public abstract class MarkupEngine implements ParserEngine {
    private static final Logger LOGGER = LoggerFactory.getLogger(MarkupEngine.class);
    /**
     * Tests if this markup engine can process the document.
     * @param context the parser context
     * @return true if this markup engine has enough context to process this document. false otherwise
     */
    public boolean validate(ParserContext context) { return true; }

    /**
     * Processes the document header. Usually subclasses will parse the document body and look for
     * specific header metadata and export it into {@link ParserContext#getContents() contents} map.
     * @param context the parser context
     */
    public void processHeader(final ParserContext context) {}

    /**
     * Processes the body of the document. Usually subclasses will parse the document body and render
     * it, exporting the result using the {@link org.jbake.parser.ParserContext#setBody(String)} method.
     * @param context the parser context
     */
    public void processBody(final ParserContext context) {}

    /**
     * Parse given file to extract as much infos as possible
     * @param file file to process
     * @return a map containing all infos. Returning null indicates an error, even if an exception would be better.
     */
	public Map<String, Object> parse(Configuration config, File file, String contentPath) {
    	
        Map<String,Object> content = new HashMap<String, Object>();
        InputStream is = null;
        List<String> fileContents = null;
        try {
            is = new FileInputStream(file);
            fileContents = IOUtils.readLines(is, config.getString("render.encoding"));
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

        if (hasHeader) {
            // read header from file
            processHeader(config, fileContents, content);
        }
        // then read engine specific headers
        processHeader(context);
        
        if (content.get(Crawler.Attributes.DATE) == null) {
        	content.put(Crawler.Attributes.DATE, new Date(file.lastModified()));
        }
        
        if (config.getString(Keys.DEFAULT_STATUS) != null) {
        	// default status has been set
        	if (content.get(Crawler.Attributes.STATUS) == null) {
        		// file hasn't got status so use default
        		content.put(Crawler.Attributes.STATUS, config.getString(Keys.DEFAULT_STATUS));
        	}
        }

        if (content.get(Crawler.Attributes.TYPE)==null||content.get(Crawler.Attributes.STATUS)==null) {
            // output error
        	LOGGER.warn("Error parsing meta data from header (missing type or status value) for file {}!", file);
            return null;
        }

        // generate default body
        processBody(fileContents, content);

        // eventually process body using specific engine
        if (validate(context)) {
            processBody(context);
        } else {
            LOGGER.error("Incomplete source file ({}) for markup engine:", file, getClass().getSimpleName());
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
     * @param config 
     *
     * @param contents Contents of file
     * @param content
     */
    private void processHeader(Configuration config, List<String> contents, final Map<String, Object> content) {
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
                        for( int i=0; i<tags.length; i++ )
                            tags[i]=tags[i].trim();
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
