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
package org.jbake.app;

import com.orientechnologies.orient.core.record.impl.ODocument;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

/**
 * Wraps an OrientDB document iterator into a model usable by
 * template engines.
 *
 * @author Cédric Champeau
 */
public class DocumentList extends LinkedList<Map<String,Object>> {

    public static DocumentList wrap(Iterator<ODocument> docs) {
        DocumentList list = new DocumentList();
        while (docs.hasNext()) {
            ODocument next = docs.next();
            list.add(DBUtil.documentToModel(next));
        }
        return list;
    }

    public DocumentList() {
    }

}
