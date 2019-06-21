package org.jbake.parser;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.parser.PegdownExtensions;
import com.vladsch.flexmark.profiles.pegdown.PegdownOptionsAdapter;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.data.DataHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Renders documents in the Markdown format.
 *
 * @author Cédric Champeau
 */
public class MarkdownEngine extends MarkupEngine {

    private static final Logger logger = LoggerFactory.getLogger(MarkdownEngine.class);

    @Override
    public void processBody(final ParserContext context) {
        List<String> mdExts = context.getConfig().getMarkdownExtensions();

        int extensions = PegdownExtensions.NONE;

        for (String ext : mdExts) {
            if (ext.startsWith("-")) {
                ext = ext.substring(1);
                extensions = removeExtension(extensions, extensionFor(ext));
            } else {
                if (ext.startsWith("+")) {
                    ext = ext.substring(1);
                }
                extensions = addExtension(extensions, extensionFor(ext));
            }
        }

        DataHolder options = PegdownOptionsAdapter.flexmarkOptions(extensions);

        Parser parser = Parser.builder(options).build();
        HtmlRenderer renderer = HtmlRenderer.builder(options).build();

        Document document = parser.parse(context.getBody());
        context.setBody(renderer.render(document));
    }

    private int extensionFor(String name) {
        int extension = PegdownExtensions.NONE;

        try {
            Field extField = PegdownExtensions.class.getDeclaredField(name);
            extension = extField.getInt(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            logger.debug("Undeclared extension field '{}', fallback to NONE", name);
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
