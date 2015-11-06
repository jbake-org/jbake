package org.jbake.app;

import static org.jbake.app.Parser.ContentBasicTags.*;

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
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Parses a File for content.
 *
 * @author Jonathan Bullock <jonbullock@gmail.com>
 */
public class Parser {
	
	public static final String END_OF_HEADER = createStringFilledWith('~', 6);
	public static final String EOL = "\n";
	public static final String CONTINUED_LINE_STARTER = createStringFilledWith(' ', 2);
	
	public enum ContentBasicTags { status, type, date, tags, body };
	
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

        // read header from file
        List<String> header = getHeaderFrom(fileContents);
        boolean hasValidHeader = header != null;
        ParserContext context = new ParserContext(
                file,
                fileContents,
                config,
                contentPath,
                hasValidHeader,
                content
        );

        MarkupEngine engine = Engines.get(FileUtil.fileExt(file));
        if (engine==null) {
            LOGGER.warn("Unable to find suitable markup engine for {}",file);
            return null;
        }

        if (hasValidHeader) {
            content = processHeader(header, content);
        }
        // then read engine specific headers
        engine.processHeader(context);
        
        if (config.getString(Keys.DEFAULT_STATUS) != null) {
        	// default status has been set
        	if (content.get(status.name()) == null) {
        		// file hasn't got status so use default
        		content.put(status.name(), config.getString(Keys.DEFAULT_STATUS));
        	}
        }

        if (content.get(type.name())==null||content.get(status.name())==null) {
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

        sanitizeTagsInside(content);
        
        // TODO: post parsing plugins to hook in here?
        
        return content;
    }
    
    private void sanitizeTagsInside(Map<String, Object> content) {
    	String[] tagValues = (String[]) content.get(tags.name());
    	if (tagValues == null) { 
    		return;
    	}
    	
        for( int i=0; i<tagValues.length; i++ ) {
        	tagValues[i]=tagValues[i].trim();
            if (config.getBoolean(Keys.TAG_SANITIZE)) {
            	tagValues[i]=tagValues[i].replace(" ", "-");
            }
        }
        content.put(tags.name(), tagValues);
    }
    
    /**
     * Checks if the file has a meta-data header.
     *
     * @param contents Contents of file
     * @return map if header exists and is valid, null if not
     */
    private List<String> getHeaderFrom(final List<String> contents) {
        boolean headerSeparatorFound = false;
        boolean statusFound = false;
        boolean typeFound = false;

        List<String> header = new ArrayList<String>();
        
        StringBuilder buffer = new StringBuilder();
        for (String line : contents) {
        	if (line.trim().isEmpty()) {
        		continue;
        	}
        	boolean newEntry = ! isContinuedEntry(line);
        	if (buffer.length() > 0 && newEntry) {
        		String e = buffer.toString();
        		header.add(e);
        		
            	if (! isValidEntry(e)) {
            		return null;
            	}
        		statusFound |= isStatusEntry(e);
        		typeFound |= isTypeEntry(e);
        		
        		buffer.setLength(0);
        	} 
        	if (line.equals(END_OF_HEADER)) {
        		headerSeparatorFound = true;
        		break;
        	}
        	String e = newEntry ? line : line.substring(CONTINUED_LINE_STARTER.length());
        	buffer.append(e);
        }
        
        return headerSeparatorFound && statusFound && typeFound ? header : null;
    }
    
    private static boolean isContinuedEntry(final String e) {
    	return e.matches("^" + CONTINUED_LINE_STARTER + ".*");
    }
    
    private static boolean isStatusEntry(final String e) {
    	return e.matches("^" + status + "\\s*=.*");
    }
    
    private static boolean isTypeEntry(final String e) {
    	return e.matches("^" + type + "\\s*=.*");
    }
    
    private static boolean isValidEntry(final String e) {
    	return e.contains("=");
    }
        
    /**
     * Process the header of the file.
     *
     * @param header list of entries
     * @param content
     */
    private final Map<String, Object> processHeader(final List<String> header, final Map<String, Object> content) {
        for (String line : header) {
            Entry<String, String> entry = getHeaderEntryFrom(line);
            String key = entry.getKey();
            String value = entry.getValue();
            if (key.equalsIgnoreCase(date.name())) {
                DateFormat df = new SimpleDateFormat(config.getString(Keys.DATE_FORMAT));
                Date date = null;
                try {
                    date = df.parse(value);
                    content.put(key, date);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            } else if (key.equalsIgnoreCase(tags.name())) {
                String[] tags = value.split(",");
                content.put(key, tags);
            } else if (value.startsWith("{") && value.endsWith("}")) {
                // Json type
                content.put(key, JSONValue.parse(value));
            } else {
                content.put(key, value);
            }
        }
        return content;
    }
    
    private static Entry<String, String> getHeaderEntryFrom(final String line) {
    	String[] parts = line.split("\\s*=\\s*", 2);
    	return new AbstractMap.SimpleEntry<String, String>(parts[0], parts[1]);
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
                body.append(line).append(EOL);
            }
            if (line.equals(END_OF_HEADER)) {
                inBody = true;
            }
        }

        if (body.length() == 0) {
            for (String line : contents) {
                body.append(line).append(EOL);
            }
        }
        
        content.put(ContentBasicTags.body.name(), body.toString());
    }

    private static String createStringFilledWith(char c, int length) {
    	final char[] array = new char[length];
        Arrays.fill(array, c);
        return new String(array);
    }

}
