package org.jbake.app.configuration

import java.io.File
import java.util.*

/**
 * JBakeConfiguration gives you access to the project configuration. Typically located in a file called jbake.properties.
 *
 * Use one of [JBakeConfigurationFactory] methods to create an instance.
 */
interface JBakeConfiguration {
    /**
     * Get property value by a given key from the configuration.
     * @param key a key for the property like `site.host`
     */
    fun get(key: String): Any?

    /**
     * @return Output filename for archive file, is only used when [.getRenderArchive] is true
     */
    val archiveFileName: String?

    /**
     * @return attributes to be set when processing input
     */
    val asciidoctorAttributes: MutableList<String>

    /**
     * Get an asciidoctor option by its key.
     * @param optionKey an option key
     */
    fun getAsciidoctorOption(optionKey: String): Any?

    /**
     * Get a list of asciidoctor options
     */
    val asciidoctorOptionKeys: MutableList<String>

    /**
     * @return the folder where assets are stored, they are copied directly in output folder and not processed
     */
    val assetFolder: File

    /**
     * @return name of folder for assets
     */
    val assetFolderName: String?

    /**
     * @return Flag indicating if hidden asset resources should be ignored
     */
    val assetIgnoreHidden: Boolean

    /**
     * @return Prefix to be used when exporting JBake properties to Asciidoctor
     */
    val attributesExportPrefixForAsciidoctor: String?

    /**
     * @return Timestamp that records when JBake build was made
     */
    val buildTimeStamp: String?

    /**
     * @return Flag indicating to flash the database cache
     */
    val clearCache: Boolean

    /**
     * @return the content folder
     */
    val contentFolder: File

    /**
     * @return name of Folder where content (that's to say files to be transformed) resides in
     */
    val contentFolderName: String?

    /**
     * @return the data folder
     */
    val dataFolder: File

    /**
     * @return name of Folder where data files reside in
     */
    val dataFolderName: String?

    /**
     * @return docType for data files
     */
    val dataFileDocType: String

    /**
     * @return Folder to store database files in
     */
    val databasePath: String?

    /**
     * @return name to identify if database is kept in memory (memory) or persisted to disk (plocal)
     */
    val databaseStore: String

    /**
     * @return How date is formated
     */
    val dateFormat: String?

    /**
     * @return Default status to use (in order to avoid putting it in all files)
     */
    val defaultStatus: String?

    /**
     * @return Default type to use (in order to avoid putting it in all files)
     */
    val defaultType: String?

    /**
     * @return The destination folder to render and copy files to
     */
    var destinationFolder: File

    val documentTypes: MutableList<String>

    /**
     * @return Suffix used to identify draft files
     */
    val draftSuffix: String?

    /**
     * @return Output filename for error404 file, is only used when [.getRenderError404] is true
     */
    val error404FileName: String?

    /**
     * Get name for example project name by given template type
     *
     * @param templateType a template type
     * @return example project name
     */
    fun getExampleProjectByType(templateType: String): String?

    /**
     * @return Flag indicating if JBake properties should be made available to Asciidoctor
     */
    val exportAsciidoctorAttributes: Boolean

    /**
     * @return Output filename for feed file, is only used when [.getRenderFeed] is true
     */
    val feedFileName: String?


    /**
     * @return String used to separate the header from the body
     */
    val headerSeparator: String?

    /**
     * @return Filename to use to ignore a directory in addition to ".jbakeignore"
     */
    val ignoreFileName: String?

    /**
     * @return Output filename for index, is only used when [.getRenderIndex] is true
     */
    val indexFileName: String?

    /**
     * Get an iterator of available configuration keys
     */
    val keys: MutableIterator<String>

    /**
     * A list of markdown extensions
     * `markdown.extension=HARDWRAPS,AUTOLINKS,FENCED_CODE_BLOCKS,DEFINITIONS`
     */
    val markdownExtensions: MutableList<String>

    /**
     * @return file extension to be used for all output files
     */
    val outputExtension: String?

