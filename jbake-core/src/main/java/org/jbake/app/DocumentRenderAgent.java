package org.jbake.app;

import org.jbake.app.configuration.JBakeConfiguration;
import org.jbake.model.DocumentModel;
import org.jbake.model.ModelAttributes;
import org.jbake.template.DelegatingTemplateEngine;
import org.jbake.template.model.TemplateModel;

import java.io.File;
import java.io.Writer;
import java.nio.file.Files;

public class DocumentRenderAgent extends RenderAgent {

    private final DocumentModel document;

    public DocumentRenderAgent(JBakeConfiguration config, DocumentModel document, DelegatingTemplateEngine renderingEngine, Renderer renderer) {
        super(config,renderingEngine,renderer);
        this.document = document;
    }

    public void renderDocument() throws Exception {
        long start = System.currentTimeMillis();
        String docType = document.getType();
        String outputFilename = config.getDestinationFolder().getPath() + File.separatorChar + document.getUri();
        if (outputFilename.lastIndexOf('.') > outputFilename.lastIndexOf(File.separatorChar)) {
            outputFilename = outputFilename.substring(0, outputFilename.lastIndexOf('.'));
        }

        // delete existing versions if they exist in case status has changed either way
        String outputExtension = config.getOutputExtensionByDocType(docType);
        File draftFile = new File(outputFilename, config.getDraftSuffix() + outputExtension);
        if (draftFile.exists()) {
            Files.delete(draftFile.toPath());
        }

        File publishedFile = new File(outputFilename + outputExtension);
        if (publishedFile.exists()) {
            Files.delete(publishedFile.toPath());
        }

        if (document.getStatus().equals(ModelAttributes.Status.DRAFT)) {
            outputFilename = outputFilename + config.getDraftSuffix();
        }

        File outputFile = new File(outputFilename + outputExtension);
        TemplateModel model = new TemplateModel();
        model.setContent(document);
        model.setRenderer(renderingEngine);

        try {
            try (Writer out = createWriter(outputFile)) {
                renderingEngine.renderDocument(model, findTemplateName(docType), out);
                renderer.incrementCount();
                long end = System.currentTimeMillis();
                long delta = end - start;
                logger.info("Rendering [{}]... done! ({} ms)", outputFile, delta);
            }
        } catch (Exception e) {
            long end = System.currentTimeMillis();
            long delta = end - start;
            logger.error("Rendering [{}]... failed! ({} ms)", outputFile, delta, e);
            throw new Exception("Failed to render file " + outputFile.getAbsolutePath() + ". Cause: " + e.getMessage(), e);
        }
    }
}
