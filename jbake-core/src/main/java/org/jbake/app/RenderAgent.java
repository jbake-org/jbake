package org.jbake.app;

import org.jbake.app.configuration.JBakeConfiguration;
import org.jbake.template.DelegatingTemplateEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

public abstract class RenderAgent implements Runnable {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    protected JBakeConfiguration config;
    protected DelegatingTemplateEngine renderingEngine;
    protected Renderer renderer;

    public RenderAgent(JBakeConfiguration config, DelegatingTemplateEngine renderingEngine, Renderer renderer) {
        this.config = config;
        this.renderingEngine = renderingEngine;
        this.renderer = renderer;
    }

    @Override
    public void run() {
        try {
            logger.info("Start render document. Current agent count: {} completed: {} tasks: {}", renderer.getActiveAgentCount(), renderer.getCompletedAgentCount(), renderer.getTaskCount());
            renderDocument();
        } catch (Exception e) {
            renderer.addError(e);
        }
    }

    protected abstract void renderDocument() throws Exception;

    protected Writer createWriter(File file) throws IOException {
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }

        return new OutputStreamWriter(new FileOutputStream(file), config.getRenderEncoding());
    }

    protected String findTemplateName(String docType) {
        return config.getTemplateFileByDocType(docType).getName();
    }
}
