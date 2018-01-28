package org.jbake.template;


import groovy.lang.GString;
import groovy.lang.Writable;
import groovy.text.SimpleTemplateEngine;
import groovy.text.Template;
import groovy.text.TemplateEngine;
import groovy.text.XmlTemplateEngine;

import org.apache.commons.configuration.CompositeConfiguration;
import org.codehaus.groovy.runtime.MethodClosure;
import org.jbake.app.ConfigUtil.Keys;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import org.jbake.app.ContentStore;

/**
 * Renders documents using a Groovy template engine. Depending on the file extension of the template, the template
 * engine will either be a {@link groovy.text.SimpleTemplateEngine}, or an {@link groovy.text.XmlTemplateEngine}
 * (.gxml).
 *
 * @author Cédric Champeau
 */
public class GroovyTemplateEngine extends AbstractTemplateEngine {

    private final Map<String, Template> cachedTemplates = new HashMap<String, Template>();

    public GroovyTemplateEngine(final CompositeConfiguration config, final ContentStore db, final File destination, final File templatesPath) {
        super(config, db, destination, templatesPath);
    }

    @Override
    public void renderDocument(final Map<String, Object> model, final String templateName, final Writer writer) throws RenderingException {
        try {
            Template template = findTemplate(templateName);
            Writable writable = template.make(wrap(model));
            writable.writeTo(writer);
        } catch (Exception e) {
            throw new RenderingException(e);
        }
    }

    private Template findTemplate(final String templateName) throws SAXException, ParserConfigurationException, ClassNotFoundException, IOException {
        TemplateEngine ste = templateName.endsWith(".gxml") ? new XmlTemplateEngine() : new SimpleTemplateEngine();
        File sourceTemplate = new File(templatesPath, templateName);
        Template template = cachedTemplates.get(templateName);
        if (template == null) {
            template = ste.createTemplate(new InputStreamReader(new BufferedInputStream(new FileInputStream(sourceTemplate)), config.getString(Keys.TEMPLATE_ENCODING)));
            cachedTemplates.put(templateName, template);
        }
        return template;
    }

    private Map<String, Object> wrap(final Map<String, Object> model) {
    	return new HashMap<String, Object>(model) {
            @Override
            public Object get(final Object property) {
                if (property instanceof String || property instanceof GString) {
                    String key = property.toString();
                    if ("include".equals(key)) {
                        return new MethodClosure(GroovyTemplateEngine.this, "doInclude").curry(this);
                    }
                	try {
                		return extractors.extractAndTransform(db, key, model, new TemplateEngineAdapter.NoopAdapter());
                	} catch(NoModelExtractorException e) {
                		// fallback to parent model
                	}
                }

                return super.get(property);
            }
        };
    }

    private void doInclude(Map<String, Object> model, String templateName) throws Exception {
        AbstractTemplateEngine engine = (AbstractTemplateEngine) model.get("renderer");
        Writer out = (Writer) model.get("out");
        engine.renderDocument(model, templateName, out);
        model.put("out", out);
    }
}
