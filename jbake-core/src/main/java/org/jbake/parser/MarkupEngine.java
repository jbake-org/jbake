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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.IOUtils;
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

    /**
     * Processes the summary of the document. Usually subclasses will parse the document body and render
     * it, exporting the result using the {@link org.jbake.parser.ParserContext#setSummary(String)} method.
     *
     * @param context the parser context
     */
    public void processSummary(final ParserContext context, int maxLength) {
        Document document = Jsoup.parse(context.getBody());
        StringBuilder builder = new StringBuilder();
        int count = 0;
        for (Element element : document.body().children()) {
            builder.append(element.outerHtml());
            builder.append("\n");
            count += element.text().length();
            if (count >= maxLength)
                break;
        }

        context.setSummary(builder.toString());
    }

    /**
     * Parse given file to extract as much infos as possible
     *
     * @param file file to process
     * @return a map containing all infos. Returning null indicates an error, even if an exception would be better.
     */
    public Map<String, Object> parse(Configuration config, File file, String contentPath) {

        Map<String, Object> content = new HashMap<String, Object>();
        List<String> fileContents = null;
        try (InputStream is = new FileInputStream(file)) {

            fileContents = IOUtils.readLines(is, config.getString(Keys.RENDER_ENCODING));
        } catch (IOException e) {
            LOGGER.error("Error while opening file {}", file, e);

            return null;
        }

        boolean hasHeader = hasHeader(config, fileContents);
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

        if (config.getString(Keys.DEFAULT_TYPE) != null) {
            // default type has been set
            if (content.get(Crawler.Attributes.TYPE) == null) {
                // file hasn't got type so use default
                content.put(Crawler.Attributes.TYPE, config.getString(Keys.DEFAULT_TYPE));
            }
        }

        if (content.get(Crawler.Attributes.TYPE) == null || content.get(Crawler.Attributes.STATUS) == null) {
            // output error
            LOGGER.warn("Parsing skipped (missing type or status value in header meta data) for file {}!", file);
            return null;
        }

        // generate default body
        processBody(config, fileContents, content);

        // eventually process body using specific engine
        if (validate(context)) {
            processBody(context);
            processSummary(context, config.getInt(Keys.POST_SUMMARY_LENGTH, 500));
        } else {
            LOGGER.error("Incomplete source file ({}) for markup engine: {}", file, getClass().getSimpleName());
            return null;
        }

        if (content.get(Crawler.Attributes.TAGS) != null) {
            String[] tags = (String[]) content.get(Crawler.Attributes.TAGS);
            for (int i = 0; i < tags.length; i++) {
                tags[i] = tags[i].trim();
                if (config.getBoolean(Keys.TAG_SANITIZE)) {
                    tags[i] = tags[i].replace(" ", "-");
                }
            }
            content.put(Crawler.Attributes.TAGS, tags);
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
    private boolean hasHeader(Configuration config, List<String> contents) {
        boolean headerValid = false;
        boolean headerSeparatorFound = false;
        boolean statusFound = false;
        boolean typeFound = false;

        List<String> header = new ArrayList<String>();

        for (String line : contents) {
            if (!line.isEmpty()) {
                header.add(line);
            }
            if (line.contains("=")) {
                if (line.startsWith("type=")) {
                    typeFound = true;
                }
                if (line.startsWith("status=")) {
                    statusFound = true;
                }
            }
            if (line.equals(config.getString(Keys.HEADER_SEPARATOR))) {
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

        return headerValid && statusFound && typeFound;
    }

    /**
     * Process the header of the file.
     *
     * @param config
     * @param contents Contents of file
     * @param content
     */
    private void processHeader(Configuration config, List<String> contents, final Map<String, Object> content) {
        for (String line : contents) {
            if (line.equals(config.getString(Keys.HEADER_SEPARATOR))) {
                break;
            }

            if (line.isEmpty()) {
                continue;
            }

            String[] parts = line.split("=", 2);
            if (parts.length != 2) {
                continue;
            }

            String utf8BOM = "\uFEFF";
            String key;
            if (parts[0].contains(utf8BOM)) {
                key = parts[0].trim().replace(utf8BOM, "");
            } else {
                key = parts[0].trim();
            }
            String value = parts[1].trim();

            if (key.equalsIgnoreCase(Crawler.Attributes.DATE)) {
                DateFormat df = new SimpleDateFormat(config.getString(Keys.DATE_FORMAT));
                Date date = null;
                try {
                    date = df.parse(value);
                    content.put(key, date);
                } catch (ParseException e) {
                    e.printStackTrace();
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

    private String[] getTags(String tagsPart) {
        String[] tags = tagsPart.split(",");
        for (int i = 0; i < tags.length; i++)
            tags[i] = tags[i].trim();
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
    private void processBody(Configuration config, List<String> contents, final Map<String, Object> content) {
        StringBuilder body = new StringBuilder();
        boolean inBody = false;
        for (String line : contents) {
            if (inBody) {
                body.append(line).append("\n");
            }
            if (line.equals(config.getString(Keys.HEADER_SEPARATOR))) {
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
