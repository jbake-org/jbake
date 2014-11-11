package org.jbake.template;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import de.neuland.jade4j.Jade4J;
import de.neuland.jade4j.JadeConfiguration;
import de.neuland.jade4j.model.JadeModel;
import de.neuland.jade4j.template.FileTemplateLoader;
import de.neuland.jade4j.template.JadeTemplate;
import de.neuland.jade4j.template.TemplateLoader;
import org.apache.commons.configuration.CompositeConfiguration;
import org.jbake.app.DBUtil;
import org.jbake.app.DocumentList;
import org.jbake.model.DocumentTypes;
import org.jbake.template.jade.DateUtils;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.*;

public class JadeTemplateEngine extends AbstractTemplateEngine {

  private final JadeConfiguration jadeConfiguration;

  public JadeTemplateEngine(final CompositeConfiguration config, ODatabaseDocumentTx db, File destination, File templatesPath) {
    super(config, db, destination, templatesPath);

    jadeConfiguration = new JadeConfiguration();
    jadeConfiguration.setMode(Jade4J.Mode.XHTML);
    jadeConfiguration.setPrettyPrint(true);
    String basePath = templatesPath.getAbsolutePath() + File.separatorChar;
    TemplateLoader loader = new FileTemplateLoader(
        basePath,
        config.getString("template.encoding"));
    jadeConfiguration.setTemplateLoader(loader);
  }

  @Override
  public void renderDocument(Map<String, Object> model, String templateName, Writer writer) throws RenderingException {
    try {
      JadeTemplate jadeTemplate = jadeConfiguration.getTemplate(templateName);
      model.put("dates", new DateUtils());
      jadeTemplate.process(wrap(model), writer);
    } catch (IOException e) {
      throw new RenderingException(e);
    }
  }

  private JadeModel wrap(final Map<String, Object> model) {
    return new JBakeVariablesMap(model);
  }

  private class JBakeVariablesMap extends JadeModel {

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
        List<ODocument> query = db.query(new OSQLSynchQuery<ODocument>("select * from " + docType + " where status='published' order by date desc"));
        publishedContent.addAll(query);
      }
      return DocumentList.wrap(publishedContent.iterator());
    }

    private Object getAllContent() {
      List<ODocument> allContent = new ArrayList<ODocument>();
      String[] documentTypes = DocumentTypes.getDocumentTypes();
      for (String docType : documentTypes) {
        List<ODocument> query = db.query(new OSQLSynchQuery<ODocument>("select * from " + docType + " order by date desc"));
        allContent.addAll(query);
      }
      return DocumentList.wrap(allContent.iterator());
    }
  }
}
