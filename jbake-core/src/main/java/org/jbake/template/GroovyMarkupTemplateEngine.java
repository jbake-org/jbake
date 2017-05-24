package org.jbake.template;

import groovy.lang.GString;
import groovy.lang.Writable;
import groovy.text.Template;
import groovy.text.markup.MarkupTemplateEngine;
import groovy.text.markup.TemplateConfiguration;
import org.apache.commons.configuration.CompositeConfiguration;
import org.jbake.app.ContentStore;

import java.io.File;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

/**
 * Renders documents using the GroovyMarkupTemplateEngine.
 *
 * The file extension to activate this Engine is .tpl
 *
 * @see <a href="http://groovy-lang.org/templating.html#_the_markuptemplateengine">Groovy MarkupTemplateEngine Documentation</a>
 */
public class GroovyMarkupTemplateEngine extends AbstractTemplateEngine {
    private TemplateConfiguration templateConfiguration;
    private MarkupTemplateEngine templateEngine;

    public GroovyMarkupTemplateEngine(final CompositeConfiguration config, final ContentStore db, final File destination, final File templatesPath) {
        super(config, db, destination, templatesPath);
        setupTemplateConfiguration();
        initializeTemplateEngine();
    }

    private void setupTemplateConfiguration() {
        templateConfiguration = new TemplateConfiguration();
        templateConfiguration.setUseDoubleQuotes(true);
        templateConfiguration.setAutoIndent(true);
        templateConfiguration.setAutoNewLine(true);
        templateConfiguration.setAutoEscape(true);
    }

    private void initializeTemplateEngine() {
        templateEngine = new MarkupTemplateEngine(MarkupTemplateEngine.class.getClassLoader(),templatesPath,templateConfiguration);
    }

    @Override
    public void renderDocument(final Map<String, Object> model, final String templateName, final Writer writer) throws RenderingException {
        try {
            Template template = templateEngine.createTemplateByPath(templateName);
            Map<String, Object> wrappedModel = wrap(model);
            Writable writable = template.make(wrappedModel);
            writable.writeTo(writer);
        } catch (Exception e) {
            throw new RenderingException(e);
        }
    }

    private Map<String, Object> wrap(final Map<String, Object> model) {
        return new HashMap<String, Object>(model) {
            @Override
            public Object get(final Object property) {
            	if (property instanceof String || property instanceof GString) {
            		String key = property.toString();
					try {
						put(key, extractors.extractAndTransform(db, key, model, new TemplateEngineAdapter.NoopAdapter()));
					} catch (NoModelExtractorException e) {
						// should never happen, as we iterate over existing extractors
					}
            	}

                return super.get(property);
            }
        };
    }
}
