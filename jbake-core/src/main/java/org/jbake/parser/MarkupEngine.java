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
import java.util.List;
import java.util.Map;

/**
 * Base class for markup engine wrappers. A markup engine is responsible for rendering
 * markup in a source file and exporting the result into the {@link ParserContext#getDocumentModel() contents} map.
 * <p>
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
     * specific header metadata and export it into {@link ParserContext#getDocumentModel() contents} map.
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
                hasHeader
        );

        if (hasHeader) {
            // read header from file
            processDefaultHeader(context);
        }
        // then read engine specific headers
        processHeader(context);

        setModelDefaultsIfNotSetInHeader(context);
        sanitizeTags(context);

        if (context.getType().isEmpty() || context.getStatus().isEmpty()) {
            // output error
            LOGGER.warn("Parsing skipped (missing type or status value in header meta data) for file {}!", file);
            return null;
        }

        // generate default body
        processDefaultBody(context);

        // eventually process body using specific engine
        if (validate(context)) {
            processBody(context);
        } else {
            LOGGER.error("Incomplete source file ({}) for markup engine: {}", file, getClass().getSimpleName());
            return null;
        }
        // TODO: post parsing plugins to hook in here?

        return context.getDocumentModel();
    }

    private void sanitizeTags(ParserContext context) {
        if (context.getTags() != null) {
            String[] tags = (String[]) context.getTags();
            for (int i = 0; i < tags.length; i++) {
                tags[i] = sanitizeValue(tags[i]);
                if (context.getConfig().getSanitizeTag()) {
                    tags[i] = tags[i].replace(" ", "-");
                }
            }
            context.setTags(tags);
        }
    }

    private void setModelDefaultsIfNotSetInHeader(ParserContext context) {
        if (context.getDate() == null) {
            context.setDate(new Date(context.getFile().lastModified()));
        }

        // default status has been set
        if (context.getConfig().getDefaultStatus() != null && context.getStatus().isEmpty()) {
            // file hasn't got status so use default
            context.setDefaultStatus();
        }

        // default type has been set
        if (context.getConfig().getDefaultType() != null && context.getType().isEmpty()) {
            // file hasn't got type so use default
            context.setDefaultType();
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

        if (!headerSeparatorDemarcatesHeader(contents)) {
            return false;
        }

        for (String line : contents) {
            if (hasHeaderSeparator(line)) {
                LOGGER.debug("Header separator found");
                break;
            }
            if (isTypeProperty(line)) {
                LOGGER.debug("Type property found");
                typeFound = true;
            }

            if (isStatusProperty(line)) {
                LOGGER.debug("Status property found");
                statusFound = true;
            }
            if (!line.isEmpty() && !line.contains("=")) {
                LOGGER.error("Property found without assignment [{}]", line);
                headerValid = false;
            }
        }
        return headerValid && (statusFound || hasDefaultStatus()) && (typeFound || hasDefaultType());
    }

    private boolean hasDefaultType() {
        return !configuration.getDefaultType().isEmpty();
    }

    private boolean hasDefaultStatus() {
        return !configuration.getDefaultStatus().isEmpty();
    }

    private boolean hasHeaderSeparatorInContent(List<String> contents) {
        return contents.indexOf(configuration.getHeaderSeparator()) != -1;
    }

    /**
     * Checks if header separator demarcates end of metadata header
     *
     * @param contents
     * @return true if header separator resides at end of metadata header, false if not
     */
    private boolean headerSeparatorDemarcatesHeader(List<String> contents) {
        List<String> subContents = null;
        int index = contents.indexOf(configuration.getHeaderSeparator());
        if (index != -1) {
            // get every line above header separator
            subContents = contents.subList(0, index);

            for (String line : subContents) {
                // header should only contain empty lines or lines with '=' in
                if (!line.contains("=") && !line.isEmpty())  {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
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
     *
     * @param context the parser context
     */
    private void processDefaultHeader(ParserContext context) {
        for (String line : context.getFileLines()) {

            if (hasHeaderSeparator(line)) {
                break;
            }
            processLine(line, context.getDocumentModel());
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
     * @param context the parser context
     */
    private void processDefaultBody(ParserContext context) {
        StringBuilder body = new StringBuilder();
        boolean inBody = false;
        for (String line : context.getFileLines()) {
            if (inBody) {
                body.append(line).append("\n");
            }
            if (line.equals(configuration.getHeaderSeparator())) {
                inBody = true;
            }
        }

        if (body.length() == 0) {
            for (String line : context.getFileLines()) {
                body.append(line).append("\n");
            }
        }
        context.setBody(body.toString());
    }
}