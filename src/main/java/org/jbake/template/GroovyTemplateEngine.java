package org.jbake.template;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;

import groovy.lang.GString;
import groovy.lang.Writable;
import groovy.text.SimpleTemplateEngine;
import groovy.text.Template;
import groovy.text.TemplateEngine;
import groovy.text.XmlTemplateEngine;

import org.apache.commons.configuration.CompositeConfiguration;
import org.codehaus.groovy.runtime.MethodClosure;
import org.jbake.app.ConfigUtil.Keys;
import org.jbake.app.DBUtil;
import org.jbake.app.DocumentList;
import org.jbake.model.DocumentTypes;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jbake.app.ContentStore;

/**
 * Renders documents using a Groovy template engine. Depending on the file extension of the template, the template
 * engine will either be a {@link groovy.text.SimpleTemplateEngine}, or an {@link groovy.text.XmlTemplateEngine}
 * (.gxml).
 *
 * @author Cédric Champeau
 */
public class GroovyTemplateEngine extends AbstractTemplateEngine {

    private final Map<String, Template> cachedTemplates = new HashMap<String, Template>();

    public GroovyTemplateEngine(final CompositeConfiguration config, final ContentStore db, final File destination, final File templatesPath) {
        super(config, db, destination, templatesPath);
    }

    @Override
    public void renderDocument(final Map<String, Object> model, final String templateName, final Writer writer) throws RenderingException {
        try {
            Template template = findTemplate(templateName);
            Writable writable = template.make(wrap(model));
            writable.writeTo(writer);
        } catch (Exception e) {
            throw new RenderingException(e);
        }
    }

    private Template findTemplate(final String templateName) throws SAXException, ParserConfigurationException, ClassNotFoundException, IOException {
        TemplateEngine ste = templateName.endsWith(".gxml") ? new XmlTemplateEngine() : new SimpleTemplateEngine();
        File sourceTemplate = new File(templatesPath, templateName);
        Template template = cachedTemplates.get(templateName);
        if (template == null) {
            template = ste.createTemplate(new InputStreamReader(new BufferedInputStream(new FileInputStream(sourceTemplate)), config.getString(Keys.TEMPLATE_ENCODING)));
            cachedTemplates.put(templateName, template);
        }
        return template;
    }

    private Map<String, Object> wrap(final Map<String, Object> model) {
        return new HashMap<String, Object>(model) {
            @Override
            public Object get(final Object property) {
                if (property instanceof String || property instanceof GString) {
                    String key = property.toString();
                    if ("include".equals(key)) {
                        return new MethodClosure(GroovyTemplateEngine.this, "doInclude").curry(this);
                    }
                    if ("db".equals(key)) {
                        return db;
                    }
                    if ("published_posts".equals(key)) {
                        List<ODocument> query = db.getPublishedPosts(); //query(new OSQLSynchQuery<ODocument>("select * from post where status='published' order by date desc"));
                        return DocumentList.wrap(query.iterator());
                    }
                    if ("published_pages".equals(key)) {
                        List<ODocument> query = db.getPublishedPages(); //query(new OSQLSynchQuery<ODocument>("select * from page where status='published' order by date desc"));
                        return DocumentList.wrap(query.iterator());
                    }
                    if ("published_content".equals(key)) {
                    	List<ODocument> publishedContent = new ArrayList<ODocument>();
                    	String[] documentTypes = DocumentTypes.getDocumentTypes();
                    	for (String docType : documentTypes) {
                    		List<ODocument> query = db.getPublishedContent(docType);
                    		publishedContent.addAll(query);
                    	}
                    	return DocumentList.wrap(publishedContent.iterator());
                    }
                    if ("all_content".equals(key)) {
                    	List<ODocument> allContent = new ArrayList<ODocument>();
                    	String[] documentTypes = DocumentTypes.getDocumentTypes();
                    	for (String docType : documentTypes) {
                    		List<ODocument> query = db.getAllContent(docType);
                    		allContent.addAll(query);
                    	}
                    	return DocumentList.wrap(allContent.iterator());
                    }
                    if ("alltags".equals(key)) {
                        List<ODocument> query = db.getAllTagsFromPublishedPosts();
                        Set<String> result = new HashSet<String>();
                        for (ODocument document : query) {
                            String[] tags = DBUtil.toStringArray(document.field("tags"));
                            Collections.addAll(result, tags);
                        }
                        return result;
                    }
                    String[] documentTypes = DocumentTypes.getDocumentTypes();
                    for (String docType : documentTypes) {
                        if ((docType+"s").equals(key)) {
                            return DocumentList.wrap(db.getAllContent(docType).iterator());
                        }
                    }
                    if ("tag_posts".equals(key)) {
                        String tag = model.get("tag").toString();
                        // fetch the tag posts from db
                        List<ODocument> query = db.getPublishedPostsByTag(tag);
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

    private void doInclude(Map<String, Object> model, String templateName) throws Exception {
        AbstractTemplateEngine engine = (AbstractTemplateEngine) model.get("renderer");
        Writer out = (Writer) model.get("out");
        engine.renderDocument(model, templateName, out);
        model.put("out", out);
    }
}
