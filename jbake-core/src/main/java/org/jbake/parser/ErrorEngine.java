package org.jbake.parser;

import org.jbake.model.DocumentModel;

import java.time.Instant;

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
        DocumentModel documentModel = context.getDocumentModel();
        documentModel.setType("post");
        documentModel.setStatus("published");
        documentModel.setTitle("Rendering engine missing");
        documentModel.setDate(Instant.now());
        documentModel.setTags(new String[0]);
    }

    @Override
    public void processBody(final ParserContext context) {
        context.setBody("The markup engine [" + engineName + "] for [" + context.getFile() + "] couldn't be loaded");
    }
}
