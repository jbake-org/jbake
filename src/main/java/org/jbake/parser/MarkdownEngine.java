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
                String ext = mdExts[index];
                if (ext.startsWith("-")) {
		    ext = ext.substring(1);
                    extensions=removeExtension(extensions, extensionFor(ext));
                } else {
                    if (ext.startsWith("+")) {
		      ext = ext.substring(1);
                    }
                    extensions=addExtension(extensions, extensionFor(ext));
                }
            }
        }
        
        long maxParsingTime = context.getConfig().getLong("markdown.maxParsingTimeInMillis", PegDownProcessor.DEFAULT_MAX_PARSING_TIME);
        
        PegDownProcessor pegdownProcessor = new PegDownProcessor(extensions, maxParsingTime);
        context.setBody(pegdownProcessor.markdownToHtml(context.getBody()));
    }

    private int extensionFor(String name) {
        int extension = Extensions.NONE;
		if (name.equals("HARDWRAPS")) {
			extension = Extensions.HARDWRAPS;
        } else if (name.equals("AUTOLINKS")) {
            extension = Extensions.AUTOLINKS;
        } else if (name.equals("FENCED_CODE_BLOCKS")) {
            extension = Extensions.FENCED_CODE_BLOCKS;
        } else if (name.equals("DEFINITIONS")) {
            extension = Extensions.DEFINITIONS;
        } else if (name.equals("ABBREVIATIONS")) {
            extension = Extensions.ABBREVIATIONS;
        } else if (name.equals("QUOTES")) {
            extension = Extensions.QUOTES;
        } else if (name.equals("SMARTS")) {
            extension = Extensions.SMARTS;
        } else if (name.equals("SMARTYPANTS")) {
            extension = Extensions.SMARTYPANTS;
        } else if (name.equals("SUPPRESS_ALL_HTML")) {
            extension = Extensions.SUPPRESS_ALL_HTML;
        } else if (name.equals("SUPPRESS_HTML_BLOCKS")) {
            extension = Extensions.SUPPRESS_HTML_BLOCKS;
        } else if (name.equals("SUPPRESS_INLINE_HTML")) {
            extension = Extensions.SUPPRESS_INLINE_HTML;
        } else if (name.equals("TABLES")) {
            extension = Extensions.TABLES;
        } else if (name.equals("WIKILINKS")) {
            extension = Extensions.WIKILINKS;
        } else if (name.equals("ANCHORLINKS")) {
            extension = Extensions.ANCHORLINKS;
        } else if (name.equals("STRIKETHROUGH")) {
            extension = Extensions.STRIKETHROUGH;
        }else if (name.equals("ATXHEADERSPACE")) {
            extension = Extensions.ATXHEADERSPACE;
        }else if (name.equals("FORCELISTITEMPARA")) {
            extension = Extensions.FORCELISTITEMPARA;
        }else if (name.equals("RELAXEDHRULES")) {
            extension = Extensions.RELAXEDHRULES;
        }else if (name.equals("TASKLISTITEMS")) {
            extension = Extensions.TASKLISTITEMS;
        }else if (name.equals("EXTANCHORLINKS")) {
            extension = Extensions.EXTANCHORLINKS;
        } else if (name.equals("ALL")) {
            extension = Extensions.ALL;
        } else if (name.equals("ALL_OPTIONALS")) {
            extension = Extensions.ALL_OPTIONALS;
        } else if (name.equals("ALL_WITH_OPTIONALS")) {
            extension = Extensions.ALL_WITH_OPTIONALS;
        }
        return extension;
    }
    private int addExtension(int previousExtensions, int additionalExtension) {
    	return previousExtensions | additionalExtension;
    }
    private int removeExtension(int previousExtensions, int unwantedExtension) {
    	return previousExtensions & (~unwantedExtension);
    }

}
