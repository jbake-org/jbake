package org.jbake.app.configuration;

import org.apache.commons.configuration.Configuration;

import java.io.File;
import java.util.Iterator;
import java.util.List;

/**
 * JBakeConfiguration gives you access to the project configuration. Typically located in a file called jbake.properties.
 *
 * Use one of {@link JBakeConfigurationFactory} methods to create an instance.
 */
public interface JBakeConfiguration {

    /**
     * @return the source folder of the project
     */
    File getSourceFolder();

    String DESTINATION_FOLDER = "destination.folder";

    /**
     * @return The destination folder to render and copy files to
     */
    File getDestinationFolder();

    String ASSET_FOLDER = "asset.folder";

    /**
     * @return the folder where assets are stored, they are copied directly in output folder and not processed
     */
    File getAssetFolder();

    /**
     *
     * @return the template folder
     */
    File getTemplateFolder();

    /**
     *
     * @return the content folder
     */
    File getContentFolder();

    String OUTPUT_EXTENSION = "output.extension";

    /**
     * @return file extension to be used for all output files
     */
    String getOutputExtension();

    File getTemplateFileByDocType(String masterindex);

    String getOutputExtensionByDocType(String docType);

    String PAGINATE_INDEX = "index.paginate";

    /**
     * @return Flag indicating if there should be pagination when rendering index
     */
    boolean getPaginateIndex();

    String ASSET_IGNORE_HIDDEN = "asset.ignore";

    /**
     * @return Flag indicating if hidden asset resources should be ignored
     */
    boolean getAssetIgnoreHidden();

    String URI_NO_EXTENSION = "uri.noExtension";

    /**
     * @return Flag indicating if content matching prefix below should be given extension-less URI's
     */
    boolean getUriWithoutExtension();

    String TAG_SANITIZE = "tag.sanitize";

    /**
     * @return Flag indicating if the tag value should be sanitized
     */
    boolean getSanitizeTag();

    String RENDER_ARCHIVE = "render.archive";

    /**
     * @return Flag indicating if archive file should be generated
     */
    boolean getRenderArchive();

    String RENDER_FEED = "render.feed";

    /**
     * @return Flag indicating if feed file should be generated
     */
    boolean getRenderFeed();

    String RENDER_INDEX = "render.index";

    /**
     * @return Flag indicating if index file should be generated
     */
    boolean getRenderIndex();

    String RENDER_SITEMAP = "render.sitemap";

    /**
     * @return Flag indicating if sitemap file should be generated
     */
    boolean getRenderSiteMap();

    String RENDER_TAGS = "render.tags";

    /**
     * @return Flag indicating if tag files should be generated
     */
    boolean getRenderTags();

    String CLEAR_CACHE="db.clear.cache";

    /**
     * @return Flag indicating to flash the database cache
     */
    boolean getClearCache();

    String DRAFT_SUFFIX = "draft.suffix";

    /**
     * @return Suffix used to identify draft files
     */
    String getDraftSuffix();

    String RENDER_ENCODING = "render.encoding";

    /**
     * @return Encoding used when rendering files
     */
    String getRenderEncoding();

    String TEMPLATE_ENCODING = "template.encoding";

    /**
     * @return Encoding to be used for template files
     */
    String getTemplateEncoding();

    String THYMELEAF_LOCALE = "thymeleaf.locale";

    /**
     * @return Locale used for Thymeleaf template rendering
     */
    String getThymeleafLocale();

    String VERSION = "version";

    /**
     * @return Version of JBake
     */
    String getVersion();

    String URI_NO_EXTENSION_PREFIX = "uri.noExtension.prefix";

    /**
     * @return URI prefix for content that should be given extension-less output URI's
     */
    String getPrefixForUriWithoutExtension();

    String DEFAULT_STATUS = "default.status";

    /**
     * @return Default status to use (in order to avoid putting it in all files)
     */
    String getDefaultStatus();

    String DEFAULT_TYPE = "default.type";

    /**
     * @return Default type to use (in order to avoid putting it in all files)
     */
    String getDefaultType();

    String DATE_FORMAT = "date.format";

    /**
     * @return How date is formated
     */
    String getDateFormat();

    String ARCHIVE_FILE = "archive.file";

    /**
     * @return Output filename for archive file, is only used when {@link #RENDER_ARCHIVE} is true
     */
    String getArchiveFileName();

    String FEED_FILE = "feed.file";

    /**
     * @return Output filename for feed file, is only used when {@link #RENDER_FEED} is true
     */
    String getFeedFileName();

    String INDEX_FILE = "index.file";

