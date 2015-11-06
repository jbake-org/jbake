package org.jbake.app;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Provides Configuration related functions.
 *
 * @author Jonathan Bullock <jonbullock@gmail.com>
 */
public class ConfigUtil {

    /**
     * Set of config keys used by JBake
     *
     * @author ndx
     */
    public interface Keys {

        /**
         * Output filename for archive file, is only used when {@link #RENDER_ARCHIVE} is true
         */
        String ARCHIVE_FILE = "archive.file";

        /**
         * Asciidoctor attributes to be set when processing input
         */
        String ASCIIDOCTOR_ATTRIBUTES = "asciidoctor.attributes";

        /**
         * Flag indicating if JBake properties should be made available to Asciidoctor
         */
        String ASCIIDOCTOR_ATTRIBUTES_EXPORT = "asciidoctor.attributes.export";

        /**
         * Prefix to be used when exporting JBake properties to Asciidoctor
         */
        String ASCIIDOCTOR_ATTRIBUTES_EXPORT_PREFIX = "asciidoctor.attributes.export.prefix";

        /**
         * Asciidoctor options to be set when processing input
         */
        String ASCIIDOCTOR_OPTION = "asciidoctor.option";

        /**
         * Folder where assets are stored, they are copied directly in output folder and not processed
         */
        String ASSET_FOLDER = "asset.folder";

        /**
         * Flag indicating if hidden asset resources should be ignored
         */
        String ASSET_IGNORE_HIDDEN = "asset.ignore";

        /**
         * Timestamp that records when JBake build was made
         */
        String BUILD_TIMESTAMP = "build.timestamp";

        /**
         * Folder where content (that's to say files to be transformed) resides in
         */
        String CONTENT_FOLDER = "content.folder";

        /**
         * How date is formated
         */
        String DATE_FORMAT = "date.format";

        /**
         * Folder to store database files in
         */
        String DB_PATH = "db.path";

        /**
         * Flag to identify if database is kept in memory (memory) or persisted to disk (local)
         */
        String DB_STORE = "db.store";

        /**
         * Default status to use (in order to avoid putting it in all files)
         */
        String DEFAULT_STATUS = "default.status";

        /**
         * Folder where rendered files are output
         */
        String DESTINATION_FOLDER = "destination.folder";

        /**
         * Suffix used to identify draft files
         */
        String DRAFT_SUFFIX = "draft.suffix";

        /**
         * Output filename for feed file, is only used when {@link #RENDER_FEED} is true
         */
        String FEED_FILE = "feed.file";

        /**
         * Output filename for index, is only used when {@link #RENDER_INDEX} is true
         */
        String INDEX_FILE = "index.file";

        /**
         * File extension to be used for all output files
         */
        String OUTPUT_EXTENSION = "output.extension";

        /**
         * Flag indicating if archive file should be generated
         */
        String RENDER_ARCHIVE = "render.archive";

        /**
         * Encoding used when rendering files
         */
        String RENDER_ENCODING = "render.encoding";

        /**
         * Flag indicating if feed file should be generated
         */
        String RENDER_FEED = "render.feed";

        /**
         * Flag indicating if index file should be generated
         */
        String RENDER_INDEX = "render.index";

        /**
         * Flag indicating if sitemap file should be generated
         */
        String RENDER_SITEMAP = "render.sitemap";

        /**
         * Flag indicating if tag files should be generated
         */
        String RENDER_TAGS = "render.tags";

        /**
         * Port used when running Jetty server
         */
        String SERVER_PORT = "server.port";

        /**
         * Sitemap template file name. Used only when {@link #RENDER_SITEMAP} is set to true
         */
        String SITEMAP_FILE = "sitemap.file";

        /**
         * Tags output path, used only when {@link #RENDER_TAGS} is true
         */
        String TAG_PATH = "tag.path";

        /**
         * Should the tag value be sanitized?
         */
        String TAG_SANITIZE = "tag.sanitize";

        /**
         * Encoding to be used for template files
         */
        String TEMPLATE_ENCODING = "template.encoding";

        /**
         * Folder where template files are looked for
         */
        String TEMPLATE_FOLDER = "template.folder";

        /**
         * Locale used for Thymeleaf template rendering
         */
        String THYMELEAF_LOCALE = "thymeleaf.locale";

        /**
         * Version of JBake
         */
        String VERSION = "version";

    }

    private final static Logger LOGGER = LoggerFactory.getLogger(ConfigUtil.class);
    private final static String LEGACY_CONFIG_FILE = "custom.properties";
    private final static String CONFIG_FILE = "jbake.properties";
    private final static String DEFAULT_CONFIG_FILE = "default.properties";
    private static boolean LEGACY_CONFIG_FILE_WARNING_SHOWN = false;

    public static CompositeConfiguration load(File source) throws ConfigurationException {
        CompositeConfiguration config = new CompositeConfiguration();
        config.setListDelimiter(',');
        File customConfigFile = new File(source, LEGACY_CONFIG_FILE);
        if (customConfigFile.exists()) {
            if (!LEGACY_CONFIG_FILE_WARNING_SHOWN) {
                LOGGER.warn(String.format("You have defined a part of your JBake configuration in %s located at: %s", LEGACY_CONFIG_FILE, customConfigFile.getParent()));
                LOGGER.warn(String.format("Usage of this file is being deprecated, please rename this file to: %s to remove this warning", CONFIG_FILE));
                LEGACY_CONFIG_FILE_WARNING_SHOWN = true;
            }
            config.addConfiguration(new PropertiesConfiguration(customConfigFile));
        }
        customConfigFile = new File(source, CONFIG_FILE);
        if (customConfigFile.exists()) {
            config.addConfiguration(new PropertiesConfiguration(customConfigFile));
        }
        config.addConfiguration(new PropertiesConfiguration(DEFAULT_CONFIG_FILE));
        return config;
    }
}
