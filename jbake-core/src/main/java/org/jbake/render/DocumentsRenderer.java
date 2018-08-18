package org.jbake.render;

import org.apache.commons.configuration.CompositeConfiguration;
import org.jbake.app.ContentStore;
import org.jbake.app.DocumentList;
import org.jbake.app.Renderer;
import org.jbake.app.configuration.JBakeConfiguration;
import org.jbake.model.DocumentAttributes;
import org.jbake.model.DocumentModel;
import org.jbake.model.DocumentTypes;
import org.jbake.template.RenderingException;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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

            Map<String, Object> nextDocument = null;

            while (index < documentList.size()) {
                try {
                    Map<String, Object> document = documentList.get(index);
                    document.put("nextContent", null);
                    document.put("previousContent", null);

                    if (index > 0) {
                        document.put("nextContent", getContentForNav(nextDocument));
                    }

                    if (index < documentList.size() - 1) {
                        Map<String, Object> tempNext = documentList.get(index + 1);
                        document.put("previousContent", getContentForNav(tempNext));
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
    private DocumentModel getContentForNav(Map<String, Object> document) {
        DocumentModel navDocument = new DocumentModel();
        navDocument.setNoExtensionUri((String) document.get(DocumentAttributes.NO_EXTENSION_URI.toString()));
        navDocument.setUri((String) document.get(DocumentAttributes.URI.toString()));
        navDocument.setTitle((String) document.get(DocumentAttributes.TITLE.toString()));
        return navDocument;
    }

    @Override
    public int render(Renderer renderer, ContentStore db, File destination, File templatesPath, CompositeConfiguration config) throws RenderingException {
        return render(renderer, db, null);
    }
}