    /**
     * @return Output filename for index, is only used when {@link #RENDER_INDEX} is true
     */
    String getIndexFileName();

    String SITEMAP_FILE = "sitemap.file";

    /**
     * @return Sitemap template file name. Used only when {@link #RENDER_SITEMAP} is set to true
     */
    String getSiteMapFileName();

    String TAG_PATH = "tag.path";

    /**
     * @return Tags output path, used only when {@link #RENDER_TAGS} is true
     */
    String getTagPathName();

    String BUILD_TIMESTAMP = "build.timestamp";

    /**
     * @return Timestamp that records when JBake build was made
     */
    String getBuildTimeStamp();

    String TEMPLATE_FOLDER = "template.folder";

    /**
     * @return name of folder where template files are looked for
     */
    String getTemplateFolderName();

    String CONTENT_FOLDER = "content.folder";

    /**
     * @return name of Folder where content (that's to say files to be transformed) resides in
     */
    String getContentFolderName();

    /**
     * @return name of folder for assets
     */
    String getAssetFolderName();

    /**
     * Get name for example project name by given template type
     * @param templateType a template type
     * @return example project name
     */
    String getExampleProjectByType(String templateType);

    String DB_STORE = "db.store";

    /**
     * @return name to identify if database is kept in memory (memory) or persisted to disk (plocal)
     */
    String getDatabaseStore();

    String DB_PATH = "db.path";

    /**
     * @return Folder to store database files in
     */
    String getDatabasePath();

    String SERVER_PORT = "server.port";

    /**
     * @return Port used when running Jetty server
     */
    int getServerPort();

    String POSTS_PER_PAGE = "index.posts_per_page";

    /**
     * @return How many posts per page on index
     */
    int getPostsPerPage();

    String MARKDOWN_MAX_PARSING_TIME="markdown.maxParsingTimeInMillis";

    /**
     * Maximum markddwn parsing time in milliseconds
     * @param defaultMaxParsingTime a default value to use
     * @return parsing time in milliseconds
     */
    long getMarkdownMaxParsingTime(long defaultMaxParsingTime);

    String MARKDOWN_EXTENSIONS = "markdown.extensions";

    /**
     * A list of markdown extensions
     *
     * <code>markdown.extension=HARDWRAPS,AUTOLINKS,FENCED_CODE_BLOCKS,DEFINITIONS</code>
     * @return list of markdown extensions as string
     */
    List<String> getMarkdownExtensions();

    String ASCIIDOCTOR_ATTRIBUTES = "asciidoctor.attributes";

    /**
     * @return attributes to be set when processing input
     */
    List<String> getAsciidoctorAttributes();

    List<String> getDocumentTypes();

    String ASCIIDOCTOR_ATTRIBUTES_EXPORT = "asciidoctor.attributes.export";

    /**
     * @return Flag indicating if JBake properties should be made available to Asciidoctor
     */
    boolean getExportAsciidoctorAttributes();

    String ASCIIDOCTOR_ATTRIBUTES_EXPORT_PREFIX = "asciidoctor.attributes.export.prefix";

    /**
     * @return Prefix to be used when exporting JBake properties to Asciidoctor
     */
    String getAttributesExportPrefixForAsciidoctor();

    String SITE_HOST = "site.host";

    /**
     * @return the host url of the site e.g. http://jbake.org
     */
    String getSiteHost();

    /**
     * Get property value by a given key from the configuration
     * @param key a key for the property like site.host
     * @return the value of the property
     */
    Object get(String key);

    String RENDER_TAGS_INDEX = "render.tagsindex";

    /**
     * Flag indicating if tag index file should be generated
     */
    boolean getRenderTagsIndex();

    void setDestinationFolder(File destination);

    /**
     * Set a property value for the given key
     * @param key the key for the property
     * @param value the value of the property
     */
    void setProperty(String key, Object value);

    /**
     * Asciidoctor options to be set when processing input
     */
    //TODO: find usage and create appropriate getter
    String ASCIIDOCTOR_OPTION = "asciidoctor.option";

    /**
     * Get an iterator of available configuration keys
     * @return an iterator of configuration keys
     */
    Iterator<String> getKeys();

    /**
     * Get a list of asciidoctor options
     *
     * @return list of asciidoctor options
     */
    List<String> getAsciidoctorOptionKeys();

    /**
     * Get an asciidoctor option by it's key
     * @param optionKey an option key
     * @return the value of the option key
     */
    Object getAsciidoctorOption(String optionKey);

    String HEADER_SEPARATOR = "header.separator";
    String getHeaderSeparator();
}

