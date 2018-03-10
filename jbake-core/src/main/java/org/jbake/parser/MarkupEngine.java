package org.jbake.parser;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.IOUtils;
import org.jbake.app.Crawler;
import org.jbake.app.configuration.DefaultJBakeConfiguration;
import org.jbake.app.configuration.JBakeConfiguration;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private static final String UTF_8_BOM = "\uFEFF";

    private JBakeConfiguration configuration;

    /**
     * Tests if this markup engine can process the document.
     *
     * @param context the parser context
     * @return true if this markup engine has enough context to process this document. false otherwise
     */
    public boolean validate(ParserContext context) {
        return true;
    }

    /**
     * Processes the document header. Usually subclasses will parse the document body and look for
     * specific header metadata and export it into {@link ParserContext#getContents() contents} map.
     *
     * @param context the parser context
     */
    public void processHeader(final ParserContext context) {
    }

    /**
     * Processes the body of the document. Usually subclasses will parse the document body and render
     * it, exporting the result using the {@link org.jbake.parser.ParserContext#setBody(String)} method.
     *
     * @param context the parser context
     */
    public void processBody(final ParserContext context) {
    }

    @Override
    public Map<String, Object> parse(Configuration config, File file, String contentPath) {
        return parse(new DefaultJBakeConfiguration((CompositeConfiguration) config), file);
    }

    /**
     * Parse given file to extract as much infos as possible
     *
     * @param file file to process
     * @return a map containing all infos. Returning null indicates an error, even if an exception would be better.
     */
    public Map<String, Object> parse(JBakeConfiguration config, File file) {
        this.configuration = config;
        Map<String, Object> documentModel = new HashMap<>();
        List<String> fileContents;
        try (InputStream is = new FileInputStream(file)) {

            fileContents = IOUtils.readLines(is, config.getRenderEncoding());
        } catch (IOException e) {
            LOGGER.error("Error while opening file {}", file, e);

            return null;
        }

        boolean hasHeader = hasHeader(fileContents);
        ParserContext context = new ParserContext(
                file,
                fileContents,
                config,
                hasHeader,
                documentModel
        );

        if (hasHeader) {
            // read header from file
            processHeader(fileContents, documentModel);
        }
        // then read engine specific headers
        processHeader(context);

        setModelDefaultsIfNotSetInHeader(config, file, documentModel);
        sanitizeTags(config, documentModel);

        if (documentModel.get(Crawler.Attributes.TYPE) == null || documentModel.get(Crawler.Attributes.STATUS) == null) {
            // output error
            LOGGER.warn("Parsing skipped (missing type or status value in header meta data) for file {}!", file);
            return null;
        }

        // generate default body
        processBody(fileContents, documentModel);

        // eventually process body using specific engine
        if (validate(context)) {
            processBody(context);
        } else {
            LOGGER.error("Incomplete source file ({}) for markup engine: {}", file, getClass().getSimpleName());
            return null;
        }
        // TODO: post parsing plugins to hook in here?

        return documentModel;
    }

    private void sanitizeTags(JBakeConfiguration config, Map<String, Object> content) {
        if (content.get(Crawler.Attributes.TAGS) != null) {
            String[] tags = (String[]) content.get(Crawler.Attributes.TAGS);
            for (int i = 0; i < tags.length; i++) {
                tags[i] = sanitizeValue(tags[i]);
                if (config.getSanitizeTag()) {
                    tags[i] = tags[i].replace(" ", "-");
                }
            }
            content.put(Crawler.Attributes.TAGS, tags);
        }
    }

    private void setModelDefaultsIfNotSetInHeader(JBakeConfiguration config, File file, Map<String, Object> content) {
        if (content.get(Crawler.Attributes.DATE) == null) {
            content.put(Crawler.Attributes.DATE, new Date(file.lastModified()));
        }

        // default status has been set
        if (config.getDefaultStatus() != null && content.get(Crawler.Attributes.STATUS) == null) {
            // file hasn't got status so use default
            content.put(Crawler.Attributes.STATUS, config.getDefaultStatus());
        }

        // default type has been set
        if (config.getDefaultType() != null && content.get(Crawler.Attributes.TYPE) == null) {
            // file hasn't got type so use default
            content.put(Crawler.Attributes.TYPE, config.getDefaultType());
        }
    }

    /**
     * Checks if the file has a meta-data header.
     *
     * @param contents Contents of file
     * @return true if header exists, false if not
     */
    private boolean hasHeader(List<String> contents) {
        boolean headerValid = true;
        boolean statusFound = false;
        boolean typeFound = false;

        if ( ! hasHeaderSeparatorInContent( contents ) ) {
            return false;
        }

        for (String line : contents) {
            if (hasHeaderSeparator(line)) {
                LOGGER.debug("Header separator found");
                break;
            }
            if ( isTypeProperty(line) ) {
                LOGGER.debug("Type property found");
                typeFound = true;
            }

            if ( isStatusProperty(line) ) {
                LOGGER.debug("Status property found");
                statusFound = true;
            }
            if ( !line.isEmpty() && !line.contains("=") ) {
                LOGGER.error("Property found without assignment [{}]", line);
                headerValid = false;
            }
        }
        return headerValid && statusFound && typeFound;
    }

    private boolean hasHeaderSeparatorInContent(List<String> contents) {
        return contents.indexOf(configuration.getHeaderSeparator()) != -1;
    }

    private boolean hasHeaderSeparator(String line) {
        return line.equals(configuration.getHeaderSeparator());
    }

    private boolean isStatusProperty(String line) {
        return line.startsWith("status=");
    }

    private boolean isTypeProperty(String line) {
        return line.startsWith("type=");
    }

    /**
     * Process the header of the file.
     *  @param contents Contents of file
     * @param content
     */
    private void processHeader(List<String> contents, final Map<String, Object> content) {
        for (String line : contents) {

            if (hasHeaderSeparator(line)) {
                break;
            }

            processLine(line, content);
        }
    }

    private void processLine(String line, Map<String, Object> content) {
        String[] parts = line.split("=", 2);
        if (!line.isEmpty() && parts.length == 2) {


            String key = sanitizeKey(parts[0]);
            String value = sanitizeValue(parts[1]);

            if (key.equalsIgnoreCase(Crawler.Attributes.DATE)) {
                DateFormat df = new SimpleDateFormat(configuration.getDateFormat());
                try {
                    Date date = df.parse(value);
                    content.put(key, date);
                } catch (ParseException e) {
                    LOGGER.error("unable to parse date {}", value);
                }
            } else if (key.equalsIgnoreCase(Crawler.Attributes.TAGS)) {
                content.put(key, getTags(value));
            } else if (isJson(value)) {
                content.put(key, JSONValue.parse(value));
            } else {
                content.put(key, value);
            }
        }
    }

    private String sanitizeValue(String part) {
        return part.trim();
    }

    private String sanitizeKey(String part) {
        String key;
        if (part.contains(UTF_8_BOM)) {
            key = part.trim().replace(UTF_8_BOM, "");
        } else {
            key = part.trim();
        }
        return key;
    }

    private String[] getTags(String tagsPart) {
        String[] tags = tagsPart.split(",");
        for (int i = 0; i < tags.length; i++)
            tags[i] = sanitizeValue(tags[i]);
        return tags;
    }

    private boolean isJson(String part) {
        return part.startsWith("{") && part.endsWith("}");
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
            if (line.equals(configuration.getHeaderSeparator())) {
                inBody = true;
            }
        }

        if (body.length() == 0) {
            for (String line : contents) {
                body.append(line).append("\n");
            }
        }

        content.put(Crawler.Attributes.BODY, body.toString());
    }
}