package org.jbake.template;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;

import de.neuland.jade4j.JadeConfiguration;
import de.neuland.jade4j.exceptions.JadeCompilerException;
import de.neuland.jade4j.filter.CDATAFilter;
import de.neuland.jade4j.filter.CssFilter;
import de.neuland.jade4j.filter.Filter;
import de.neuland.jade4j.filter.JsFilter;
import de.neuland.jade4j.model.JadeModel;
import de.neuland.jade4j.template.FileTemplateLoader;
import de.neuland.jade4j.template.JadeTemplate;
import de.neuland.jade4j.template.TemplateLoader;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.lang.StringEscapeUtils;
import org.jbake.app.ContentStore;
import org.jbake.app.DBUtil;
import org.jbake.app.DocumentList;
import org.jbake.model.DocumentTypes;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Renders pages using the <a href="http://jade.org/">Jade</a> template language.
 *
 * @author Aleksandar Vidakovic
 */
public class JadeTemplateEngine extends AbstractTemplateEngine {
    private static final String FILTER_CDATA = "cdata";
    private static final String FILTER_STYLE = "css";
    private static final String FILTER_SCRIPT = "js";

    private JadeConfiguration jadeConfiguration = new JadeConfiguration();

    public JadeTemplateEngine(final CompositeConfiguration config, final ContentStore db, final File destination, final File templatesPath) {
        super(config, db, destination, templatesPath);

        TemplateLoader loader = new FileTemplateLoader(templatesPath.getAbsolutePath() + "/", "UTF-8");
        jadeConfiguration.setTemplateLoader(loader);
        jadeConfiguration.setFilter(FILTER_CDATA, new CDATAFilter());
        jadeConfiguration.setFilter(FILTER_SCRIPT, new JsFilter());
        jadeConfiguration.setFilter(FILTER_STYLE, new CssFilter());
        jadeConfiguration.getSharedVariables().put("formatter", new FormatHelper());
    }

    @Override
    public void renderDocument(Map<String, Object> model, String templateName, Writer writer) throws RenderingException {
        try {
            JadeTemplate template = jadeConfiguration.getTemplate(templateName);

            renderTemplate(template, model, writer);
        } catch (IOException e) {
            throw new RenderingException(e);
        }
    }

    public void renderTemplate(JadeTemplate template, Map<String, Object> model, Writer writer) throws JadeCompilerException {
        JadeModel jadeModel = wrap(jadeConfiguration.getSharedVariables());
        jadeModel.putAll(model);
        template.process(jadeModel, writer);
    }

    private JadeModel wrap(final Map<String, Object> model) {
        return new JadeModel(model) {

            @Override
            public Object get(final Object property) {
                String key = property.toString();
                if ("db".equals(key)) {
                    return db;
                }
                if ("published_posts".equals(key)) {
                    List<ODocument> query = db.getPublishedPosts();
                    return DocumentList.wrap(query.iterator());
                }
                if ("published_pages".equals(key)) {
                    List<ODocument> query = db.getPublishedPages();
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

                return super.get(property);
            }
        };
    }

    public static class FormatHelper {
        private Map<String, SimpleDateFormat> formatters = new HashMap<String, SimpleDateFormat>();

        public String format(Date date, String pattern) {
            if(date!=null && pattern!=null) {
                SimpleDateFormat df = formatters.get(pattern);

                if(df==null) {
                    df = new SimpleDateFormat(pattern);
                    formatters.put(pattern, df);
                }

                return df.format(date);
            } else {
                return "";
            }
        }

        public String escape(String s) {
            return StringEscapeUtils.escapeHtml(s);
        }
    }
}
