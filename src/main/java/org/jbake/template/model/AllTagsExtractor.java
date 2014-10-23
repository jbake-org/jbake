package org.jbake.template.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jbake.app.DBUtil;
import org.jbake.template.ModelExtractor;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;

public class AllTagsExtractor implements ModelExtractor<Set<String>> {

	@Override
	public Set<String> get(ODatabaseDocumentTx db, Map model, String key) {
        List<ODocument> query = db.query(new OSQLSynchQuery<ODocument>("select tags from post where status='published'"));
        Set<String> result = new HashSet<String>();
        for (ODocument document : query) {
            String[] tags = DBUtil.toStringArray(document.field("tags"));
            Collections.addAll(result, tags);
        }
        return result;
	}

}
