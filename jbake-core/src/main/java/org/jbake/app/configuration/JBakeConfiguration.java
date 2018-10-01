package org.jbake.app.configuration;

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
     * Get property value by a given key from the configuration
     *
     * @param key a key for the property like site.host
     * @return the value of the property
     */
    Object get(String key);

    /**
     * @return Output filename for archive file, is only used when {@link #getRenderArchive()} is true
     */
    String getArchiveFileName();

    /**
     * @return attributes to be set when processing input
     */
    List<String> getAsciidoctorAttributes();

    /**
     * Get an asciidoctor option by it's key
     *
     * @param optionKey an option key
     * @return the value of the option key
     */
    Object getAsciidoctorOption(String optionKey);

    /**
     * Get a list of asciidoctor options
     *
     * @return list of asciidoctor options
     */
    List<String> getAsciidoctorOptionKeys();

    /**
     * @return the folder where assets are stored, they are copied directly in output folder and not processed
     */
    File getAssetFolder();

    /**
     * @return name of folder for assets
     */
    String getAssetFolderName();

    /**
     * @return Flag indicating if hidden asset resources should be ignored
     */
    boolean getAssetIgnoreHidden();

    /**
     * @return Prefix to be used when exporting JBake properties to Asciidoctor
     */
    String getAttributesExportPrefixForAsciidoctor();

    /**
     * @return Timestamp that records when JBake build was made
     */
    String getBuildTimeStamp();

    /**
     * @return Flag indicating to flash the database cache
     */
    boolean getClearCache();

    /**
     * @return the content folder
     */
    File getContentFolder();

    /**
     * @return name of Folder where content (that's to say files to be transformed) resides in
     */
    String getContentFolderName();

    /**
     * @return Folder to store database files in
     */
    String getDatabasePath();

    /**
     * @return name to identify if database is kept in memory (memory) or persisted to disk (plocal)
     */
    String getDatabaseStore();

    /**
     * @return How date is formated
     */
    String getDateFormat();

    /**
     * @return Default status to use (in order to avoid putting it in all files)
     */
    String getDefaultStatus();

    /**
     * @return Default type to use (in order to avoid putting it in all files)
     */
    String getDefaultType();

    /**
     * @return The destination folder to render and copy files to
     */
    File getDestinationFolder();

    void setDestinationFolder(File destination);

    List<String> getDocumentTypes();

    /**
     * @return Suffix used to identify draft files
     */
    String getDraftSuffix();

    /**
     * Get name for example project name by given template type
     *
     * @param templateType a template type
     * @return example project name
     */
    String getExampleProjectByType(String templateType);

    /**
     * @return Flag indicating if JBake properties should be made available to Asciidoctor
     */
    boolean getExportAsciidoctorAttributes();

    /**
     * @return Output filename for feed file, is only used when {@link #getRenderFeed()} der} is true
     */
    String getFeedFileName();


    /**
     * @return String used to separate the header from the body
     */
    String getHeaderSeparator();

    /**
     * @return Output filename for index, is only used when {@link #getRenderIndex()} is true
     */
    String getIndexFileName();

    /**
     * Get an iterator of available configuration keys
     *
     * @return an iterator of configuration keys
     */
    Iterator<String> getKeys();

    /**
     * A list of markdown extensions
     * <p>
     * <code>markdown.extension=HARDWRAPS,AUTOLINKS,FENCED_CODE_BLOCKS,DEFINITIONS</code>
     *
     * @return list of markdown extensions as string
     */
    List<String> getMarkdownExtensions();

    /**
     * @return file extension to be used for all output files
     */
    String getOutputExtension();

    String getOutputExtensionByDocType(String docType);

    /**
     * @return Flag indicating if there should be pagination when rendering index
     */
    boolean getPaginateIndex();

    /**
     * @return How many posts per page on index
     */
    int getPostsPerPage();

    /**
     * @return URI prefix for content that should be given extension-less output URI's
     */
    String getPrefixForUriWithoutExtension();

    /**
     * @return Flag indicating if archive file should be generated
     */
    boolean getRenderArchive();

    /**
     * @return Encoding used when rendering files
     */
    String getRenderEncoding();

    /**
     * @return Flag indicating if feed file should be generated
     */
    boolean getRenderFeed();

    /**
     * @return Flag indicating if index file should be generated
     */
    boolean getRenderIndex();

    /**
     * @return Flag indicating if sitemap file should be generated
     */
    boolean getRenderSiteMap();

    /**
     * @return Flag indicating if tag files should be generated
     */
    boolean getRenderTags();

    /**
     * @return Flag indicating if tag index file should be generated
     */
    boolean getRenderTagsIndex();

    /**
     * @return Flag indicating if the tag value should be sanitized
     */
    boolean getSanitizeTag();

    /**
     * @return Port used when running Jetty server
     */
    int getServerPort();

    /**
     * @return the host url of the site e.g. http://jbake.org
     */
    String getSiteHost();

    /**
     * @return Sitemap template file name. Used only when {@link #getRenderSiteMap()} is set to true
     */
    String getSiteMapFileName();

    /**
     * @return the source folder of the project
     */
    File getSourceFolder();

    /**
     * @return Tags output path, used only when {@link #getRenderTags()} is true
     */
    String getTagPathName();

    /**
     * @return Encoding to be used for template files
     */
    String getTemplateEncoding();

    File getTemplateFileByDocType(String masterindex);

    /**
     * @return the template folder
     */
    File getTemplateFolder();

    /**
     * @return name of folder where template files are looked for
     */
    String getTemplateFolderName();

    /**
     * @return Locale used for Thymeleaf template rendering
     */
    String getThymeleafLocale();

    /**
     * @return Flag indicating if content matching prefix below should be given extension-less URI's
     */
    boolean getUriWithoutExtension();

    /**
     * @return Flag indicating if image paths should be prepended with {@link #getSiteHost()} value - only has an effect if
     * {@link #getImgPathUpdate()} is set to true
     */
    boolean getImgPathPrependHost();

    /**
     * @return Flag indicating if image paths in content should be updated with absolute path (using URI value of content file),
     * see {@link #getImgPathUpdate()} which allows you to control the absolute path used
     */
    boolean getImgPathUpdate();

    /**
     * @return Version of JBake
     */
    String getVersion();

    /**
     * Set a property value for the given key
     *
     * @param key   the key for the property
     * @param value the value of the property
     */
    void setProperty(String key, Object value);
}

