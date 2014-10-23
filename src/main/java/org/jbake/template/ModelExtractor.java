package org.jbake.template;

import java.util.Map;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;

/**
 * Temporary extraction of constants used to access data in model has.
 * This interface has for sole goal to make sure all template engines use the same data keys,
 * before to extract them in a sanme fashion "somewhere else"
 * @author ndx
 *
 * @param Type the type of data returned by this model extractor
 */
public interface ModelExtractor<Type extends Object> {
	public static interface Names {

		static final String PUBLISHED_POSTS = "published_posts";
		static final String PUBLISHED_PAGES = "published_pages";
		static final String PUBLISHED_CONTENT = "published_content";
		static final String ALL_CONTENT = "all_content";
		static final String ALLTAGS = "alltags";
		static final String TAG_POSTS = "tag_posts";
		static final String PUBLISHED_DATE = "published_date";
		static final String DB = "db";
		static final String TAG = "tag";
	}

	Type get(ODatabaseDocumentTx db, Map model, String key);

}
