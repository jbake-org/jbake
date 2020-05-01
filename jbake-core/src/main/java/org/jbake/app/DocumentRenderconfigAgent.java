package org.jbake.app;

import org.jbake.app.Renderer.RenderingConfig;
import org.jbake.app.configuration.JBakeConfiguration;
import org.jbake.template.DelegatingTemplateEngine;

import java.io.File;
import java.io.Writer;

public class DocumentRenderconfigAgent extends RenderAgent {

    private final RenderingConfig renderingConfig;

    public DocumentRenderconfigAgent(JBakeConfiguration config, RenderingConfig renderingConfig, DelegatingTemplateEngine renderingEngine, Renderer renderer) {
        super(config, renderingEngine, renderer);
        this.renderingConfig = renderingConfig;
    }

    @Override
    protected void renderDocument() throws Exception {
        File outputFile = renderingConfig.getPath();
        try {
            try (Writer out = createWriter(outputFile)) {
                renderingEngine.renderDocument(renderingConfig.getModel(), renderingConfig.getTemplate(), out);
                renderer.incrementCount();
            }
            logger.info("Rendering {} [{}]... done!", renderingConfig.getName(), outputFile);
        } catch (Exception e) {
            logger.error("Rendering {} [{}]... failed!", renderingConfig.getName(), outputFile, e);
            throw new Exception("Failed to render " + renderingConfig.getName(), e);
        }
    }

}
