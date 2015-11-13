package org.jbake.app;

import static com.google.common.base.Preconditions.*;
import static java.lang.String.format;
import static org.jbake.app.ContentTag.*;
import static org.jbake.app.excerpt.Truncator.*;

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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.jbake.app.ConfigUtil.Keys;
import org.jbake.app.excerpt.TruncateContentHandler.Unit;
import org.jbake.app.excerpt.Truncator;
import org.jbake.app.excerpt.TruncatorException;
import org.jbake.parser.Engines;
import org.jbake.parser.MarkupEngine;
import org.jbake.parser.ParserContext;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parses a File for content.
 *
 * @author Jonathan Bullock <jonbullock@gmail.com>
 */
public class Parser {

	private enum Readmore { with, without} ;

	public static final String END_OF_HEADER = createStringFilledWith('~', 6);
	public static final String EOL = "\n";
	public static final String CONTINUED_LINE_STARTER = createStringFilledWith(' ', 2);

	private final static Logger LOGGER = LoggerFactory.getLogger(Parser.class);

    private CompositeConfiguration config;
    private String contentPath;

    public static class PostParsingProcessor {
    	
    	private final Content content;
    	
    	public PostParsingProcessor() {
    		this.content = new Content();
    	}
    	
    	/**
    	 * Do the job.
    	 */
	    public Content process(final Content content) {
		    checkArgument(content != null, "The parser post parsing processor requires a content output.");
		    this.content.putAll(content);
			doProcess(content);
			return content;
		}
		
		/**
		 * Define the job.
		 * 
		 * This method is available to be overridden with access to the input (source)
		 * and the output (content) of the parsing.
		 * 
		 * Both arguments are always provided with non null value.
		 */
		public void doProcess(final Content content) {}
    	
    }
    
    /**
     * Creates a new instance of Parser.
     */
    public Parser(final CompositeConfiguration config, final String contentPath) {
        this.config = config;
        this.contentPath = contentPath;
    }

    /**
     * Parse a file to extract and format its contents.
     * 
     * Kept to prevent API break.
     *
     * @param    file
     * @return The contents of the file
     * 
     * @deprecated see {@link #processSource(File)}
     */
    @Deprecated
    public Map<String, Object> processFile(final File file) {
        Content content = processSource(file);
        return content == null ? null : content.getContentAsMap();
    }

        
    public Content processSource(final File file) {
    	Exception e;
    	try {
    		return process(file, new PostParsingProcessor());
    	} catch(IllegalStateException ise) {
    		e = ise;
    	} catch(IllegalArgumentException iae) {
    		e = iae;
    	}
		LOGGER.error(e.getMessage(), e);
		return null;
    }

    /**
     * Process the file embedded in the PostParsingProcessor instance provided.
     * 
     * End the job by running the post parsing processor itself. 
     * By default this processor does nothing.
     *
     * @param    post parsing processor
     * @return The contents of the file provided
     */
    public Content process(final File file, final PostParsingProcessor postParsingProcessor) {
    	return doProcess(file, postParsingProcessor);
    }
    
    private Content doProcess(final File file, final PostParsingProcessor postParsingProcessor) {
    	checkState(postParsingProcessor != null, "The parser requires a post parsing processor.");

    	final List<String> fileContents = retrieveContentFromFile(file);
    	if (fileContents == null) {
    	    return null;
    	}
    	
    	final Content content = new Content();

        final List<String> header = getHeaderFrom(fileContents);
        final ParserContext context = new ParserContext(
                file,
                fileContents,
                config,
                contentPath,
                header != null,
                content
        );

        final MarkupEngine engine = Engines.get(FileUtil.fileExt(file));
        if (engine==null) {
            LOGGER.warn("Unable to find suitable markup engine for {}",file);
            return null;
        }

        // read header from file
        processHeader(header, content);
        
        // then read engine specific headers
        engine.processHeader(context);
        
        content.tryToSetupStatusIfNeededWithDefaultValue(getDefaultStatus());
 
        if (! content.isWithValidHeader()) {
            // output error
            LOGGER.warn("Error parsing meta data from header (missing type or status value) for file {}!", file);
            return null;
        }

        // generate default body
        processBody(fileContents, content);

        if (! engine.validate(context)) {
            LOGGER.warn("Incomplete source file ({}) for markup engine:", file, engine.getClass().getSimpleName());
            return null;
        }
        // eventually process body using specific engine
        engine.processBody(context);

        sanitizeTagsInside(content);

        // TODO: post parsing plugins to hook in here?

        postParsingProcessor.process(content);

        createSummaryForHomePage(context);
        createSummaryForFeedPage(context);

        return content;
    }
    
