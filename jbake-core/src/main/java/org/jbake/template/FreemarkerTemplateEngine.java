package org.jbake.template;


import freemarker.ext.beans.BeansWrapper;
import freemarker.ext.beans.BeansWrapperBuilder;
import freemarker.template.Configuration;
import freemarker.template.ObjectWrapper;
import freemarker.template.SimpleCollection;
import freemarker.template.SimpleDate;
import freemarker.template.SimpleHash;
import freemarker.template.SimpleSequence;
import freemarker.template.Template;
import freemarker.template.TemplateDateModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModelException;
import org.apache.commons.configuration2.CompositeConfiguration;
import org.jbake.app.ContentStore;
import org.jbake.app.configuration.JBakeConfiguration;
import org.jbake.exception.NoModelExtractorException;
import org.jbake.exception.RenderingException;
import org.jbake.model.ModelAttributes;
import org.jbake.model.TemplateModel;
import org.jbake.util.DataFileUtil;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Date;

/**
 * Renders pages using the <a href="http://freemarker.org/">Freemarker</a> template engine.
 *
 * @author Cédric Champeau
 */
public class FreemarkerTemplateEngine extends AbstractTemplateEngine {

    private Configuration templateCfg;

    /**
     * @deprecated use {@link FreemarkerTemplateEngine(JBakeConfiguration, ContentStore)}
     */
    @Deprecated
    public FreemarkerTemplateEngine(final CompositeConfiguration config, final ContentStore db, final File destination, final File templatesPath) {
        super(config, db, destination, templatesPath);
        createTemplateConfiguration();
    }

    public FreemarkerTemplateEngine(final JBakeConfiguration config, final ContentStore db) {
        super(config, db);
        createTemplateConfiguration();
    }

    private void createTemplateConfiguration() {
        templateCfg = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
        templateCfg.setDefaultEncoding(config.getRenderEncoding());
        templateCfg.setOutputEncoding(config.getOutputEncoding());
        if (config.getFreemarkerTimeZone() != null) {
            templateCfg.setTimeZone(config.getFreemarkerTimeZone());
            templateCfg.setSQLDateAndTimeTimeZone(config.getFreemarkerTimeZone());
        }
        try {
            templateCfg.setDirectoryForTemplateLoading(config.getTemplateFolder());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void renderDocument(final TemplateModel model, final String templateName, final Writer writer) throws RenderingException {
        try {
            Template template = templateCfg.getTemplate(templateName);
            template.process(new LazyLoadingModel(templateCfg.getObjectWrapper(), model, db), writer);
        } catch (IOException | TemplateException e) {
            throw new RenderingException(e);
        }
    }

    /**
     * A custom Freemarker model that avoids loading the whole documents into memory if not necessary.
     */
    public static class LazyLoadingModel implements TemplateHashModel {
        private final ObjectWrapper wrapper;
        private final SimpleHash eagerModel;
        private final ContentStore db;

        public LazyLoadingModel(ObjectWrapper wrapper, TemplateModel eagerModel, final ContentStore db) {
            this.eagerModel = new SimpleHash(eagerModel, wrapper);
            this.db = db;
            this.wrapper = wrapper;
        }

        @Override
        public freemarker.template.TemplateModel get(final String key) throws TemplateModelException {
            try {

                /*
                 * GIT Issue#357: Accessing db in freemarker template throws exception
                 * When content store is accessed with key "db" then wrap the ContentStore with BeansWrapper and return to template.
                 * All methods on db are then accessible in template. Eg: <code>${db.getPublishedPostsByTag(tagName).size()}</code>
                 */
                if (key.equals(ModelAttributes.DB)) {
                    BeansWrapperBuilder bwb = new BeansWrapperBuilder(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
                    BeansWrapper bw = bwb.build();
                    return bw.wrap(db);
                }
                if (key.equals(ModelAttributes.DATA)) {
                    BeansWrapperBuilder bwb = new BeansWrapperBuilder(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
                    BeansWrapper bw = bwb.build();
                    return bw.wrap(new DataFileUtil(db));
                }

                return extractors.extractAndTransform(db, key, (TemplateModel) eagerModel.toMap(), (key1, extractedValue) -> {
                    if (key1.equals(ModelAttributes.ALLTAGS)) {
                        return new SimpleCollection((Collection) extractedValue, wrapper);
                    } else if (key1.equals(ModelAttributes.PUBLISHED_DATE)) {
                        return new SimpleDate((Date) extractedValue, TemplateDateModel.UNKNOWN);
                    } else {
                        // All other cases, as far as I know, are document collections
                        return new SimpleSequence((Collection) extractedValue, wrapper);
                    }
                });
            } catch (NoModelExtractorException e) {
                return eagerModel.get(key);
            }
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

    }

}
