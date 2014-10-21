package org.jbake.template;

/**
 * Temporary extraction of constants used to access data in model has.
 * This interface has for sole goal to make sure all template engines use the same data keys,
 * before to extract them in a sanme fashion "somewhere else"
 * @author ndx
 *
 */
public interface ModelExtractor {

	static final String PUBLISHED_POSTS = "published_posts";
	static final String PUBLISHED_PAGES = "published_pages";
	static final String PUBLISHED_CONTENT = "published_content";
	static final String ALL_CONTENT = "all_content";
	static final String ALLTAGS = "alltags";
	static final String TAG_POSTS = "tag_posts";
	static final String PUBLISHED_DATE = "published_date";
	static final String DB = "db";

}
