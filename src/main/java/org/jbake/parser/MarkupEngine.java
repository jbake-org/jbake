package org.jbake.parser;

/**
 * Base class for markup engine wrappers. A markup engine is responsible for rendering
 * markup in a source file and exporting the result into the {@link ParserContext#getContents() contents} map.
 *
 * This specific engine does nothing, meaning that the body is rendered as raw contents.
 *
 * @author Cédric Champeau
 */
public abstract class MarkupEngine {
    /**
     * Tests if this markup engine can process the document.
     * @param context the parser context
     * @return true if this markup engine has enough context to process this document. false otherwise
     */
    public boolean validate(ParserContext context) { return true; }

    /**
     * Processes the document header. Usually subclasses will parse the document body and look for
     * specific header metadata and export it into {@link ParserContext#getContents() contents} map.
     * @param context the parser context
     */
    public void processHeader(final ParserContext context) {}

    /**
     * Processes the body of the document. Usually subclasses will parse the document body and render
     * it, exporting the result using the {@link org.jbake.parser.ParserContext#setBody(String)} method.
     * @param context the parser context
     */
    public void processBody(final ParserContext context) {}
}
