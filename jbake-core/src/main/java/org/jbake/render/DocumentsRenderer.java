package org.jbake.render;

import org.apache.commons.configuration2.CompositeConfiguration;
import org.jbake.app.ContentStore;
import org.jbake.app.DocumentList;
import org.jbake.app.Renderer;
import org.jbake.app.configuration.JBakeConfiguration;
import org.jbake.exception.RenderingException;
import org.jbake.model.DocumentModel;
import org.jbake.model.ModelAttributes;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class DocumentsRenderer implements RenderingTool {

    @Override
    public int render(Renderer renderer, ContentStore db, JBakeConfiguration config) throws RenderingException {
        int renderedCount = 0;
        int index = 0;
        final List<String> errors = new LinkedList<>();
        DocumentModel nextDocument = null;

        DocumentList<DocumentModel> documentList = db.getUnrenderedContent();
        for (DocumentModel document : documentList) {
            try {
                document.setNextContent(null);
                document.setPreviousContent(null);

                if (nextDocument != null && index > 0) {
                    document.setNextContent(getContentForNav(nextDocument));
                }

                if (index < documentList.size() - 1) {
                    DocumentModel tempNext = findPrevPublishedDocument(documentList, index);
                    if (tempNext != null) {
                        document.setPreviousContent(getContentForNav(tempNext));
                    }
                }

                if (isPublished(document)) {
                    nextDocument = document;
                }

                renderer.render(document);
                db.markContentAsRendered(document);
                renderedCount++;

            } catch (Exception e) {
                errors.add(e.getMessage());
            }

            index++;
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

    private DocumentModel findPrevPublishedDocument(DocumentList<DocumentModel> documentList, int index) {
        for ( int prevDocIndex = index+1; prevDocIndex < documentList.size(); ++prevDocIndex ) {
            DocumentModel prevDocument = documentList.get(prevDocIndex);
            if (isPublished(prevDocument)) {
                return prevDocument;
            }
        }
        return null;
    }

    private boolean isPublished(DocumentModel document) {
        // Attributes.Status.PUBLISHED_DATE cannot occur here
        // because it's converted TO either PUBLISHED or DRAFT in the Crawler.
        return ModelAttributes.Status.PUBLISHED.equals(document.getStatus());
    }

    /**
     * Creates a simple content model to use in individual post navigations.
     *
     * @param document original
     * @return navigation model for the 'document'
     */
    private DocumentModel getContentForNav(DocumentModel document) {
        DocumentModel navDocument = new DocumentModel();
        navDocument.setNoExtensionUri(document.getNoExtensionUri());
        navDocument.setUri(document.getUri());
        navDocument.setTitle(document.getTitle());
        return navDocument;
    }

    @Override
    public int render(Renderer renderer, ContentStore db, File destination, File templatesPath, CompositeConfiguration config) throws RenderingException {
        return render(renderer, db, null);
    }
}
