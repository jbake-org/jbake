package org.jbake.render;

import org.apache.commons.configuration.CompositeConfiguration;
import org.jbake.app.ContentStore;
import org.jbake.app.Crawler.Attributes;
import org.jbake.app.DocumentList;
import org.jbake.app.Renderer;
import org.jbake.app.configuration.JBakeConfiguration;
import org.jbake.model.DocumentTypes;
import org.jbake.template.RenderingException;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DocumentsRenderer implements RenderingTool {

    @Override
    public int render(Renderer renderer, ContentStore db, JBakeConfiguration config) throws RenderingException {
        int renderedCount = 0;
        final List<String> errors = new LinkedList<>();
        for (String docType : DocumentTypes.getDocumentTypes()) {
            final DocumentList documentList = db.getUnrenderedContent(docType);

            if (documentList == null) {
                continue;
            }

            int index = 0;

            Map<String, Object> nextDocumentForNav = null;

            while (index < documentList.size()) {
                try {
                    final Map<String, Object> documentToRender = documentList.get(index);
                    documentToRender.put("nextContent", null);
                    documentToRender.put("previousContent", null);

                    if (nextDocumentForNav != null) {
                        documentToRender.put("nextContent", getContentForNav(nextDocumentForNav));
                    }

                    if (index < documentList.size() - 1) {
                        Map<String, Object> prevDocumentForNav = findPrevPublishedDocument(documentList, index);
                        if (prevDocumentForNav != null) {
                            documentToRender.put("previousContent", getContentForNav(prevDocumentForNav));
                        }
                    }

                    if (isPublished(documentToRender)) {
                        nextDocumentForNav = documentToRender;
                    }

                    renderer.render(documentToRender);
                    renderedCount++;

                } catch (Exception e) {
                     errors.add(e.getMessage() != null ? e.getMessage() : e.getClass().getName());
                }

                index++;
            }

            db.markContentAsRendered(docType);
        }
        if (!errors.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("Failed to render documents. Cause(s):");
            for (String error : errors) {
                sb.append("\n").append(error);
            }
            throw new RenderingException(sb.toString());
        } else {
            return renderedCount;
        }
    }

    private Map<String, Object> findPrevPublishedDocument(DocumentList documentList, int index) {
        for ( int prevDocIndex = index+1; prevDocIndex < documentList.size(); ++prevDocIndex ) {
            Map<String, Object> prevDocument = documentList.get(prevDocIndex);
            if (isPublished(prevDocument)) {
                return prevDocument;
            }
        }
        return null;
    }

    private boolean isPublished(Map<String, Object> document) {
        // Attributes.Status.PUBLISHED_DATE cannot occur here
        // because it's converted TO either PUBLISHED or DRAFT in the Crawler.
        return Attributes.Status.PUBLISHED.equals(document.get(Attributes.STATUS));
    }

    /**
     * Creates a simple content model to use in individual post navigations.
     *
     * @param document original
     * @return navigation model for the 'document'
     */
    private Map<String, Object> getContentForNav(Map<String, Object> document) {
        Map<String, Object> navDocument = new HashMap<>();
        navDocument.put(Attributes.NO_EXTENSION_URI, document.get(Attributes.NO_EXTENSION_URI));
        navDocument.put(Attributes.URI, document.get(Attributes.URI));
        navDocument.put(Attributes.TITLE, document.get(Attributes.TITLE));
        return navDocument;
    }

    @Override
    public int render(Renderer renderer, ContentStore db, File destination, File templatesPath, CompositeConfiguration config) throws RenderingException {
        return render(renderer, db, null);
    }
}
