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