    private List<String> retrieveContentFromFile(final File file) {
        InputStream is = null;
        try {
            is = new FileInputStream(file);
            return IOUtils.readLines(is, config.getString(Keys.RENDER_ENCODING));
        } catch (IOException e) {
            LOGGER.error("Error while opening file {}: {}", file, e);
            return null;
        } finally {
          IOUtils.closeQuietly(is);
        }
    }
    
    private ContentStatus getDefaultStatus() {
        String s = config.getString(Keys.DEFAULT_STATUS, null);
        if (s == null) {
            return null;
        }
        try {
            return ContentStatus.valueOf(s);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("A content status must have a valid value.", e);
        }
    }
    
    private void createSummaryForHomePage(final ParserContext context) {
        final Content content = context.getContent();
        String summary;
        if (!config.getBoolean(Keys.INDEX_SUMMERY, false)) {
            summary = content.getString(body, null);
        } else {
            try {
                summary = getSummary(context, Readmore.with);
            } catch (TruncatorException e) {
                LOGGER.error("Unable to provide a summary or excerpt for home page.");
                summary = content.getString(body, null);
            }
        }
    	content.put(summaryForHome, summary);
    }
    
    private void createSummaryForFeedPage(final ParserContext context) {
        final Content content = context.getContent();
        String summary;
        if (!config.getBoolean(Keys.FEED_SUMMERY, false)) {
            summary = content.getString(body, null);
        } else {
            try {
                summary = getSummary(context, Readmore.without);
            } catch (Exception e) {
                LOGGER.error("Unable to provide a summary or excerpt for feed page.");
                summary = content.getString(body, null);
            }
        }
    	summary = StringEscapeUtils.escapeXml(summary);
    	content.put(summaryForFeed, summary);
    }
    
    private String getSummary(final ParserContext context, final Readmore with) throws TruncatorException 
    {
    	final Content content = context.getContent();
    	
    	final String contentSummary = content.getString(summary, "").trim();
    	// TODO check if the URI can be null outside unit tests 
    	final String contentUri = content.getString(uri, "???");
    	final String readmorePattern = content.getString(readmore, config.getString(readmore.key(), DEFAULT_READ_MORE));
    	final String contentReadmore = with == Readmore.with ? format(readmorePattern, contentUri) : "";
    	if (!contentSummary.isEmpty()) {
    		// if a summary is provided in the header, it is never truncated
    		return contentSummary + contentReadmore;
    	} 

        final int excerptMaxLength = content.getInt(summaryLength, config.getInt(summaryLength.key(), NO_LIMIT));
        
    	final String contentbody = content.getString(body, null);
        final String contentEllipsis = content.getString(ellipsis, config.getString(ellipsis.key(), DEFAULT_ELLIPSIS));
    	
    	return new Truncator(getDefaultExcerptCounterUnit(config), excerptMaxLength)
    			.readmore(contentReadmore)
    			.ellipsis(contentEllipsis)
    			.source(contentbody)
    			.run();
    }
    
    private Unit getDefaultExcerptCounterUnit(CompositeConfiguration config) {
        Unit defaultUnit = Unit.word;
        if (! config.containsKey(summaryUnit.key())) {
            return defaultUnit;
        }
        String configUnit = config.getString(summaryUnit.key());
        try {
            return Unit.valueOf(configUnit);
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Truncator default unit, wrong value in jbake configuration: {}", configUnit);
        } 
        return defaultUnit;
    }
    
    private void sanitizeTagsInside(final Content content) {
    	String[] tagValues = (String[]) content.get(tags);
    	if (tagValues == null) { 
    		return;
    	}
    	
        for( int i=0; i<tagValues.length; i++ ) {
        	tagValues[i]=tagValues[i].trim();
            if (config.getBoolean(Keys.TAG_SANITIZE)) {
            	tagValues[i]=tagValues[i].replace(" ", "-");
            }
        }
        content.put(tags, tagValues);
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

    /**
     * Process the header of the file.
     * 
     * All the header entries are valid ones, i.e. with the syntax "key = value", without leading spaces
     *
     * @param header valid header of the file
     * @param content
     */
    private void processHeader(List<String> header, final Content content) {
    	if (header == null) {
    		return;
    	}
        for (String line : header) {
            if (line.equals(END_OF_HEADER)) {
                break;
            } 
            
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
    private void processBody(List<String> contents, final Content content) {
        StringBuilder contentBody = new StringBuilder();
        boolean inBody = false;
        for (String line : contents) {
            if (inBody) {
                contentBody.append(line).append(EOL);
            }
            if (line.equals(END_OF_HEADER)) {
                inBody = true;
            }
        }

        if (contentBody.length() == 0) {
            for (String line : contents) {
                contentBody.append(line).append(EOL);
            }
        }
        
        content.put(body, contentBody.toString());
    }

    private static String createStringFilledWith(char c, int length) {
    	final char[] array = new char[length];
        Arrays.fill(array, c);
        return new String(array);
    }

    // a la java properties file
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

}
