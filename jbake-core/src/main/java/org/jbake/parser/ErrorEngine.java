package org.jbake.parser;

import org.jbake.app.Crawler.Attributes;

import java.util.Date;
import java.util.Map;

/**
 * An internal rendering engine used to notify the user that the markup format he used requires an engine that couldn't
 * be loaded.
 *
 * @author Cédric Champeau
 */
public class ErrorEngine extends MarkupEngine {
    private final String engineName;

    public ErrorEngine() {
        this("unknown");
    }

    public ErrorEngine(final String name) {
        engineName = name;
    }

    @Override
    public Map<String, String> parseHeaderBlock(final ParserContext context) {
        Map<String, Object> contents = context.getDocumentModel();
        contents.put(Attributes.TYPE, "post");
        contents.put(Attributes.STATUS, "published");
        contents.put(Attributes.TITLE, "Rendering engine missing");
        contents.put(Attributes.DATE, new Date());
        contents.put(Attributes.TAGS, new String[0]);
        contents.put(Attributes.ID, context.getFile().getName());
        return null; // TODO: Create the header map first, then apply to the doc.
    }

    @Override
    public void processBody(final ParserContext context) {
        context.setBody("The markup engine [" + engineName + "] for [" + context.getFile() + "] couldn't be loaded");
    }
}
