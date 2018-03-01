package org.jbake.parser;

import java.util.Date;
import java.util.Map;

import org.jbake.app.Crawler.Attributes;

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
    public void processHeader(final ParserContext context) {
        Map<String, Object> contents = context.getContents();
        contents.put(Attributes.TYPE, "post");
        contents.put(Attributes.STATUS, "published");
        contents.put(Attributes.TITLE, "Rendering engine missing");
        contents.put(Attributes.DATE, new Date());
        contents.put(Attributes.TAGS, new String[0]);
        contents.put(Attributes.ID, context.getFile().getName());
    }

    @Override
    public void processBody(final ParserContext context) {
        context.setBody("The markup engine [" + engineName + "] for [" + context.getFile() + "] couldn't be loaded");
    }
}
