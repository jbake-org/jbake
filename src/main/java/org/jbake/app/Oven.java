package org.jbake.app;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FileUtils;

import static org.jbake.app.SortUtil.REVERSE;

/**
 * All the baking happens in the Oven!
 *
 * @author Jonathan Bullock <jonbullock@gmail.com>
 *
 */
public class Oven {

	private CompositeConfiguration config;
	private File source;
	private File destination;
	private File templatesPath;
	private File contentsPath;
	private File assetsPath;

	/**
	 * Creates a new instance of the Oven with references to the source and destination folders.
	 *
	 * @param source		The source folder
	 * @param destination	The destination folder
	 * @throws ConfigurationException
	 */
	public Oven(File source, File destination) throws ConfigurationException {
		this.source = source;
		this.destination = destination;
		this.config = new CompositeConfiguration();
		File customConfig = new File(source.getPath()+File.separator+"custom.properties");
		if (customConfig.exists()) {
			config.addConfiguration(new PropertiesConfiguration(customConfig));
		}
		config.addConfiguration(new PropertiesConfiguration("default.properties"));
	}

	/**
	 * Checks source path contains required sub-folders (i.e. templates) and setups up variables for them.
	 *
	 * @throws Exception If template or contents folder don't exist
	 */
	public void setupPaths() throws Exception {
		templatesPath = new File(source.getPath() + File.separator + config.getString("template.folder"));
		if (!templatesPath.exists()) {
			throw new Exception("Error: Required template folder cannot be found!");
		}
		contentsPath = new File(source.getPath() + File.separator + config.getString("content.folder"));
		if (!contentsPath.exists()) {
			throw new Exception("Error: Required content folder cannot be found!");
		}
		assetsPath = new File(source.getPath() + File.separator + config.getString("asset.folder"));
		if (!assetsPath.exists()) {
			System.out.println("Warning: No asset folder was found!");
		}
	}

	/**
	 * All the good stuff happens in here...
	 *
	 * @throws Exception
	 */
	public void bake() throws Exception {
		long start = new Date().getTime();
		System.out.println("Baking has started...");

		// process source content
		Crawler crawler = new Crawler(source, config);
		crawler.crawl(contentsPath);
		List<Map<String, Object>> pages = crawler.getPages();
		List<Map<String, Object>> posts = crawler.getPosts();

		// sort posts
		Collections.sort(posts, SortUtil.getComparator(REVERSE));

		Renderer renderer = new Renderer(source, destination, templatesPath, config, posts, pages);

		int renderedCount = 0;
		int errorCount = 0;
		
		// render all pages
		for (Map<String, Object> page : pages) {
			// TODO: could add check here to see if rendering needs to be done again
			try {
				renderer.render(page);
				renderedCount++;
			} catch (Exception e) {
				errorCount++;
			}
		}

		// render all posts
		for (Map<String, Object> post : posts) {
			// TODO: could add check here to see if rendering needs to be done again
			try {
				renderer.render(post);
				renderedCount++;
			} catch (Exception e) {
				errorCount++;
			}
		}

		// only interested in published posts from here on
		List<Map<String, Object>> publishedPosts = Filter.getPublishedPosts(posts);

		// write index file
		if (config.getBoolean("render.index")) {
			renderer.renderIndex(publishedPosts, config.getString("index.file"));
		}

		// write feed file
		if (config.getBoolean("render.feed")) {
			renderer.renderFeed(publishedPosts, config.getString("feed.file"));
		}

		// write master archive file
		if (config.getBoolean("render.archive")) {
			renderer.renderArchive(publishedPosts, config.getString("archive.file"));
		}

		// write tag files 
		if (config.getBoolean("render.tags")) {
			renderer.renderTags(crawler.getPostsByTags(), config.getString("tag.path"));
		}

		// copy assets
		Asset asset = new Asset(source, destination);
		asset.copy(assetsPath);

		System.out.println("...finished!");
		long end = new Date().getTime();
		System.out.println("Baked " + renderedCount + " items in " + (end-start) + "ms");
		if (errorCount > 0) {
			System.out.println("Failed to bake " + errorCount + " item(s)!");
		}
//		System.out.println("Baking took: " + (end-start) + "ms");
	}
}
