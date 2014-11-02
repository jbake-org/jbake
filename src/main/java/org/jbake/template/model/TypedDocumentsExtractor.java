package org.jbake.template.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.jbake.app.DBUtil;
import org.jbake.app.DocumentList;
import org.jbake.template.ModelExtractor;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;

public class TypedDocumentsExtractor implements ModelExtractor<DocumentList> {

	@Override
	public DocumentList get(ODatabaseDocumentTx db, Map model, String key) {
		// document types are pluralized in model, so unpluralize
		try {
			String type = ModelExtractionUtils.unpluralizeDocumentType(key);
			return DocumentList.wrap(DBUtil.query(db, "select * from "+type+" order by date desc").iterator());
		} catch(UnsupportedOperationException e) {
	    	List<ODocument> none = Collections.emptyList();
			return DocumentList.wrap(none.iterator());
		}
	}

}
