package org.jbake.template.model;

import java.util.List;
import java.util.Map;

import org.jbake.app.DBUtil;
import org.jbake.app.DocumentList;
import org.jbake.template.ModelExtractor;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;

public class TagPostsExtractor implements ModelExtractor<DocumentList> {

	@Override
	public DocumentList get(ODatabaseDocumentTx db, Map model, String key) {
        String tag = model.get(Names.TAG).toString();
        // fetch the tag posts from db
        List<ODocument> query = DBUtil.query(db, "select * from post where status='published' where ? in tags order by date desc", tag);
        return DocumentList.wrap(query.iterator());
	}

}
