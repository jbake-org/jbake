package org.jbake.template.model;

import java.util.Date;
import java.util.Map;

import org.jbake.template.ModelExtractor;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;

public class PublishedDateExtractor implements ModelExtractor<Date> {

	@Override
	public Date get(ODatabaseDocumentTx db, Map model, String key) {
		return new Date();
	}

}
