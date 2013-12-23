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
package org.jbake.renderer;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import org.apache.commons.configuration.CompositeConfiguration;

import java.io.File;
import java.util.Map;

/**
 * A renderer is responsible for converting a model into a rendered document. The model
 * consists of key/value pairs, some of them potentially converted from a markup language
 * to HTML already.
 *
 * An appropriate rendering engine will be chosen by JBake based on the template suffix. If
 * contents is not available in the supplied model, a renderer has access to the document
 * database in order to complete the model.
 *
 * Note that some rendering engines may rely on a different rendering model than the one
 * provided by the first argument of {@link #renderDocument(java.util.Map, String, java.io.File)}.
 * In this case, it is the responsability of the engine to convert it.
 *
 * @author Cédric Champeau
 */
public abstract class AbstractRenderer {

    protected final CompositeConfiguration config;
    protected final ODatabaseDocumentTx db;
    protected final File destination;
    protected final File templatesPath;

    protected AbstractRenderer(final CompositeConfiguration config, final ODatabaseDocumentTx db, final File destination, final File templatesPath) {
        this.config = config;
        this.db = db;
        this.destination = destination;
        this.templatesPath = templatesPath;
    }

    public abstract void renderDocument(Map<String,Object> model, String templateName, File outputFile) throws RenderingException;
}
