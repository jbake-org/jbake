package org.jbake.app;

import org.apache.commons.configuration.CompositeConfiguration;
import org.jbake.app.ConfigUtil.Keys;
import org.jbake.template.DelegatingTemplateEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Render output to a file.
 *
 * @author Jonathan Bullock <jonbullock@gmail.com>
 */
public class Renderer {
	
	private static interface RenderingConfig {

		File getPath();

		String getName();

		String getTemplate();

		Map<String, Object> getModel();
	}
	
	private static abstract class AbstractRenderingConfig implements RenderingConfig{

		protected final File path;
		protected final String name;
		protected final String template;

		public AbstractRenderingConfig(File path, String name, String template) {
			super();
			this.path = path;
			this.name = name;
			this.template = template;
		}
		
		@Override
		public File getPath() {
			return path;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public String getTemplate() {
			return template;
		}
		
	}
	public static class ModelRenderingConfig extends AbstractRenderingConfig {
		private final Map<String, Object> model;

		public ModelRenderingConfig(File path, String name, Map<String, Object> model, String template) {
			super(path, name, template);
			this.model = model;
		}
		
		@Override
		public Map<String, Object> getModel() {
			return model;
		}
	}
	
	class DefaultRenderingConfig extends AbstractRenderingConfig {

		private final Object content;
		
		private DefaultRenderingConfig(File path, String allInOneName) {
			super(path, allInOneName, findTemplateName(allInOneName));
			this.content = Collections.singletonMap("type",allInOneName);
		}
		
		public DefaultRenderingConfig(String filename, String allInOneName) {
			super(new File(destination.getPath() + File.separator + filename), allInOneName, findTemplateName(allInOneName));
			this.content = Collections.singletonMap("type",allInOneName);
		}
		
		/**
		 * Constructor added due to known use of a allInOneName which is used for name, template and content
		 * @param path
		 * @param allInOneName
		 */
		public DefaultRenderingConfig(String allInOneName) {
			this(new File(destination.getPath() + File.separator + allInOneName + config.getString(Keys.OUTPUT_EXTENSION)), 
							allInOneName);
		}

		@Override
		public Map<String, Object> getModel() {
	        Map<String, Object> model = new HashMap<String, Object>();
	        model.put("renderer", renderingEngine);
	        model.put("content", content);
	        return model;
		}
		
	}

    private final static Logger LOGGER = LoggerFactory.getLogger(Renderer.class);

    // TODO: should all content be made available to all templates via this class??

    private final File destination;
    private final CompositeConfiguration config;
    private final DelegatingTemplateEngine renderingEngine;

    /**
     * Creates a new instance of Renderer with supplied references to folders.
     *
     * @param db            The database holding the content
     * @param destination   The destination folder
     * @param templatesPath The templates folder
     * @param config        
     */
    public Renderer(ContentStore db, File destination, File templatesPath, CompositeConfiguration config) {
        this.destination = destination;
        this.config = config;
        this.renderingEngine = new DelegatingTemplateEngine(config, db, destination, templatesPath);
    }

    private String findTemplateName(String docType) {
        String templateKey = "template."+docType+".file";
		String returned = config.getString(templateKey);
        return returned;
    }

    /**
     * Render the supplied content to a file.
     *
     * @param content The content to renderDocument
     * @throws Exception
     */
    public void render(Map<String, Object> content) throws Exception {
    	String docType = (String) content.get(Crawler.Attributes.TYPE);
        String outputFilename = destination.getPath() + File.separatorChar + (String) content.get(Crawler.Attributes.URI);
        if (outputFilename.lastIndexOf(".") > 0) {
        	outputFilename = outputFilename.substring(0, outputFilename.lastIndexOf("."));
        }

        // delete existing versions if they exist in case status has changed either way
        File draftFile = new File(outputFilename + config.getString(Keys.DRAFT_SUFFIX) + FileUtil.findExtension(config, docType));
        if (draftFile.exists()) {
            draftFile.delete();
        }

        File publishedFile = new File(outputFilename + FileUtil.findExtension(config, docType));
        if (publishedFile.exists()) {
            publishedFile.delete();
        }

        if (content.get(Crawler.Attributes.STATUS).equals(Crawler.Attributes.Status.DRAFT)) {
            outputFilename = outputFilename + config.getString(Keys.DRAFT_SUFFIX);
        }

        File outputFile = new File(outputFilename + FileUtil.findExtension(config,docType));
        StringBuilder sb = new StringBuilder();
        sb.append("Rendering [").append(outputFile).append("]... ");
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("content", content);
        model.put("renderer", renderingEngine);

        try {
            Writer out = createWriter(outputFile);
            renderingEngine.renderDocument(model, findTemplateName(docType), out);
            out.close();
            sb.append("done!");
            LOGGER.info(sb.toString());
        } catch (Exception e) {
            sb.append("failed!");
            LOGGER.error(sb.toString(), e);
            throw new Exception("Failed to render file. Cause: " + e.getMessage());
        }
    }

    private Writer createWriter(File file) throws IOException {
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }

        return new OutputStreamWriter(new FileOutputStream(file), config.getString(ConfigUtil.Keys.RENDER_ENCODING));
    }

