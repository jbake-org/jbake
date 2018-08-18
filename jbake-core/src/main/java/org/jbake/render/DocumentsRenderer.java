package org.jbake.render;

import org.apache.commons.configuration.CompositeConfiguration;
import org.jbake.app.ContentStore;
import org.jbake.app.DocumentList;
import org.jbake.app.Renderer;
import org.jbake.app.configuration.JBakeConfiguration;
import org.jbake.model.DocumentModel;
import org.jbake.model.DocumentTypes;
import org.jbake.template.RenderingException;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class DocumentsRenderer implements RenderingTool {

    @Override
    public int render(Renderer renderer, ContentStore db, JBakeConfiguration config) throws RenderingException {
        int renderedCount = 0;
        final List<String> errors = new LinkedList<>();
        for (String docType : DocumentTypes.getDocumentTypes()) {
            DocumentList documentList = db.getUnrenderedContent(docType);

            if (documentList == null) {
                continue;
            }

            int index = 0;

            DocumentModel nextDocument = null;

            while (index < documentList.size()) {
                try {
                    DocumentModel document = documentList.getDocumentModel(index);
                    document.setNextContent(null);
                    document.setPreviousContent(null);

                    if (nextDocument != null && index > 0) {
                        document.setNextContent(getContentForNav(nextDocument));
                    }

                    if (index < documentList.size() - 1) {
                        DocumentModel tempNext = (DocumentModel) documentList.get(index + 1);
                        document.setPreviousContent(getContentForNav(tempNext));
                    }

                    nextDocument = document;

                    renderer.render(document);
                    renderedCount++;

                } catch (Exception e) {
                    errors.add(e.getMessage());
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

    /**
     * Creates a simple content model to use in individual post navigations.
     *
     * @param document
     * @return
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
