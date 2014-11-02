package org.jbake.template.model;

import java.util.Map;

import org.jbake.template.ModelExtractor;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;

public class DBExtractor implements ModelExtractor<ODatabaseDocumentTx> {

	@Override
	public ODatabaseDocumentTx get(ODatabaseDocumentTx db, Map model, String key) {
		return db;
	}

}
