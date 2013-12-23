/*
 * Copyright 2003-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
import org.jbake.app.DBUtil;
import org.jbake.app.DocumentList;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Renders documents using a Groovy template engine. Depending on the file extension of the template, the template
 * engine will either be a {@link groovy.text.SimpleTemplateEngine}, or an {@link groovy.text.XmlTemplateEngine}
 * (.gxml).
 *
 * @author Cédric Champeau
 */
public class GroovyTemplateEngine extends AbstractTemplateEngine {

    private final Map<String, Template> cachedTemplates = new HashMap<String, Template>();

    public GroovyTemplateEngine(final CompositeConfiguration config, final ODatabaseDocumentTx db, final File destination, final File templatesPath) {
        super(config, db, destination, templatesPath);
    }

    @Override
    public void renderDocument(final Map<String, Object> model, final String templateName, final File outputFile) throws RenderingException {
        try {
            outputFile.getParentFile().mkdirs();
            Template template = findTemplate(templateName);
            Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), config.getString("render.encoding")));
            Writable writable = template.make(wrap(model));
            writable.writeTo(out);
            out.flush();
            out.close();
        } catch (Exception e) {
            throw new RenderingException(e);
        }
    }

    private Template findTemplate(final String templateName) throws SAXException, ParserConfigurationException, ClassNotFoundException, IOException {
        TemplateEngine ste = templateName.endsWith(".gxml") ? new XmlTemplateEngine() : new SimpleTemplateEngine();
        File sourceTemplate = new File(templatesPath, templateName);
        Template template = cachedTemplates.get(templateName);
        if (template == null) {
            template = ste.createTemplate(new InputStreamReader(new BufferedInputStream(new FileInputStream(sourceTemplate)), config.getString("template.encoding")));
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
                        List<ODocument> query = db.query(new OSQLSynchQuery<ODocument>("select * from post where status='published'"));
                        return DocumentList.wrap(query.iterator());
                    }
                    if ("pages".equals(key) || "posts".equals(key)) {
                        return DocumentList.wrap(db.browseClass(key.substring(0, key.length() - 1)));
                    }
                    if ("tag_posts".equals(key)) {
                        String tag = model.get("tag").toString();
                        // fetch the tag posts from db
                        List<ODocument> query = DBUtil.query(db, "select * from post where status='published' where ? in tags", tag);
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

    private String doInclude(Map<String, Object> model, String templateName) throws Exception {
        Template tpl = findTemplate(templateName);
        Object out = model.get("out");
        String str = tpl.make(model).toString();
        model.put("out", out);
        return str;
    }
}
