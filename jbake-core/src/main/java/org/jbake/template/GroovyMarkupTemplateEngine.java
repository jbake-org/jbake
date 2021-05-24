package org.jbake.template;

import groovy.lang.Writable;
import groovy.text.Template;
import groovy.text.markup.MarkupTemplateEngine;
import groovy.text.markup.TemplateConfiguration;
import org.apache.commons.configuration2.CompositeConfiguration;
import org.jbake.app.ContentStore;
import org.jbake.app.configuration.JBakeConfiguration;
import org.jbake.template.model.TemplateModel;

import java.io.File;
import java.io.Writer;
import java.util.Map;

/**
 * Renders documents using the GroovyMarkupTemplateEngine.
 * <p>
 * The file extension to activate this Engine is .tpl
 *
 * @see <a href="http://groovy-lang.org/templating.html#_the_markuptemplateengine">Groovy MarkupTemplateEngine Documentation</a>
 */
public class GroovyMarkupTemplateEngine extends AbstractTemplateEngine {
    private TemplateConfiguration templateConfiguration;
    private MarkupTemplateEngine templateEngine;

    /**
     * @deprecated Use {@link #GroovyMarkupTemplateEngine(JBakeConfiguration, ContentStore)} instead
     *
     * @param config the {@link CompositeConfiguration} of jbake
     * @param db the {@link ContentStore}
     * @param destination the destination path
     * @param templatesPath the templates path
     */
    @Deprecated
    public GroovyMarkupTemplateEngine(final CompositeConfiguration config, final ContentStore db, final File destination, final File templatesPath) {
        super(config, db, destination, templatesPath);
        setupTemplateConfiguration();
        initializeTemplateEngine();
    }

    public GroovyMarkupTemplateEngine(final JBakeConfiguration config, final ContentStore db) {
        super(config, db);
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
        templateEngine = new MarkupTemplateEngine(MarkupTemplateEngine.class.getClassLoader(), config.getTemplateFolder(), templateConfiguration);
    }

    @Override
    public void renderDocument(final TemplateModel model, final String templateName, final Writer writer) throws RenderingException {
        try {
            Template template = templateEngine.createTemplateByPath(templateName);
            Map<String, Object> wrappedModel = wrap(model);
            Writable writable = template.make(wrappedModel);
            writable.writeTo(writer);
        } catch (Exception e) {
            throw new RenderingException(e);
        }
    }

    private TemplateModel wrap(final TemplateModel model) {
        return new TemplateModel(model) {
            @Override
            public Object get(Object key) {
                try {
                    return extractors.extractAndTransform(db, (String) key, model, new TemplateEngineAdapter.NoopAdapter());
                } catch (NoModelExtractorException e) {
                    return super.get(key);
                }
            }
        };
    }
}