    fun getOutputExtensionByDocType(docType: String): String?

    /**
     * @return Flag indicating if there should be pagination when rendering index
     */
    val paginateIndex: Boolean

    /**
     * @return How many posts per page on index
     */
    val postsPerPage: Int

    /**
     * @return URI prefix for content that should be given extension-less output URI's
     */
    val prefixForUriWithoutExtension: String?

    /**
     * @return Flag indicating if archive file should be generated
     */
    val renderArchive: Boolean

    /**
     * @return Encoding used when rendering files
     */
    val renderEncoding: String?

    /**
     * @return Output encoding for freemarker url escaping
     */
    val outputEncoding: String?

    /**
     * @return Flag indicating if error404 file should be generated
     */
    val renderError404: Boolean

    /**
     * @return Flag indicating if feed file should be generated
     */
    val renderFeed: Boolean

    /**
     * @return Flag indicating if index file should be generated
     */
    val renderIndex: Boolean

    /**
     * @return Flag indicating if sitemap file should be generated
     */
    val renderSiteMap: Boolean

    /**
     * @return Flag indicating if tag files should be generated
     */
    val renderTags: Boolean

    /**
     * @return Flag indicating if tag index file should be generated
     */
    val renderTagsIndex: Boolean

    /**
     * @return Flag indicating if the tag value should be sanitized
     */
    val sanitizeTag: Boolean

    /**
     * @return Port used when running Jetty server
     */
    val serverPort: Int

    /**
     * @return the host url of the site e.g. http://jbake.org
     */
    val siteHost: String?

    /**
     * @return Sitemap template file name. Used only when [.getRenderSiteMap] is set to true
     */
    val siteMapFileName: String?

    /**
     * @return the source folder of the project
     * /// TODO: This is nullable - see Main.kt around line 140.
     */
    val sourceFolder: File?

    /**
     * @return Tags output path, used only when [.getRenderTags] is true
     */
    val tagPathName: String?

    /**
     * @return Encoding to be used for template files
     */
    val templateEncoding: String?

    fun getTemplateByDocType(doctype: String): String?

    fun getTemplateFileByDocType(doctype: String): File?

    /**
     * @return the template folder
     */
    val templateFolder: File

    /**
     * @return name of folder where template files are looked for
     */
    val templateFolderName: String?

    /**
     * @return Locale used for Thymeleaf template rendering
     */
    val thymeleafLocale: String?

    /**
     * @return Flag indicating if content matching prefix below should be given extension-less URI's
     */
    val uriWithoutExtension: Boolean

    /**
     * @return Flag indicating if image paths should be prepended with [.getSiteHost] value - only has an effect if
     * [.getImgPathUpdate] is set to true
     */
    val imgPathPrependHost: Boolean

    /**
     * @return Flag indicating if image paths in content should be updated with absolute path (using URI value of content file),
     * see [.getImgPathUpdate] which allows you to control the absolute path used
     */
    val imgPathUpdate: Boolean

    /**
     * @return Version of JBake
     */
    val version: String?

    /**
     * Set a property value for the given key
     *
     * @param key   the key for the property
     * @param value the value of the property
     */
    fun setProperty(key: String, value: Any?)

    /**
     *
     * @param type the documents type
     * @return the thymeleaf render mode ( defaults to [DefaultJBakeConfiguration.DEFAULT_TYHMELEAF_TEMPLATE_MODE] )
     */
    fun getThymeleafModeByType(type: String): String

    val serverContextPath: String

    val serverHostname: String

    /**
     * @return Abbreviated hash of latest git commit
     */
    val abbreviatedGitHash: String?

    /**
     * @return Locale to set in the JVM
     */
    val jvmLocale: String?

    /**
     *
     * @return TimeZone to use within Freemarker
     */
    val freemarkerTimeZone: TimeZone

    fun asHashMap(): MutableMap<String, Any>

    val jbakeProperties: MutableList<Property>

    fun addConfiguration(properties: Properties)
}
