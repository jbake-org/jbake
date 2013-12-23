package org.jbake.app;

import java.io.File;
import java.util.Date;
import java.util.Map;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import org.apache.commons.configuration.CompositeConfiguration;
import org.jbake.model.DocumentTypes;

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
    private boolean isClearCache;

	/**
	 * Creates a new instance of the Oven.
	 *
	 */
	public Oven() {
	}
	
	/**
	 * Creates a new instance of the Oven with references to the source and destination folders.
	 *
	 * @param source		The source folder
	 * @param destination	The destination folder
	 * @throws Exception
	 */
	public Oven(File source, File destination, boolean isClearCache) throws Exception {
		this.source = source;
		this.destination = destination;
        this.config = ConfigUtil.load(source);
        this.isClearCache = isClearCache;
	}

    private void ensureSource() throws Exception {
        if (!FileUtil.isExistingFolder(source)) {
            throw new Exception("Error: Source folder must exist!");
        }
        if (!source.canRead()) {
            throw new Exception("Error: Source folder is not readable!");
        }
    }


    private void ensureDestination() throws Exception {
        if (null == destination) {
            destination = new File(config.getString("destination.folder"));
        }
        if (!destination.exists()) {
            destination.mkdirs();
        }
        if (!destination.canWrite()) {
            throw new Exception("Error: Destination folder is not writable!");
        }
    }

	/**
	 * Checks source path contains required sub-folders (i.e. templates) and setups up variables for them.
	 *
	 * @throws Exception If template or contents folder don't exist
	 */
	public void setupPaths() throws Exception {
		ensureSource();
        templatesPath = setupRequiredFolderFromConfig("template.folder");
        contentsPath = setupRequiredFolderFromConfig("content.folder");
        assetsPath = setupPathFromConfig("asset.folder");
		if (!assetsPath.exists()) {
			System.out.println("Warning: No asset folder was found!");
		}
		ensureDestination();
	}

    private File setupPathFromConfig(String key) {
        return new File(source, config.getString(key));
    }

    private File setupRequiredFolderFromConfig(String key) throws Exception {
        File path = setupPathFromConfig(key);
        if (!FileUtil.isExistingFolder(path)) {
            throw new Exception("Error: Required folder cannot be found! Expected to find [" + key + "] at: " + path.getCanonicalPath());
        }
        return path;
    }

	/**
	 * All the good stuff happens in here...
	 *
	 * @throws Exception
	 */
	public void bake() throws Exception {
        ODatabaseDocumentTx db = DBUtil.createDB(config.getString("db.store"), config.getString("db.path"));
        try {
            long start = new Date().getTime();
            System.out.println("Baking has started...");
            clearCacheIfNeeded(db);

            // process source content
            Crawler crawler = new Crawler(db, source, config);
            crawler.crawl(contentsPath);
            System.out.println("Pages : " + crawler.getPageCount());
            System.out.println("Posts : " + crawler.getPostCount());

            Renderer renderer = new Renderer(db, destination, templatesPath, config);

            int renderedCount = 0;
            int errorCount = 0;

            DocumentIterator pagesIt = DBUtil.fetchDocuments(db, "select * from page where rendered=false");
            while (pagesIt.hasNext()) {
                Map<String, Object> page = pagesIt.next();
                try {
                    renderer.render(page);
                    renderedCount++;
                } catch (Exception e) {
                    errorCount++;
                }
            }

            DocumentIterator postIt = DBUtil.fetchDocuments(db,"select * from post where rendered=false");
            while (postIt.hasNext()) {
                Map<String, Object> post =  postIt.next();
                try {
                    renderer.render(post);
                    renderedCount++;
                } catch (Exception e) {
                    errorCount++;
                }
            }

            // write index file
            if (config.getBoolean("render.index")) {
                renderer.renderIndex(config.getString("index.file"));
            }

            // write feed file
            if (config.getBoolean("render.feed")) {
                renderer.renderFeed(config.getString("feed.file"));
            }

            // write sitemap file
            if (config.getBoolean("render.sitemap")) {
                renderer.renderSitemap(config.getString("sitemap.file"));
            }

            // write master archive file
            if (config.getBoolean("render.archive")) {
                renderer.renderArchive(config.getString("archive.file"));
            }

            // write tag files
            if (config.getBoolean("render.tags")) {
                renderer.renderTags(crawler.getPostsByTags(), config.getString("tag.path"));
            }

            // mark docs as rendered
            for (String docType : DocumentTypes.getDocumentTypes()) {
                DBUtil.update(db, "update "+docType+" set rendered=true where rendered=false");
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
        } finally {
            db.close();
        }
    }

    private void clearCacheIfNeeded(final ODatabaseDocumentTx db) {
        if (isClearCache) {
            for (String docType : DocumentTypes.getDocumentTypes()) {
                DBUtil.update(db,"delete from "+docType);
            }
        }
    }
}
