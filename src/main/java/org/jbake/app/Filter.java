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
	public static List<Map<String, Object>> getPublishedPosts(List<Map<String, Object>> posts) {
		List<Map<String, Object>> publishedPosts = new ArrayList<Map<String, Object>>();
		for (Map<String, Object> post : posts) {
			if (post.get("status") != null) {
				if (((String)post.get("status")).equalsIgnoreCase("published")) {
					publishedPosts.add(post);
				}
			}
		}
		
		return publishedPosts;
	}
}
