package org.jbake.template;

import org.jbake.exception.RenderingException;
import org.jbake.model.TemplateModel;

import java.io.Writer;

public interface TemplateEngine {
    void renderDocument(TemplateModel model, String templateName, Writer writer) throws RenderingException;
}