    private void render(RenderingConfig renderConfig) throws Exception {
        File outputFile = renderConfig.getPath();
        StringBuilder sb = new StringBuilder();
        sb.append("Rendering ").append(renderConfig.getName()).append(" [").append(outputFile).append("]...");

        try {
            Writer out = createWriter(outputFile);
            renderingEngine.renderDocument(renderConfig.getModel(), renderConfig.getTemplate(), out);
            out.close();
            sb.append("done!");
            LOGGER.info(sb.toString());
        } catch (Exception e) {
            sb.append("failed!");
            LOGGER.error(sb.toString(), e);
            throw new Exception("Failed to render "+renderConfig.getName(), e);
        }
    }
    
    /**
     * Render an index file using the supplied content.
     *
     * @param indexFile The name of the output file
     * @throws Exception 
     */
    public void renderIndex(String indexFile, ContentStore db) throws Exception {
      long totalPosts = db.countClass("post");
      boolean paginate = config.getBoolean(Keys.PAGINATE_INDEX, false);
      int postsPerPage = config.getInt(Keys.POSTS_PER_PAGE, -1);
      int start = 0;

      Map<String, Object> model = new HashMap<String, Object>();
      model.put("renderer", renderingEngine);
      model.put("content", buildSimpleModel("masterindex"));
      if (paginate) {
          db.setLimit(postsPerPage);
      }

      try {
          int page = 1;
          while (start < totalPosts) {
              String fileName = indexFile;

              if (paginate) {
                  db.setStart(start);
                  int index = fileName.lastIndexOf(".");
                  if (page != 1) {
                      String previous = fileName.substring(0, index) +  (page > 2 ? page-1 : "") + 
                              fileName.substring(index);
                      model.put("previousFileName", previous);
                  } else {
                      model.remove("previousFileName");
                  }
                  
                  // If this iteration won't consume the remaining posts, calculate
                  // the next file name
                  if ((start + postsPerPage) < totalPosts) {
                      model.put("nextFileName", fileName.substring(0, index) + (page+1) +
                          fileName.substring(index));
                  } else {
                      model.remove("nextFileName");
                  }
                  // Add page number to file name
                  fileName = fileName.substring(0, index) + (page > 1 ? page : "") +
                          fileName.substring(index);
              }
              render(new DefaultRenderingConfig(fileName, "masterindex"));
              
              if (paginate) {
                  start += postsPerPage;
                  page++;
              } else {
                  break; // TODO: eww
              }
          }
          db.resetPagination();
      } catch (Exception e) {
          throw new Exception("Failed to render index. Cause: " + e.getMessage());
      }
    }

    /**
     * Render an XML sitemap file using the supplied content.
     * @throws Exception 
     *
     * @see <a href="https://support.google.com/webmasters/answer/156184?hl=en&ref_topic=8476">About Sitemaps</a>
     * @see <a href="http://www.sitemaps.org/">Sitemap protocol</a>
     */
    public void renderSitemap(String sitemapFile) throws Exception {
    	render(new DefaultRenderingConfig(sitemapFile, "sitemap"));
    }

    /**
     * Render an XML feed file using the supplied content.
     *
     * @param feedFile The name of the output file
     * @throws Exception 
     */
    public void renderFeed(String feedFile) throws Exception {
    	render(new DefaultRenderingConfig(feedFile, "feed"));
    }

    /**
     * Render an archive file using the supplied content.
     *
     * @param archiveFile The name of the output file
     * @throws Exception 
     */
    public void renderArchive(String archiveFile) throws Exception {
    	render(new DefaultRenderingConfig(archiveFile, "archive"));
    }

    /**
     * Render tag files using the supplied content.
     *
     * @param tags    The content to renderDocument
     * @param tagPath The output path
     * @throws Exception 
     */
    public int renderTags(Set<String> tags, String tagPath) throws Exception {
    	int renderedCount = 0;
    	final List<String> errors = new LinkedList<String>();
        for (String tag : tags) {
            try {
            	Map<String, Object> model = new HashMap<String, Object>();
            	model.put("renderer", renderingEngine);
            	model.put("tag", tag);
            	model.put("content", Collections.singletonMap("type","tag"));

            	tag = tag.trim().replace(" ", "-");
            	File path = new File(destination.getPath() + File.separator + tagPath + File.separator + tag + config.getString(Keys.OUTPUT_EXTENSION));
            	render(new ModelRenderingConfig(path, "tag", model, findTemplateName("tag")));
                renderedCount++;
            } catch (Exception e) {
                errors.add(e.getCause().getMessage());
            }
        }
        if (!errors.isEmpty()) {
        	StringBuilder sb = new StringBuilder();
        	sb.append("Failed to render tags. Cause(s):");
        	for(String error: errors) {
        		sb.append("\n" + error);
        	}
        	throw new Exception(sb.toString());
        } else {
        	return renderedCount;
        }
    }
    
    /**
     * Builds simple map of values, which are exposed when rendering index/archive/sitemap/feed/tags.
     * 
     * @param type
     * @return
     */
    private Map<String, Object> buildSimpleModel(String type) {
    	Map<String, Object> content = new HashMap<String, Object>();
    	content.put("type", type);
    	content.put("rootpath", "");
    	// add any more keys here that need to have a default value to prevent need to perform null check in templates
    	return content;
    }
}
