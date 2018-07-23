package org.jbake.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jbake.app.Crawler;
import org.jbake.app.DebugUtil;
import org.jbake.app.JBakeException;
import org.jbake.app.configuration.DefaultJBakeConfiguration;
import org.jbake.app.configuration.JBakeConfiguration;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public static final int MAX_HEADER_LINES = 50;
    public static final Pattern HEADER_LINE_REGEX = Pattern.compile("^[\\p{Alnum}.-_]+\\p{Blank}*(=|:)\\p{Blank}*[^\\n\\p{Cntrl}]*\\p{Blank}*");


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
     * Validates the core requirements the documents have to fulfil.
     */
    protected void validateInternal(ParserContext context) {
        validateDocumentAttribute(context, Crawler.Attributes.TITLE, String.class);
        validateDocumentAttribute(context, Crawler.Attributes.DATE, Date.class);
        validateDocumentAttribute(context, Crawler.Attributes.TYPE, String.class);
    }

    private void validateDocumentAttribute(ParserContext context, String attrName, Class<?> expectedType)
    {
        Object type = context.getDocumentModel().get(attrName);
        if (null == type)
            throw new JBakeException("Document doesn't have a " + attrName + ": " + context.getFile().getPath());
        if (!expectedType.isAssignableFrom(type.getClass()))
            throw new JBakeException("Document's " + attrName + " is not a " + expectedType.getName() + ", but " + type.getClass().getCanonicalName() + ": " + context.getFile().getPath());
    }


    /**
     * Processes the document header. Usually subclasses will parse the document body and look for
     * specific header metadata and export it into {@link ParserContext#getDocumentModel() contents} map.
     *
     * @param context the parser context
     */
    /*public void parseHeaderBlock(final ParserContext context) {
    }*/

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
        List<String> fileLines;

        try (InputStream is = new FileInputStream(file)) {
            // TODO: This should be done using streams for performance and memory reasons.
            fileLines = IOUtils.readLines(is, config.getInputCharset());
        } catch (IOException e) {
            LOGGER.error("Error while opening file {}", file, e);

            return null;
        }

        boolean hasHeader = hasHeader(fileLines);

        ParserContext context = new ParserContext(
                file,
                fileLines,
                config,
                hasHeader);

        Map<String, String> headersMap = parseHeaderBlock(context);
        DebugUtil.printMap(headersMap, System.out);

        applyHeadersToDocument(headersMap, context.getDocumentModel());

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

        validateInternal(context);

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
     * @param fileLines Contents of file
     * @return true if header exists, false if not
     */
    private boolean hasHeader(List<String> fileLines)
    {
        boolean headerSeparatorFound = false;
        boolean statusFound = false;
        boolean typeFound = false;

        for (int i = 0; i < fileLines.size() && i < MAX_HEADER_LINES; i++) {
            String line = fileLines.get(i);

            if (line.equals(configuration.getHeaderSeparator())) {
                headerSeparatorFound = true;
                break;
            }
        }

        return headerSeparatorFound;
    }


    /**
     * Process the header of the file.
     *
     * @param context the parser context
     */
    private Map<String, String> parseHeaderBlock(ParserContext context)
    {
        String headerSeparator = configuration.getHeaderSeparator();

        Map<String, String> headers = new HashMap<>();

        List<String> fileLines = context.getFileLines();

        for (int i = 0; i < fileLines.size() && i < MAX_HEADER_LINES; i++) {
            String line = fileLines.get(i);

            if (line.equals(headerSeparator)) {
                return headers;
            }

            parsePotentialHeader(line, headers);
        }
        // Only return headers if a separator line was found.
        // Otherwise, prevent returning some randomly matching file content.
        return Collections.emptyMap();
    }

    private void parsePotentialHeader(String line, Map<String, String> headersToFill) {
        if (StringUtils.isBlank(line))
            return;

        if (line.startsWith("#"))
            return;

        if (!HEADER_LINE_REGEX.matcher(line).matches())
            return;

        String[] parts = StringUtils.split(line, "=:", 2);
        if (parts.length != 2)
            return;

        String key = sanitizeKey(parts[0]);
        String value = sanitizeValue(parts[1]);

        headersToFill.put(key, value);
    }

    private static String sanitizeValue(String part) {
        return part.trim();
    }

    private static String sanitizeKey(String part) {
        String key;
        if (part.contains(UTF_8_BOM)) {
            key = part.trim().replace(UTF_8_BOM, "");
        } else {
            key = part.trim();
        }
        return key;
    }


    private void applyHeadersToDocument(Map<String, String> headers, Map<String, Object> documentModel) {

        for (Map.Entry<String, String> header : headers.entrySet()) {
            String key = header.getKey();
            String value = header.getValue();

            // Convert date to Date
            if (key.equalsIgnoreCase(Crawler.Attributes.DATE)) {
                DateFormat df = new SimpleDateFormat(configuration.getDateFormat());
                try {
                    Date date = df.parse(value);
                    documentModel.put(key, date);
                } catch (ParseException e) {
                    LOGGER.error("Unable to parse date: {}", value);
                }
            }
            // Tags
            else if (key.equalsIgnoreCase(Crawler.Attributes.TAGS)) {
                documentModel.put(key, parseTags(value));
            }
            // JSON
            else if (isJson(value)) {
                documentModel.put(key, JSONValue.parse(value));
            }
            // Ordinary String
            else {
                documentModel.put(key, value);
            }
        }
    }

    private static String[] parseTags(String tagsPart) {
        String[] tags = tagsPart.split(",");
        for (int i = 0; i < tags.length; i++)
            tags[i] = sanitizeValue(tags[i]);
        return tags;
    }

    private static boolean isJson(String part) {
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
