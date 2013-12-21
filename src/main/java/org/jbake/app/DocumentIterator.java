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
import java.util.Map;

public class DocumentIterator implements Iterator<Map<String, Object>> {
    private final Iterator<ODocument> innerIterator;

    public DocumentIterator(final Iterator<ODocument> source) {
        innerIterator = source;
    }

    @Override
    public boolean hasNext() {
        return innerIterator.hasNext();
    }

    @Override
    public Map<String, Object> next() {
        return DBUtil.documentToModel(innerIterator.next());
    }

    @Override
    public void remove() {
        innerIterator.remove();
    }
}
