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
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import org.apache.commons.configuration.CompositeConfiguration;
import org.jbake.app.ContentStore;
import org.jbake.app.Crawler;
import org.jbake.app.configuration.JBakeConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

/**
 * Renders pages using the <a href="http://freemarker.org/">Freemarker</a> template engine.
 *
 * @author Cédric Champeau
 */
public class FreemarkerTemplateEngine extends AbstractTemplateEngine {

    private Configuration templateCfg;

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
        try {
            templateCfg.setDirectoryForTemplateLoading(config.getTemplateFolder());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void renderDocument(final Map<String, Object> model, final String templateName, final Writer writer) throws RenderingException {
        try {
            Template template = templateCfg.getTemplate(templateName);
            template.process(new LazyLoadingModel(templateCfg.getObjectWrapper(), model, db), writer);
        } catch (IOException e) {
            throw new RenderingException(e);
        } catch (TemplateException e) {
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

        public LazyLoadingModel(ObjectWrapper wrapper, Map<String, Object> eagerModel, final ContentStore db) {
            this.eagerModel = new SimpleHash(eagerModel, wrapper);
            this.db = db;
            this.wrapper = wrapper;
        }

        @Override
        public TemplateModel get(final String key) throws TemplateModelException {
        	try {

        		// GIT Issue#357: Accessing db in freemarker template throws exception
        		// When content store is accessed with key "db" then wrap the ContentStore with BeansWrapper and return to template.
        		// All methods on db are then accessible in template. Eg: ${db.getPublishedPostsByTag(tagName).size()}
        		if(key.equals(Crawler.Attributes.DB)) {
        			BeansWrapperBuilder bwb = new BeansWrapperBuilder(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
    				BeansWrapper bw = bwb.build();
    				return bw.wrap(db);
				}


        		return extractors.extractAndTransform(db, key, eagerModel.toMap(), new TemplateEngineAdapter<TemplateModel>() {

					@Override
					public TemplateModel adapt(String key, Object extractedValue) {
						if(key.equals(Crawler.Attributes.ALLTAGS)) {
							return new SimpleCollection((Collection) extractedValue, wrapper);
						} else if(key.equals(Crawler.Attributes.PUBLISHED_DATE)) {
							return new SimpleDate((Date) extractedValue, TemplateDateModel.UNKNOWN);
						} else {
							// All other cases, as far as I know, are document collections
							return new SimpleSequence((Collection) extractedValue, wrapper);
						}
										
					}
				});
        	} catch(NoModelExtractorException e) {
        		return eagerModel.get(key);
        	}
        }

        @Override
        public boolean isEmpty() throws TemplateModelException {
            return false;
        }

    }

}
