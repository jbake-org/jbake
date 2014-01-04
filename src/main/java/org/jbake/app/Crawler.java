package org.jbake.app;

import static java.io.File.separator;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.CompositeConfiguration;

/**
 * Crawls a file system looking for content.
 * 
 * @author Jonathan Bullock <jonbullock@gmail.com>
 *
 */
public class Crawler {
	
	// TODO: replace separate lists with custom impl of hashmap that provides methods
	// TODO: to get back certain types of content (i.e. pages or posts), this allows for 
	// TODO: support of extra types with very little extra dev 
	
	private File source;
	private CompositeConfiguration config;
	private Parser parser;
	
	private List<Map<String, Object>> pages = new ArrayList<Map<String, Object>>();
	private List<Map<String, Object>> posts = new ArrayList<Map<String, Object>>();
	private Map<String, List<Map<String, Object>>> postsByTags = new HashMap<String, List<Map<String, Object>>>();
//	private Map<String, List<Map<String, Object>>> postsByarchive = new HashMap<String, List<Map<String, Object>>>();
   private String contentPath;
	
	/**
	 * Creates new instance of Crawler.
	 * 
	 */
	public Crawler(File source, CompositeConfiguration config) {
		this.source = source;
		this.config = config;
		this.contentPath = source.getPath() + separator + config.getString("content.folder");
		this.parser = new Parser(config,contentPath);
	}
	
	/**
	 * Crawl all files and folders looking for content.
	 * 
	 * @param path	Folder to start from
	 */
	public void crawl(File path) {
		File[] contents = path.listFiles(FileUtil.getFileFilter());
		if (contents != null) {
			Arrays.sort(contents);
			for (int i = 0; i < contents.length; i++) {
				if (contents[i].isFile()) {
					System.out.print("Processing [" + contents[i].getPath() + "]... ");
					Map<String, Object> fileContents = parser.processFile(contents[i]);
					if (fileContents != null) {
						fileContents.put("file", contents[i].getPath());
						String uri = FileUtil.asPath(contents[i]).replace(FileUtil.asPath( contentPath), "");
						uri = uri.substring(0, uri.lastIndexOf("."));
						fileContents.put("uri", uri+config.getString("output.extension"));
						
						if (fileContents.get("type").equals("page")) {
							pages.add(fileContents);
						} else {
							// everything else is considered a post
							posts.add(fileContents);
							if (fileContents.get("tags") != null) {
								String[] tags = (String[]) fileContents.get("tags");
								for (String tag : tags) {
									if (postsByTags.containsKey(tag)) {
										postsByTags.get(tag).add(fileContents);
									} else {
										List<Map<String, Object>> posts = new ArrayList<Map<String, Object>>();
										posts.add(fileContents);
										postsByTags.put(tag, posts);
									}
								}
							}
 
							if (fileContents.get("status").equals("published-date")) {
								if (fileContents.get("date") != null && (fileContents.get("date") instanceof Date)) {
									if (new Date().after((Date)fileContents.get("date"))) {
										fileContents.put("status", "published");
									}
								}
							}
						}
						System.out.println("done!");
					}
				} 
				
				if (contents[i].isDirectory()) {
					crawl(contents[i]);
				}
			}
		}
	}

	public List<Map<String, Object>> getPages() {
		return pages;
	}

	public void setPages(List<Map<String, Object>> pages) {
		this.pages = pages;
	}

	public List<Map<String, Object>> getPosts() {
		return posts;
	}

	public void setPosts(List<Map<String, Object>> posts) {
		this.posts = posts;
	}

	public Map<String, List<Map<String, Object>>> getPostsByTags() {
		return postsByTags;
	}

	public void setPostsByTags(Map<String, List<Map<String, Object>>> postsByTags) {
		this.postsByTags = postsByTags;
	}
}
