package org.jbake.parser.texy;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.jbake.app.Crawler;
import org.jbake.parser.MarkupEngine;
import org.jbake.parser.ParserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Renders documents in the Texy syntax.
 *
 * @author Ondřej Žižka
 */
public class TexyServiceEngine extends MarkupEngine
{

    private static final Logger LOG = LoggerFactory.getLogger(TexyServiceEngine.class);


    @Override
    public Map<String, String> parseHeaderBlock(ParserContext context)
    {
        return super.parseHeaderBlock(context);
    }

    @Override
    public void processBody(final ParserContext context) {
        String documentBody = context.getBody();

        try (InputStream stream = new ByteArrayInputStream(documentBody.getBytes(StandardCharsets.UTF_8))){
            TexyRestService texyService = new TexyRestService(new URL("http://localhost:8022/TexyService.rest.php"));

            String xhtmlString = null;
            try (InputStream xhtmlIS = texyService.convertTexyToXhtml(stream)){
                java.util.Scanner s = new java.util.Scanner(xhtmlIS, StandardCharsets.UTF_8.name()).useDelimiter("\\A");
                xhtmlString = s.hasNext() ? s.next() : "";
                context.setBody(xhtmlString);

                if (!context.getDocumentModel().containsKey(Crawler.Attributes.TITLE)) {
                    try {
                        new TitleExtractor().tryExtractHighestHeader(context);
                    }
                    catch (Exception ex){
                        LOG.warn("Could not extract title from '{}': {}\nConverted XHTML: \n{}", context.getFile().getName(), ex.getMessage(), xhtmlString);
                    }
                    if (StringUtils.isBlank((String) context.getDocumentModel().get(Crawler.Attributes.TITLE)))
                        context.getDocumentModel().put(Crawler.Attributes.TITLE, context.getFile().getName());
                }
            }
            catch (IOException ex) {
                String msg = "Couldn't convert:'" + context.getFile().getPath() + "': " + ex.getMessage();
                throw new RuntimeException(msg, ex);
                // TOOO: I am not sure how to handle errors in JBake. The exception stops the whole process.
                //LOG.warn(msg, ex);
                //LOG.debug("Document's XHTML: \n" + xhtmlString);
            }
        }
        catch (IOException ex) {
            throw new RuntimeException("Failed opening String stream: " + ex.getMessage(), ex);
        }
    }

}
