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
import org.apache.commons.configuration.CompositeConfiguration;
import org.jbake.app.FileUtil;

import java.io.File;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A template which is responsible for delegating to a supported template engine,
 * based on the file extension.
 *
 * @author Cédric Champeau
 */
public class DelegatingTemplateEngine extends AbstractTemplateEngine {
    private final TemplateEngines renderers;

    public DelegatingTemplateEngine(final CompositeConfiguration config, final ODatabaseDocumentTx db, final File destination, final File templatesPath) {
        super(config, db, destination, templatesPath);
        this.renderers = new TemplateEngines(config, db, destination, templatesPath);
    }

    @Override
    public void renderDocument(final Map<String, Object> model, final String templateName, final Writer writer) throws RenderingException {
        model.put("version", config.getString("version"));
        Map<String, Object> configModel = new HashMap<String, Object>();
        Iterator<String> configKeys = config.getKeys();
        while (configKeys.hasNext()) {
            String key = configKeys.next();
            //replace "." in key so you can use dot notation in templates
            configModel.put(key.replace(".", "_"), config.getProperty(key));
        }
        model.put("config", configModel);
        String ext = FileUtil.fileExt(templateName);
        AbstractTemplateEngine engine = renderers.getEngine(ext);
        if (engine!=null) {
            engine.renderDocument(model, templateName, writer);
        } else {
            System.err.println("Warning - No template engine found for template: "+templateName);
        }
    }
}
