package org.jbake.template.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jbake.app.DocumentList;
import org.jbake.model.DocumentTypes;
import org.jbake.template.ModelExtractor;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;

public class PublishedContentExtractor implements ModelExtractor<DocumentList> {

	@Override
	public DocumentList get(ODatabaseDocumentTx db, Map model, String key) {
    	List<ODocument> publishedContent = new ArrayList<ODocument>();
    	String[] documentTypes = DocumentTypes.getDocumentTypes();
    	for (String docType : documentTypes) {
    		List<ODocument> query = db.query(new OSQLSynchQuery<ODocument>("select * from "+docType+" where status='published' order by date desc"));
    		publishedContent.addAll(query);
    	}
    	return DocumentList.wrap(publishedContent.iterator());
	}

}
