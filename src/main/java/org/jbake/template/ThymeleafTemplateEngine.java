package org.jbake.template;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.lang.LocaleUtils;
import org.jbake.app.ConfigUtil.Keys;
import org.jbake.app.DBUtil;
import org.jbake.app.DocumentList;
import org.jbake.model.DocumentTypes;
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
import java.util.concurrent.locks.ReentrantLock;
import org.jbake.app.ContentStore;

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
    private final ReentrantLock lock = new ReentrantLock();

    private TemplateEngine templateEngine;
    private FileTemplateResolver templateResolver;

    public ThymeleafTemplateEngine(final CompositeConfiguration config, final ContentStore db, final File destination, final File templatesPath) {
        super(config, db, destination, templatesPath);
        initializeTemplateEngine();
    }

    private void initializeTemplateEngine() {
        templateResolver = new FileTemplateResolver();
        templateResolver.setPrefix(templatesPath.getAbsolutePath() + File.separatorChar);
        templateResolver.setCharacterEncoding(config.getString(Keys.TEMPLATE_ENCODING));
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

    private class JBakeVariablesMap extends VariablesMap<String, Object> {

        public JBakeVariablesMap(final Map<String, Object> model) {
            super(model);
            completeModel();
        }

        private void completeModel() {
            put("db", db);
            put("alltags", getAllTags());
            put("tag_posts", getTagPosts());
            put("published_date", new Date());
            String[] documentTypes = DocumentTypes.getDocumentTypes();
            for (String docType : documentTypes) {
                put(docType + "s", DocumentList.wrap(DBUtil.query(db, "select * from " + docType + " order by date desc").iterator()));
                put("published_" + docType + "s", DocumentList.wrap(DBUtil.query(db, "select * from " + docType + " where status='published' order by date desc").iterator()));
            }
            put("published_content", getPublishedContent());
            put("all_content", getAllContent());
        }

        private Object getTagPosts() {
            Object tagName = get("tag");
            if (tagName != null) {
                String tag = tagName.toString();
                // fetch the tag posts from db
                List<ODocument> query = DBUtil.query(db, "select * from post where status='published' where ? in tags order by date desc", tag);
                return DocumentList.wrap(query.iterator());
            } else {
                return Collections.emptyList();
            }
        }

        private Object getAllTags() {
            List<ODocument> query = db.query(new OSQLSynchQuery<ODocument>("select tags from post where status='published'"));
            Set<String> result = new HashSet<String>();
            for (ODocument document : query) {
                String[] tags = DBUtil.toStringArray(document.field("tags"));
                Collections.addAll(result, tags);
            }
            return result;
        }
        
        private Object getPublishedContent() {
        	List<ODocument> publishedContent = new ArrayList<ODocument>();
        	String[] documentTypes = DocumentTypes.getDocumentTypes();
        	for (String docType : documentTypes) {
        		List<ODocument> query = db.query(new OSQLSynchQuery<ODocument>("select * from "+docType+" where status='published' order by date desc"));
        		publishedContent.addAll(query);
        	}
        	return DocumentList.wrap(publishedContent.iterator());
        }
        
        private Object getAllContent() {
        	List<ODocument> allContent = new ArrayList<ODocument>();
        	String[] documentTypes = DocumentTypes.getDocumentTypes();
        	for (String docType : documentTypes) {
        		List<ODocument> query = db.query(new OSQLSynchQuery<ODocument>("select * from "+docType+" order by date desc"));
        		allContent.addAll(query);
        	}
        	return DocumentList.wrap(allContent.iterator());
        }
    }
}
