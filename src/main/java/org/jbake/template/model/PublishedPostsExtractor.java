package org.jbake.template.model;

import java.util.List;
import java.util.Map;

import org.jbake.app.DocumentList;
import org.jbake.template.ModelExtractor;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;

public class PublishedPostsExtractor implements ModelExtractor<DocumentList> {

	@Override
	public DocumentList get(ODatabaseDocumentTx db, Map model, String key) {
        List<ODocument> query = db.query(new OSQLSynchQuery<ODocument>("select * from post where status='published' order by date desc"));
        return DocumentList.wrap(query.iterator());
	}

}
