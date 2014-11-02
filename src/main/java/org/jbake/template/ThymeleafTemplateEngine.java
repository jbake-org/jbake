package org.jbake.template;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.lang.LocaleUtils;
import org.jbake.app.DBUtil;
import org.jbake.app.DocumentList;
import org.jbake.model.DocumentTypes;
import org.jbake.template.ModelExtractor.Names;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.context.VariablesMap;
import org.thymeleaf.templateresolver.FileTemplateResolver;

import java.io.File;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.locks.ReentrantLock;

/**
 * <p>A template engine which renders pages using Thymeleaf.</p>
 *
 * <p>This template engine is not recommanded for large sites because the whole model
 * is loaded into memory due to Thymeleaf internal limitations.</p>
 *
 * <p>The default rendering mode is "HTML5", but it is possible to use another mode
 * for each document type, by adding a key in the configuration, for example:</p>
 * <p/>
 * <code>
 *     template.feed.thymeleaf.mode=XML
 * </code>
 *
 * @author Cédric Champeau
 */
public class ThymeleafTemplateEngine extends AbstractTemplateEngine {
    private static ModelExtractors extractors = new ModelExtractors();
    private final ReentrantLock lock = new ReentrantLock();

    private TemplateEngine templateEngine;
    private FileTemplateResolver templateResolver;

    public ThymeleafTemplateEngine(final CompositeConfiguration config, final ODatabaseDocumentTx db, final File destination, final File templatesPath) {
        super(config, db, destination, templatesPath);
        initializeTemplateEngine();
    }

    private void initializeTemplateEngine() {
        templateResolver = new FileTemplateResolver();
        templateResolver.setPrefix(templatesPath.getAbsolutePath() + File.separatorChar);
        templateResolver.setCharacterEncoding(config.getString("template.encoding"));
        templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);
    }

    @Override
    public void renderDocument(final Map<String, Object> model, final String templateName, final Writer writer) throws RenderingException {
        String localeString = config.getString("thymeleaf.locale");
        Locale locale = localeString != null ? LocaleUtils.toLocale(localeString) : Locale.getDefault();
        Context context = new Context(locale, wrap(model));
        lock.lock();
        try {
        	initializeTemplateEngine();
            @SuppressWarnings("unchecked")
            Map<String, Object> config = (Map<String, Object>) model.get("config");
            @SuppressWarnings("unchecked")
            Map<String, Object> content = (Map<String, Object>) model.get("content");
            String mode = "HTML5";
            if (config != null && content != null) {
                String key = "template_" + content.get("type") + "_thymeleaf_mode";
                String configMode = (String) config.get(key);
                if (configMode != null) {
                    mode = configMode;
                }
            }
            templateResolver.setTemplateMode(mode);
            templateEngine.process(templateName, context, writer);
        } finally {
            lock.unlock();
        }
    }

    private VariablesMap<String, Object> wrap(final Map<String, Object> model) {
        return new JBakeVariablesMap(model);
    }

    /**
     * Due to OGNL interpreter(as far as i understandà, Thymeleaf do not support any form of data lazy loading.
     * As a consequence, all the model extractors are called at variables map construction.
     * This is sad, as it may impact rendering on heavy sites, but has otherwise no effect on code.
     */
    private class JBakeVariablesMap extends VariablesMap<String, Object> {

		public JBakeVariablesMap(final Map<String, Object> model) {
            super(model);
            for(String key : extractors.keySet()) {
            	try {
					put(key, extractors.extractAndTransform(db, key, model, new TemplateEngineAdapter.NoopAdapter()));
				} catch (NoModelExtractorException e) {
					// should never happen, as we iterate over xisting extractors
				}
            }
        }
    }
}
