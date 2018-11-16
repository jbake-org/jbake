package org.jbake.parser;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.apache.commons.lang.StringUtils;
import org.jbake.app.Crawler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HTML markup engine. Processes .html files.
 * TODO: Rename to HtmlMarkupEngine
 */
public class RawMarkupEngine extends MarkupEngine {

    private static final Logger LOG = LoggerFactory.getLogger(RawMarkupEngine.class);

    /**
     * Headers used as automatically detected titles will be marked with this class.
     */
    public static final String CSS_CLASS_EXTRACTED_TITLE = "jbakeExtractedTitle";

    @Override
    public void processBody(ParserContext context)
    {
        super.processBody(context);

        boolean extractTitle = context.getConfig().getExtractTitleFromDoc();

        Object currentTitle = context.getDocumentModel().get(Crawler.Attributes.TITLE);
        extractTitle &= null == currentTitle || StringUtils.isBlank("" + currentTitle);


        // Keeping everything off by default for backward compatibility.
        boolean normalizeHtml = context.getConfig().getNormalizeHtml(); // Default false
        boolean convertToXhtml = context.getConfig().getConvertHtmlToXhtml();
        boolean prettyPrint = context.getConfig().getPrettyPrintHtml();

        // The input charset is already handled by MarkupEngine when it reads the file.
        Charset inputCharset = StandardCharsets.UTF_8; // context.getConfig().getInputCharset();
        Charset outputCharset = context.getConfig().getOutputHtmlCharset(); // Default: UTF-8


        if (extractTitle || normalizeHtml) {

            Document doc;
            if (inputCharset == null)
                doc = Jsoup.parse(context.getBody());
            else try {
                doc = Jsoup.parse(new ByteArrayInputStream(context.getBody().getBytes()), inputCharset.name(), "");
            } catch (Exception ex) {
                LOG.warn("Couldn't read a string?");
                doc = Jsoup.parse(context.getBody());
            }

            boolean domNeedsToBeSaved = false;

            findTitle:
            if (extractTitle) {

                if (!StringUtils.isBlank(doc.title())) {
                    context.getDocumentModel().put(Crawler.Attributes.TITLE, doc.title());
                    break findTitle;
                }

                Elements headings = doc.select("h1, h2, h3, h4, h5, h6");
                if (headings.size() > 0) {
                    context.getDocumentModel().put(Crawler.Attributes.TITLE, headings.first().text());
                    headings.first().addClass(CSS_CLASS_EXTRACTED_TITLE);
                    domNeedsToBeSaved = true;
                    break findTitle;
                }
            }

            if (doc.body() != null || normalizeHtml || convertToXhtml || domNeedsToBeSaved) {
                Document.OutputSettings outputSettings = doc.outputSettings();

                outputSettings.prettyPrint(prettyPrint);
                if (outputCharset != null)
                    outputSettings.charset(outputCharset);

                if (convertToXhtml) {
                    outputSettings.escapeMode(Entities.EscapeMode.xhtml);
                    outputSettings.syntax(Document.OutputSettings.Syntax.xml);
                }
                else {
                    outputSettings.escapeMode(Entities.EscapeMode.extended);
                    outputSettings.syntax(Document.OutputSettings.Syntax.html);
                }

                doc.outputSettings(outputSettings);

                Element elementToExport = doc.body();
                if (null == elementToExport)
                    elementToExport = doc;
                context.getDocumentModel().put(Crawler.Attributes.BODY, elementToExport.html());
                context.getDocumentModel().put("charset", outputSettings.charset().name());
            }
        }
    }



    /**
     * TODO: This should be in some ConfigUtils and happen when reading the config.
     *
    private boolean getFlag(ParserContext context, String configKey, boolean defaultValue) {
        try {
            return context.getConfig().getBoolean(configKey, defaultValue);
        }
        catch (Exception ex) {
             LOG.warn("Invalid configuration value for '{}', should be 'true' or 'false': {}", configKey,  ... );
            return defaultValue;
        }
    }/**/
}
