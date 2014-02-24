package org.jbake.template;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
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
import org.apache.commons.configuration.CompositeConfiguration;
import org.jbake.app.DBUtil;
import org.jbake.app.DocumentList;
import org.jbake.model.DocumentTypes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Renders pages using the <a href="http://freemarker.org/">Freemarker</a> template engine.
 *
 * @author Cédric Champeau
 */
public class FreemarkerTemplateEngine extends AbstractTemplateEngine {

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
            if ("published_posts".equals(key)) {
                List<ODocument> query = db.query(new OSQLSynchQuery<ODocument>("select * from post where status='published' order by date desc"));
                return new SimpleSequence(DocumentList.wrap(query.iterator()));
            }
            if ("published_pages".equals(key)) {
                List<ODocument> query = db.query(new OSQLSynchQuery<ODocument>("select * from page where status='published' order by date desc"));
                return new SimpleSequence(DocumentList.wrap(query.iterator()));
            }
            if ("published_content".equals(key)) {
            	List<ODocument> publishedContent = new ArrayList<ODocument>();
            	String[] documentTypes = DocumentTypes.getDocumentTypes();
            	for (String docType : documentTypes) {
            		List<ODocument> query = db.query(new OSQLSynchQuery<ODocument>("select * from "+docType+" where status='published' order by date desc"));
            		publishedContent.addAll(query);
            	}
            	return new SimpleSequence(DocumentList.wrap(publishedContent.iterator()));
            }
            if ("all_content".equals(key)) {
            	List<ODocument> allContent = new ArrayList<ODocument>();
            	String[] documentTypes = DocumentTypes.getDocumentTypes();
            	for (String docType : documentTypes) {
            		List<ODocument> query = db.query(new OSQLSynchQuery<ODocument>("select * from "+docType+" order by date desc"));
            		allContent.addAll(query);
            	}
            	return new SimpleSequence(DocumentList.wrap(allContent.iterator()));
            }
            if ("alltags".equals(key)) {
                List<ODocument> query = db.query(new OSQLSynchQuery<ODocument>("select tags from post where status='published'"));
                Set<String> result = new HashSet<String>();
                for (ODocument document : query) {
                    String[] tags = DBUtil.toStringArray(document.field("tags"));
                    Collections.addAll(result, tags);
                }
                return new SimpleCollection(result);
            }
            String[] documentTypes = DocumentTypes.getDocumentTypes();
            for (String docType : documentTypes) {
                if ((docType+"s").equals(key)) {
                    return new SimpleSequence(DocumentList.wrap(DBUtil.query(db, "select * from "+docType+" order by date desc").iterator()));
                }
            }
            if ("tag_posts".equals(key)) {
                String tag = eagerModel.get("tag").toString();
                // fetch the tag posts from db
                List<ODocument> query = DBUtil.query(db, "select * from post where status='published' where ? in tags order by date desc", tag);
                return new SimpleSequence(DocumentList.wrap(query.iterator()));
            }
            if ("published_date".equals(key)) {
                return new SimpleDate(new Date(), TemplateDateModel.UNKNOWN);
            }
            return eagerModel.get(key);
        }

        @Override
        public boolean isEmpty() throws TemplateModelException {
            return false;
        }

    }

}
