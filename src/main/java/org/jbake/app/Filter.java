package org.jbake.app;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Filters content.
 * 
 * @author Jonathan Bullock <jonbullock@gmail.com>
 *
 */
public class Filter {
	
	/**
	 * Filters published posts.
	 * 
	 * @param posts	The posts to filter
	 * @return		Just the published posts
	 */
	public static List<Map<String, Object>> getPublishedContent(List<Map<String, Object>> contentList) {
		List<Map<String, Object>> publishedContent = new ArrayList<Map<String, Object>>();
		for (Map<String, Object> content : contentList) {
			if (content.get("status") != null) {
				if (((String)content.get("status")).equalsIgnoreCase("published")) {
					publishedContent.add(content);
				}
			}
		}
		
		return publishedContent;
	}
}
