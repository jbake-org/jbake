package org.jbake.template;

import groovy.lang.GString;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.configuration.CompositeConfiguration;
import org.jbake.app.DBUtil;
import org.jbake.app.DocumentList;
import org.jbake.model.DocumentTypes;

import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.error.PebbleException;
import com.mitchellbosecke.pebble.loader.FileLoader;
import com.mitchellbosecke.pebble.loader.Loader;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;

/**
 * Renders pages using the <a
 * href="http://www.mitchellbosecke.com/pebble">Pebble</a> template engine.
 *
 * @author Mitchell Bosecke
 */
public class PebbleTemplateEngine extends AbstractTemplateEngine {

    private PebbleEngine engine;

    public PebbleTemplateEngine(final CompositeConfiguration config, final ODatabaseDocumentTx db,
            final File destination, final File templatesPath) {
        super(config, db, destination, templatesPath);
        initializeTemplateEngine(config, templatesPath);
    }

    private void initializeTemplateEngine(final CompositeConfiguration config, final File templatesPath) {
        Loader loader = new FileLoader();
        loader.setPrefix(templatesPath.getAbsolutePath());
        engine = new PebbleEngine(loader);
    }

    @Override
    public void renderDocument(final Map<String, Object> model, final String templateName, final Writer writer)
            throws RenderingException {

        PebbleTemplate template;
        try {
            template = engine.getTemplate(templateName);
            template.evaluate(writer, wrap(model));
        } catch (PebbleException e) {
            throw new RenderingException(e);
        } catch (IOException e) {
            throw new RenderingException(e);
        }

    }

    private Map<String, Object> wrap(final Map<String, Object> model) {
        return new HashMap<String, Object>(model) {

            private static final long serialVersionUID = -5489285491728950547L;

            @Override
            public Object get(final Object property) {
                if (property instanceof String || property instanceof GString) {
                    String key = property.toString();

                    if ("db".equals(key)) {
                        return db;
                    }
                    if ("published_posts".equals(key)) {
                        List<ODocument> query = db.query(new OSQLSynchQuery<ODocument>(
                                "select * from post where status='published' order by date desc"));
                        return DocumentList.wrap(query.iterator());
                    }
                    if ("published_pages".equals(key)) {
                        List<ODocument> query = db.query(new OSQLSynchQuery<ODocument>(
                                "select * from page where status='published' order by date desc"));
                        return DocumentList.wrap(query.iterator());
                    }
                    if ("published_content".equals(key)) {
                        List<ODocument> publishedContent = new ArrayList<ODocument>();
                        String[] documentTypes = DocumentTypes.getDocumentTypes();
                        for (String docType : documentTypes) {
                            List<ODocument> query = db.query(new OSQLSynchQuery<ODocument>("select * from " + docType
                                    + " where status='published' order by date desc"));
                            publishedContent.addAll(query);
                        }
                        return DocumentList.wrap(publishedContent.iterator());
                    }
                    if ("all_content".equals(key)) {
                        List<ODocument> allContent = new ArrayList<ODocument>();
                        String[] documentTypes = DocumentTypes.getDocumentTypes();
                        for (String docType : documentTypes) {
                            List<ODocument> query = db.query(new OSQLSynchQuery<ODocument>("select * from " + docType
                                    + " order by date desc"));
                            allContent.addAll(query);
                        }
                        return DocumentList.wrap(allContent.iterator());
                    }
                    if ("alltags".equals(key)) {
                        List<ODocument> query = db.query(new OSQLSynchQuery<ODocument>(
                                "select tags from post where status='published'"));
                        Set<String> result = new HashSet<String>();
                        for (ODocument document : query) {
                            String[] tags = DBUtil.toStringArray(document.field("tags"));
                            Collections.addAll(result, tags);
                        }
                        return result;
                    }
                    String[] documentTypes = DocumentTypes.getDocumentTypes();
                    for (String docType : documentTypes) {
                        if ((docType + "s").equals(key)) {
                            return DocumentList.wrap(DBUtil.query(db,
                                    "select * from " + docType + " order by date desc").iterator());
                        }
                    }
                    if ("tag_posts".equals(key)) {
                        String tag = model.get("tag").toString();
                        // fetch the tag posts from db
                        List<ODocument> query = DBUtil.query(db,
                                "select * from post where status='published' where ? in tags order by date desc", tag);
                        return DocumentList.wrap(query.iterator());
                    }
                    if ("published_date".equals(key)) {
                        return new Date();
                    }
                }

                return super.get(property);
            }
        };
    }

}
