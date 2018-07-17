package org.jbake.template;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.lang.LocaleUtils;
import org.jbake.app.ContentStore;
import org.jbake.app.Crawler.Attributes;
import org.jbake.app.configuration.JBakeConfiguration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.context.LazyContextVariable;
import org.thymeleaf.templateresolver.FileTemplateResolver;

import java.io.File;
import java.io.Writer;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

/**
 * <p>A template engine which renders pages using Thymeleaf.</p>
 *
 * <p>This template engine is not recommended for large sites because the whole model
 * is loaded into memory due to Thymeleaf internal limitations.</p>
 *
 * <p>The default rendering mode is "HTML", but it is possible to use another mode
 * for each document type, by adding a key in the configuration, for example:</p>
 *
 * <code>
 * template.feed.thymeleaf.mode=XML
 * </code>
 *
 * @author Cédric Champeau
 */
public class ThymeleafTemplateEngine extends AbstractTemplateEngine {
    private static final String DEFAULT_TEMPLATE_MODE = "HTML";
    private final ReentrantLock lock = new ReentrantLock();
    private TemplateEngine templateEngine;
    private Context context;
    private FileTemplateResolver templateResolver;

    /**
     * @deprecated Use {@link #ThymeleafTemplateEngine(JBakeConfiguration, ContentStore)} instead
     */
	@Deprecated
    public ThymeleafTemplateEngine(final CompositeConfiguration config, final ContentStore db, final File destination, final File templatesPath) {
        super(config, db, destination, templatesPath);
        this.context = new Context();
        initializeTemplateEngine();
    }

    public ThymeleafTemplateEngine(final JBakeConfiguration config, final ContentStore db) {
        super(config, db);
        this.context = new Context();
        initializeTemplateEngine();
    }

    private void initializeTemplateEngine() {
        templateResolver = new FileTemplateResolver();
        templateResolver.setPrefix(config.getTemplateFolder().getAbsolutePath() + File.separatorChar);
        templateResolver.setCharacterEncoding(config.getTemplateEncoding());
        templateResolver.setTemplateMode(DEFAULT_TEMPLATE_MODE);
        templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);
        templateEngine.clearTemplateCache();
    }

    private void updateTemplateMode(Map<String, Object> model) {
        templateResolver.setTemplateMode(getTemplateModeByModel(model));
    }

    private String getTemplateModeByModel(Map<String, Object> model) {
        @SuppressWarnings("unchecked")
        Map<String, Object> config = (Map<String, Object>) model.get("config");
        @SuppressWarnings("unchecked")
        Map<String, Object> content = (Map<String, Object>) model.get("content");
        if (config != null && content != null) {
            String key = "template_" + content.get(Attributes.TYPE) + "_thymeleaf_mode";
            String configMode = (String) config.get(key);
            if (configMode != null) {
                return configMode;
            }
        }
        return DEFAULT_TEMPLATE_MODE;
    }

    @Override
    public void renderDocument(Map<String, Object> model, String templateName, Writer writer) throws RenderingException {

        String localeString = config.getThymeleafLocale();
        Locale locale = localeString != null ? LocaleUtils.toLocale(localeString) : Locale.getDefault();


        lock.lock();
        try {
            initializeContext(locale,model);
            updateTemplateMode(model);
            templateEngine.process(templateName, context, writer);
        } finally {
            lock.unlock();
        }
    }

    private void initializeContext(Locale locale, Map<String, Object> model) {
        context.clearVariables();
        context.setLocale(locale);
        context.setVariables(model);

        for (String key : extractors.keySet()) {
            context.setVariable(key, new ContextVariable(db,key,model));
        }
    }

    /**
     * Helper class to lazy load data form extractors by key
     */
    private class ContextVariable extends LazyContextVariable {

        private ContentStore db;
        private String key;
        private Map<String,Object> model;

        public ContextVariable(ContentStore db, String key, Map<String, Object> model) {
            this.db = db;
            this.key = key;
            this.model = model;
        }

        @Override
        protected Object loadValue() {

            try {
                return extractors.extractAndTransform(db, key, model, new TemplateEngineAdapter<LazyContextVariable>() {
                    @Override
                    public LazyContextVariable adapt(String key, final Object extractedValue) {
                        if (key.equals(Attributes.ALLTAGS)) {
                            return new LazyContextVariable<Set<?>>() {
                                @Override
                                protected Set<?> loadValue() {
                                    return (Set<?>) extractedValue;
                                }
                            };
                        } else if (key.equals(Attributes.PUBLISHED_DATE)) {
                            return new LazyContextVariable<Date>() {
                                @Override
                                protected Date loadValue() {
                                    return (Date) extractedValue;
                                }
                            };
                        } else {
                            return new LazyContextVariable<Object>() {
                                @Override
                                protected Object loadValue() {
                                    return extractedValue;
                                }
                            };
                        }
                    }
                }).getValue();
            } catch (NoModelExtractorException e) {
                return "";
            }
        }
    }
}
