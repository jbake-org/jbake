package org.jbake.template;

import com.orientechnologies.orient.core.record.impl.ODocument;
import freemarker.template.*;
import org.apache.commons.configuration.CompositeConfiguration;
import org.jbake.app.ConfigUtil.Keys;
import org.jbake.app.ContentStore;
import org.jbake.app.DBUtil;
import org.jbake.app.DocumentList;
import org.jbake.model.DocumentTypes;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.*;

/**
 * Renders pages using the <a href="http://freemarker.org/">Freemarker</a> template engine.
 *
 * @author Cédric Champeau
 */
public class FreemarkerTemplateEngine extends AbstractTemplateEngine {

    private Configuration templateCfg;

    public FreemarkerTemplateEngine(final CompositeConfiguration config, final ContentStore db, final File destination, final File templatesPath) {
        super(config, db, destination, templatesPath);
        createTemplateConfiguration(config, templatesPath);
    }

    private void createTemplateConfiguration(final CompositeConfiguration config, final File templatesPath) {
        templateCfg = new Configuration();
        templateCfg.setDefaultEncoding(config.getString(Keys.RENDER_ENCODING));
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
     * A custom Freemarker model that avoids loading the whole documents into memory if not necessary.
     */
    public static class LazyLoadingModel implements TemplateHashModel {
        private final SimpleHash eagerModel;
        private final ContentStore db;

        public LazyLoadingModel(final Map<String, Object> eagerModel, final ContentStore db) {
            this.eagerModel = new SimpleHash(eagerModel);
            this.db = db;
        }

        @Override
        public TemplateModel get(final String key) throws TemplateModelException {
            if ("published_posts".equals(key)) {
                List<ODocument> query = db.getPublishedPosts();
                return new SimpleSequence(DocumentList.wrap(query.iterator()));
            }
            if ("published_pages".equals(key)) {
                List<ODocument> query = db.getPublishedPages();
                return new SimpleSequence(DocumentList.wrap(query.iterator()));
            }
            if ("published_content".equals(key)) {
                List<ODocument> publishedContent = new ArrayList<ODocument>();
                String[] documentTypes = DocumentTypes.getDocumentTypes();
                for (String docType : documentTypes) {
                    List<ODocument> query = db.getPublishedContent(docType);
                    publishedContent.addAll(query);
                }
                return new SimpleSequence(DocumentList.wrap(publishedContent.iterator()));
            }
            if ("all_content".equals(key)) {
                List<ODocument> allContent = new ArrayList<ODocument>();
                String[] documentTypes = DocumentTypes.getDocumentTypes();
                for (String docType : documentTypes) {
                    List<ODocument> query = db.getAllContent(docType);
                    allContent.addAll(query);
                }
                return new SimpleSequence(DocumentList.wrap(allContent.iterator()));
            }
            if ("alltags".equals(key)) {
                List<ODocument> query = db.getAllTagsFromPublishedPosts();
                Set<String> result = new HashSet<String>();
                for (ODocument document : query) {
                    String[] tags = DBUtil.toStringArray(document.field("tags"));
                    Collections.addAll(result, tags);
                }
                return new SimpleCollection(result);
            }
            String[] documentTypes = DocumentTypes.getDocumentTypes();
            for (String docType : documentTypes) {
                if ((docType + "s").equals(key)) {
                    return new SimpleSequence(DocumentList.wrap(db.getAllContent(docType).iterator()));
                }
            }
            if ("tag_posts".equals(key)) {
                String tag = eagerModel.get("tag").toString();
                // fetch the tag posts from db
                List<ODocument> query = db.getPublishedPostsByTag(tag);
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
