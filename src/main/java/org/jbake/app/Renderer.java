package org.jbake.app;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import org.apache.commons.configuration.CompositeConfiguration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Render output to a file.
 * 
 * @author Jonathan Bullock <jonbullock@gmail.com>
 *
 */
public class Renderer {

	// TODO: should all content be made available to all templates via this class??
	
	private File destination;
	private Configuration templateCfg;
	private CompositeConfiguration config;
	private List<Map<String, Object>> posts;
	private List<Map<String, Object>> pages;
	
	/**
	 * Creates a new instance of Renderer with supplied references to folders.
	 * 
	 * @param source		The source folder
	 * @param destination	The destination folder
	 * @param templatesPath	The templates folder
	 */
	public Renderer(File source, File destination, File templatesPath, CompositeConfiguration config) {
		this.destination = destination;
		this.config = config;
		templateCfg = new Configuration();
        templateCfg.setDefaultEncoding(config.getString("render.encoding"));
		try {
			templateCfg.setDirectoryForTemplateLoading(templatesPath);
		} catch (IOException e) {
			e.printStackTrace();
		}
		templateCfg.setObjectWrapper(new DefaultObjectWrapper());	
	}
	
	/**
	 * Creates a new instance of Renderer with supplied references to folders.
	 * 
	 * @param source		The source folder
	 * @param destination	The destination folder
	 * @param templatesPath	The templates folder
	 * @param posts			The list of posts
	 * @param pages			The list of pages
	 */
	public Renderer(File source, File destination, File templatesPath, CompositeConfiguration config, List<Map<String, Object>> posts, List<Map<String, Object>> pages) {
		this(source, destination, templatesPath, config);
		this.posts = posts;
		this.pages = pages;
	}
	
	private void render(Map<String, Object> model, String templateFilename, File outputFile) throws Exception {
		model.put("version", config.getString("version"));
		model.put("posts", posts);
		model.put("pages", pages);
		Map<String, Object> configModel = new HashMap<String, Object>();
		Iterator<String> configKeys = config.getKeys();
		while (configKeys.hasNext()) {
			String key = configKeys.next();
			//replace "." in key so you can use dot notation in templates
			configModel.put(key.replace(".", "_"), config.getProperty(key));
		}
		model.put("config", configModel);
		Template template = null;
		template = templateCfg.getTemplate(templateFilename);
		
		if (!outputFile.exists()) {
			outputFile.getParentFile().mkdirs();
			outputFile.createNewFile();
		}

        Writer out = new OutputStreamWriter(new FileOutputStream(outputFile), config.getString("render.encoding"));
		template.process(model, out);
		out.close();
	}
	
	/**
	 * Render the supplied content to a file.
	 * 
	 * @param content	The content to render
	 * @throws Exception 
	 */
	public void render(Map<String, Object> content) throws Exception {
//		String outputFilename = (new File((String)content.get("file")).getPath().replace(source.getPath()+File.separator+"content", destination.getPath()));
		String outputFilename = destination.getPath() + (String)content.get("uri");
		outputFilename = outputFilename.substring(0, outputFilename.lastIndexOf("."));
		
		// delete existing versions if they exist in case status has changed either way
		File draftFile = new File(outputFilename + config.getString("draft.suffix") + config.getString("output.extension"));
		if (draftFile.exists()) {
			draftFile.delete();
		}
		File publishedFile = new File(outputFilename+config.getString("output.extension"));
		if (publishedFile.exists()) {
			publishedFile.delete();
		}
		
		if (content.get("status").equals("draft")) {
			outputFilename = outputFilename + config.getString("draft.suffix");
		}		
		File outputFile = new File(outputFilename+config.getString("output.extension"));
		
		System.out.print("Rendering [" + outputFile + "]... ");		
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("content", content);
		
		try {
			render(model, ((String)content.get("type"))+".ftl", outputFile);
			System.out.println("done!");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("failed!");
			throw new Exception("Failed to render file");
		}
	}
	
	/**
	 * Render an index file using the supplied content. 
	 * 
	 * @param posts		The content to render
	 * @param indexFile	The name of the output file
	 */
	public void renderIndex(List<Map<String, Object>> posts, String indexFile) {
		File outputFile = new File(destination.getPath() + File.separator + indexFile);
		System.out.print("Rendering index [" + outputFile + "]... ");
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("published_posts", posts);
		
		try {
			render(model, config.getString("template.index.file"), outputFile);
			System.out.println("done!");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("failed!");
		}
	}
	
	/**
	 * Render an XML feed file using the supplied content.  
	 * 
	 * @param posts		The content to render
	 * @param feedFile	The name of the output file
	 */
	public void renderFeed(List<Map<String, Object>> posts, String feedFile) {
		File outputFile = new File(destination.getPath() + File.separator + feedFile);
		System.out.print("Rendering feed [" + outputFile + "]... ");
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("published_posts", posts);
		model.put("published_date", new Date());
		
		try {
			render(model, config.getString("template.feed.file"), outputFile);
			System.out.println("done!");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("failed!");
		}
	}

    /**
     * Render an XML sitemap file using the supplied content.
     *
     * @param pages     The combined list of pages and posts to render
     *
     * @see <a href="https://support.google.com/webmasters/answer/156184?hl=en&ref_topic=8476">About Sitemaps</a>
     * @see <a href="http://www.sitemaps.org/">Sitemap protocol</a>
     */
    public void renderSitemap(List<Map<String, Object>> pages, List<Map<String, Object>> posts, String sitemapFile) {
        File outputFile = new File(destination.getPath() + File.separator + sitemapFile);
        System.out.print("Rendering sitemap [" + outputFile + "]... ");

        Map<String, Object> model = new HashMap<String, Object>();
        model.put("published_pages", pages);
        model.put("published_posts", posts);

        try {
            render(model, config.getString("template.sitemap.file"), outputFile);
            System.out.println("done!");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("failed!");
        }
    }

    /**
	 * Render an archive file using the supplied content.   
	 * 
	 * @param posts			The content to render
	 * @param archiveFile	The name of the output file 
	 */
	public void renderArchive(List<Map<String, Object>> posts, String archiveFile) {
		File outputFile = new File(destination.getPath() + File.separator + archiveFile);
		System.out.print("Rendering archive [" + outputFile + "]... ");
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("published_posts", posts);
		
		try {
			render(model, config.getString("template.archive.file"), outputFile);
			System.out.println("done!");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("failed!");
		}
	}
	
	/**
	 * Render tag files using the supplied content. 
	 * 
	 * @param tags		The content to render
	 * @param tagPath	The output path
	 */
	public void renderTags(Map<String, List<Map<String, Object>>> tags, String tagPath) {
		for (String tag : tags.keySet()) {
			Map<String, Object> model = new HashMap<String, Object>();
			model.put("tag", tag);
			// TODO: sort posts here
			List<Map<String, Object>> posts = Filter.getPublishedContent(tags.get(tag));
			model.put("tag_posts", posts);
			
			tag = tag.trim().replace(" ", "-");
			File outputFile = new File(destination.getPath() + File.separator + tagPath + File.separator + tag + config.getString("output.extension"));
			System.out.print("Rendering tags [" + outputFile + "]... ");
			
			try {
				render(model, config.getString("template.tag.file"), outputFile);
				System.out.println("done!");
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("failed!");
			}
		}
	}
}
