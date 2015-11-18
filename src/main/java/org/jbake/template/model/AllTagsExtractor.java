package org.jbake.template.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jbake.app.ContentStore;
import org.jbake.app.Crawler;
import org.jbake.app.DBUtil;
import org.jbake.template.ModelExtractor;

import com.orientechnologies.orient.core.record.impl.ODocument;

public class AllTagsExtractor implements ModelExtractor<Set<String>> {

	@Override
	public Set<String> get(ContentStore db, Map model, String key) {
        List<ODocument> query = db.getAllTagsFromPublishedPosts();
        Set<String> result = new HashSet<String>();
        for (ODocument document : query) {
            String[] tags = DBUtil.toStringArray(document.field(Crawler.Attributes.TAGS));
            Collections.addAll(result, tags);
        }
        return result;
	}

}