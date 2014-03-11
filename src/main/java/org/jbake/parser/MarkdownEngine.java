package org.jbake.parser;

import org.pegdown.Extensions;
import org.pegdown.PegDownProcessor;

/**
 * Renders documents in the Markdown format.
 *
 * @author Cédric Champeau
 */
public class MarkdownEngine extends MarkupEngine {

    public MarkdownEngine() {
        Class engineClass = PegDownProcessor.class;
        assert engineClass!=null;
    }

    @Override
    public void processBody(final ParserContext context) {
        String[] mdExts = context.getConfig().getStringArray("markdown.extensions");

        int extensions = Extensions.NONE;
        if (mdExts.length > 0) {
            for (int index = 0; index < mdExts.length; index++) {
                if (mdExts[index].equals("HARDWRAPS")) {
                    extensions |= Extensions.HARDWRAPS;
                } else if (mdExts[index].equals("AUTOLINKS")) {
                    extensions |= Extensions.AUTOLINKS;
                } else if (mdExts[index].equals("FENCED_CODE_BLOCKS")) {
                    extensions |= Extensions.FENCED_CODE_BLOCKS;
                } else if (mdExts[index].equals("DEFINITIONS")) {
                    extensions |= Extensions.DEFINITIONS;
                } else if (mdExts[index].equals("ABBREVIATIONS")) {
                    extensions |= Extensions.ABBREVIATIONS;
                } else if (mdExts[index].equals("QUOTES")) {
                    extensions |= Extensions.QUOTES;
                } else if (mdExts[index].equals("SMARTS")) {
                    extensions |= Extensions.SMARTS;
                } else if (mdExts[index].equals("SMARTYPANTS")) {
                    extensions |= Extensions.SMARTYPANTS;
                } else if (mdExts[index].equals("SUPPRESS_ALL_HTML")) {
                    extensions |= Extensions.SUPPRESS_ALL_HTML;
                } else if (mdExts[index].equals("SUPPRESS_HTML_BLOCKS")) {
                    extensions |= Extensions.SUPPRESS_HTML_BLOCKS;
                } else if (mdExts[index].equals("SUPPRESS_INLINE_HTML")) {
                    extensions |= Extensions.SUPPRESS_INLINE_HTML;
                } else if (mdExts[index].equals("TABLES")) {
                    extensions |= Extensions.TABLES;
                } else if (mdExts[index].equals("WIKILINKS")) {
                    extensions |= Extensions.WIKILINKS;
                } else if (mdExts[index].equals("ALL")) {
                    extensions = Extensions.ALL;
                }
            }

        }
        PegDownProcessor pegdownProcessor = new PegDownProcessor(extensions);
        context.setBody(pegdownProcessor.markdownToHtml(context.getBody()));
    }
}
