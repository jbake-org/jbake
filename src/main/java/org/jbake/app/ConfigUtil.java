package org.jbake.app;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import java.io.File;

/**
 * Provides Configuration related functions.
 *
 * @author Jonathan Bullock <jonbullock@gmail.com>
 */
public class ConfigUtil {
	/**
	 * Set of config keys used by JBake
	 * @author ndx
	 *
	 */
	public static interface Keys {

		/**
		 * Path of archived file. Should only be used when {@link #RENDER_ARCHIVE} is true.
		 */
		static final String ARCHIVE_FILE = "archive.file";
		static final String ASCIIDOCTOR_ATTRIBUTES = "asciidoctor.attributes";
		static final String ASCIIDOCTOR_ATTRIBUTES_EXPORT = "asciidoctor.attributes.export";
		static final String ASCIIDOCTOR_ATTRIBUTES_EXPORT_PREFIX = "asciidoctor.attributes.export.prefix";
		static final String ASCIIDOCTOR_OPTION = "asciidoctor.option";
		/**
		 * Folder where assets are stored. Assets are copied directly in output folder and not processed by JBake.
		 */
		static final String ASSET_FOLDER = "asset.folder";
		/**
		 * Timestamp that will be output when generation start.
		 */
		static final String BUILD_TIMESTAMP = "build.timestamp";
		/**
		 * Folder where content (that's to say files to be transformed) resides in
		 */
		static final String CONTENT_FOLDER = "content.folder";
		/**
		 * How date is formated
		 */
		final static String DATE_FORMAT = "date.format";
		static final String DB_PATH = "db.path";
		static final String DB_STORE = "db.store";
		/**
		 * Default status to use (in roder to avoid putting it in all files
		 */
		static final String DEFAULT_STATUS = "default.status";
		/**
		 * Path where html files are generated in.
		 */
		static final String DESTINATION_FOLDER = "destination.folder";
		/**
		 * Suffix used to identify draft files
		 */
		static final String DRAFT_SUFFIX = "draft.suffix";
		/**
		 * Feed template file. IS required only when {@link #RENDER_FEED} is set to true
		 */
		static final String FEED_FILE = "feed.file";
		/**
		 * Index template file. Required only when {@link #RENDER_INDEX} is set to true
		 */
		static final String INDEX_FILE = "index.file";
		/**
		 * Common extension for all output files
		 */
		static final String OUTPUT_EXTENSION = "output.extension";
		/**
		 * Flag indicating if archive file should be generated.
		 */
		static final String RENDER_ARCHIVE = "render.archive";
		/**
		 * Encoding used to render files.
		 */
		static final String RENDER_ENCODING = "render.encoding";
		/**
		 * Flag indicating if feed file should be generated.
		 */
		static final String RENDER_FEED = "render.feed";
		/**
		 * Flag indicating if index file should be generated.
		 */
		static final String RENDER_INDEX = "render.index";
		/**
		 * Flag indicating if sitemap file should be generated.
		 */
		static final String RENDER_SITEMAP = "render.sitemap";
		/**
		 * Flag indicating if tags file should be generated.
		 */
		static final String RENDER_TAGS = "render.tags";
		/**
		 * Port used when running Jetty server
		 */
		static final String SERVER_PORT = "server.port";
		/**
		 * Sitemap template file name. Used only when {@link #RENDER_SITEMAP} is set to true
		 */
		static final String SITEMAP_FILE = "sitemap.file";
		/**
		 * tags template file name. Used only when {@link #RENDER_TAGS} is set to true
		 */
		static final String TAG_PATH = "tag.path";
		/**
		 * Ecoding for template files
		 */
		static final String TEMPLATE_ENCODING = "template.encoding";
		/**
		 * Folder where template files are looked for
		 */
		static final String TEMPLATE_FOLDER = "template.folder";
		/**
		 * Locale used for thymeleaf template rendering
		 */
		public static final String THYMELEAF_LOCALE = "thymeleaf.locale";
		static final String VERSION = "version";
		
	}

    public static CompositeConfiguration load(File source) throws ConfigurationException {
        CompositeConfiguration config = new CompositeConfiguration();
        config.setListDelimiter(',');
        File customConfigFile = new File(source, "custom.properties");
        if (customConfigFile.exists()) {
            config.addConfiguration(new PropertiesConfiguration(customConfigFile));
        }
        customConfigFile = new File(source, "jbake.properties");
        if (customConfigFile.exists()) {
            config.addConfiguration(new PropertiesConfiguration(customConfigFile));
        }
        config.addConfiguration(new PropertiesConfiguration("default.properties"));
        return config;
    }
}
