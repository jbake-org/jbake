package org.jbake.parser;

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
    public void processHeader(final ParserContext context) {
        Map<String, Object> contents = context.getContents();
        contents.put("type", "post");
        contents.put("status", "published");
        contents.put("title", "Rendering engine missing");
        contents.put("date", new Date());
        contents.put("tags", new String[0]);
        contents.put("id", context.getFile().getName());
    }

    @Override
    public void processBody(final ParserContext context) {
        context.setBody("The markup engine [" + engineName + "] for [" + context.getFile() + "] couldn't be loaded");
    }
}
