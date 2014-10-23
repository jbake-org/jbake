package org.jbake.template;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import org.apache.commons.configuration.CompositeConfiguration;
import org.jbake.template.ModelExtractor.Names;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
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

/**
 * Renders pages using the <a href="http://freemarker.org/">Freemarker</a> template engine.
 *
 * @author Cédric Champeau
 */
public class FreemarkerTemplateEngine extends AbstractTemplateEngine {
    private static ModelExtractors extractors = new ModelExtractors();

    private Configuration templateCfg;

    public FreemarkerTemplateEngine(final CompositeConfiguration config, final ODatabaseDocumentTx db, final File destination, final File templatesPath) {
        super(config, db, destination, templatesPath);
        createTemplateConfiguration(config, templatesPath);
    }

    private void createTemplateConfiguration(final CompositeConfiguration config, final File templatesPath) {
        templateCfg = new Configuration();
        templateCfg.setDefaultEncoding(config.getString("render.encoding"));
        try {
            templateCfg.setDirectoryForTemplateLoading(templatesPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        templateCfg.setObjectWrapper(new DefaultObjectWrapper());
    }

    @Override
    public void renderDocument(final Map<String, Object> model, final String templateName, final Writer writer) throws RenderingException {
        try {
            Template template = templateCfg.getTemplate(templateName);
            template.process(new LazyLoadingModel(model, db), writer);
        } catch (IOException e) {
            throw new RenderingException(e);
        } catch (TemplateException e) {
            throw new RenderingException(e);
        }
    }

    /**
     * A custom Freemarker model that avoids loading the whole documents into memory if not neccessary.
     */
    public static class LazyLoadingModel implements TemplateHashModel {
        private final SimpleHash eagerModel;
        private final ODatabaseDocumentTx db;

        public LazyLoadingModel(final Map<String, Object> eagerModel, final ODatabaseDocumentTx db) {
            this.eagerModel = new SimpleHash(eagerModel);
            this.db = db;
        }

        @Override
        public TemplateModel get(final String key) throws TemplateModelException {
        	try {
        		return extractors.extractAndTransform(db, key, eagerModel.toMap(), new TemplateEngineAdapter<TemplateModel>() {

					@Override
					public TemplateModel adapt(String key, Object extractedValue) {
						if(key.equals(Names.ALLTAGS)) {
							return new SimpleCollection((Collection) extractedValue);
						} else if(key.equals(Names.PUBLISHED_DATE)) {
							return new SimpleDate((Date) extractedValue, TemplateDateModel.UNKNOWN);
						} else {
							// All other cases, as far as I know, are document collections
							return new SimpleSequence((Collection) extractedValue);
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
