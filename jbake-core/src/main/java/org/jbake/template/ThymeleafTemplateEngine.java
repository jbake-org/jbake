package org.jbake.template;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.lang.LocaleUtils;
import org.jbake.app.ConfigUtil.Keys;
import org.jbake.app.ContentStore;
import org.jbake.app.Crawler;
import org.jbake.app.Crawler.Attributes;
import org.jbake.app.DocumentList;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.context.LazyContextVariable;
import org.thymeleaf.templateresolver.FileTemplateResolver;

import java.io.File;
import java.io.Writer;
import java.util.Date;
import java.util.HashMap;
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
 *     template.feed.thymeleaf.mode=XML
 * </code>
 *
 * @author Cédric Champeau
 */
public class ThymeleafTemplateEngine extends AbstractTemplateEngine {
    private final ReentrantLock lock = new ReentrantLock();

    private TemplateEngine templateEngine;
    private FileTemplateResolver templateResolver;

	private String templateMode;

    public ThymeleafTemplateEngine(final CompositeConfiguration config, final ContentStore db, final File destination, final File templatesPath) {
        super(config, db, destination, templatesPath);
    }

    private void initializeTemplateEngine(String mode) {
    	if (mode.equals(templateMode)) {
    		return;
    	}
        templateMode = mode;
        templateResolver = new FileTemplateResolver();
        templateResolver.setPrefix(templatesPath.getAbsolutePath() + File.separatorChar);
        templateResolver.setCharacterEncoding(config.getString(Keys.TEMPLATE_ENCODING));
        templateResolver.setTemplateMode(mode);
        templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);
    }

    @Override
    public void renderDocument(final Map<String, Object> model, final String templateName, final Writer writer) throws RenderingException {
        String localeString = config.getString(Keys.THYMELEAF_LOCALE);
        Locale locale = localeString != null ? LocaleUtils.toLocale(localeString) : Locale.getDefault();
        Context context = new Context(locale, wrap(model));
        lock.lock();
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> config = (Map<String, Object>) model.get("config");
            @SuppressWarnings("unchecked")
            Map<String, Object> content = (Map<String, Object>) model.get("content");
            String mode = "HTML";
            if (config != null && content != null) {
                String key = "template_" + content.get(Attributes.TYPE) + "_thymeleaf_mode";
                String configMode = (String) config.get(key);
                if (configMode != null) {
                    mode = configMode;
                }
            }
            initializeTemplateEngine(mode);
            templateEngine.process(templateName, context, writer);
        } finally {
            lock.unlock();
        }
    }

    private Map<String, Object> wrap(final Map<String, Object> model) {
        return new JBakeMap(model);
    }

    private class JBakeMap extends HashMap<String, Object> {
	    	public JBakeMap(final Map<String, Object> model) {
	            super(model);
	            for(String key : extractors.keySet()) {
		            	try {
						put(key, extractors.extractAndTransform(db, key, model, new TemplateEngineAdapter<LazyContextVariable>() {
							@Override
							public LazyContextVariable adapt(String key, final Object extractedValue) {
								if(key.equals(Crawler.Attributes.ALLTAGS)) {
									return new LazyContextVariable<Set<String>>() {
										@Override
										protected Set<String> loadValue() {
											return (Set<String>) extractedValue; 
										}
									};
								} else if(key.equals(Crawler.Attributes.PUBLISHED_DATE)) {
									return new LazyContextVariable<Date>() {
										@Override
										protected Date loadValue() {
											return (Date) extractedValue; 
										}
									};
								} else {
									// All other cases, as far as I know, are document collections
									return new LazyContextVariable<DocumentList>() {
										@Override
										protected DocumentList loadValue() {
											return (DocumentList) extractedValue;
										}
									};
								}
							}
						}));
						} catch (NoModelExtractorException e) {
							// should never happen, as we iterate over existing extractors
						}
		            }
	        }
    }
}
