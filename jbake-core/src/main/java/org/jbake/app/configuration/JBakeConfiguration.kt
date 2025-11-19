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
     * Get property value by a given key from the configuration
     *
     * @param key a key for the property like site.host
     * @return the value of the property
     */
    fun get(key: String?): Any?

    /**
     * @return Output filename for archive file, is only used when [.getRenderArchive] is true
     */
    //@JvmField
    val archiveFileName: String?

    /**
     * @return attributes to be set when processing input
     */
    ////@JvmField
    val asciidoctorAttributes: MutableList<String>?

    /**
     * Get an asciidoctor option by it's key
     *
     * @param optionKey an option key
     * @return the value of the option key
     */
    fun getAsciidoctorOption(optionKey: String?): Any?

    /**
     * Get a list of asciidoctor options
     *
     * @return list of asciidoctor options
     */
    //@JvmField
    val asciidoctorOptionKeys: MutableList<String?>?

    /**
     * @return the folder where assets are stored, they are copied directly in output folder and not processed
     */
    //@JvmField
    val assetFolder: File

    /**
     * @return name of folder for assets
     */
    //@JvmField
    val assetFolderName: String?

    /**
     * @return Flag indicating if hidden asset resources should be ignored
     */
    //@JvmField
    val assetIgnoreHidden: Boolean

    /**
     * @return Prefix to be used when exporting JBake properties to Asciidoctor
     */
    //@JvmField
    val attributesExportPrefixForAsciidoctor: String?

    /**
     * @return Timestamp that records when JBake build was made
     */
    //@JvmField
    val buildTimeStamp: String?

    /**
     * @return Flag indicating to flash the database cache
     */
    //@JvmField
    val clearCache: Boolean

    /**
     * @return the content folder
     */
    //@JvmField
    val contentFolder: File

    /**
     * @return name of Folder where content (that's to say files to be transformed) resides in
     */
    //@JvmField
    val contentFolderName: String?

    /**
     * @return the data folder
     */
    //@JvmField
    val dataFolder: File?

    /**
     * @return name of Folder where data files reside in
     */
    val dataFolderName: String?

    /**
     * @return docType for data files
     */
    //@JvmField
    val dataFileDocType: String

    /**
     * @return Folder to store database files in
     */
    //@JvmField
    val databasePath: String?

    /**
     * @return name to identify if database is kept in memory (memory) or persisted to disk (plocal)
     */
    //@JvmField
    val databaseStore: String?

    /**
     * @return How date is formated
     */
    //@JvmField
    val dateFormat: String?

    /**
     * @return Default status to use (in order to avoid putting it in all files)
     */
    //@JvmField
    val defaultStatus: String?

    /**
     * @return Default type to use (in order to avoid putting it in all files)
     */
    //@JvmField
    val defaultType: String?

    /**
     * @return The destination folder to render and copy files to
     */
    //@JvmField
    var destinationFolder: File?

    //@JvmField
    val documentTypes: MutableList<String>

    /**
     * @return Suffix used to identify draft files
     */
    //@JvmField
    val draftSuffix: String?

    /**
     * @return Output filename for error404 file, is only used when [.getRenderError404] is true
     */
    //@JvmField
    val error404FileName: String?

    /**
     * Get name for example project name by given template type
     *
     * @param templateType a template type
     * @return example project name
     */
    fun getExampleProjectByType(templateType: String?): String?

    /**
     * @return Flag indicating if JBake properties should be made available to Asciidoctor
     */
    //@JvmField
    val exportAsciidoctorAttributes: Boolean

    /**
     * @return Output filename for feed file, is only used when [.getRenderFeed] is true
     */
    //@JvmField
    val feedFileName: String?


    /**
     * @return String used to separate the header from the body
     */
    //@JvmField
    val headerSeparator: String?

    /**
     * @return Filename to use to ignore a directory in addition to ".jbakeignore"
     */
    //@JvmField
    val ignoreFileName: String?

    /**
     * @return Output filename for index, is only used when [.getRenderIndex] is true
     */
    //@JvmField
    val indexFileName: String?

    /**
     * Get an iterator of available configuration keys
     *
     * @return an iterator of configuration keys
     */
    //@JvmField
    val keys: MutableIterator<String?>?

    /**
     * A list of markdown extensions
     *
     *
     * `markdown.extension=HARDWRAPS,AUTOLINKS,FENCED_CODE_BLOCKS,DEFINITIONS`
     *
     * @return list of markdown extensions as string
     */
    //@JvmField
    val markdownExtensions: MutableList<String?>?

    /**
     * @return file extension to be used for all output files
     */
    //@JvmField
    val outputExtension: String?

    fun getOutputExtensionByDocType(docType: String?): String?

    /**
     * @return Flag indicating if there should be pagination when rendering index
     */
    //@JvmField
    val paginateIndex: Boolean

    /**
     * @return How many posts per page on index
     */
    //@JvmField
    val postsPerPage: Int

    /**
     * @return URI prefix for content that should be given extension-less output URI's
     */
    //@JvmField
    val prefixForUriWithoutExtension: String?

    /**
     * @return Flag indicating if archive file should be generated
     */
    //@JvmField
    val renderArchive: Boolean

    /**
     * @return Encoding used when rendering files
     */
    //@JvmField
    val renderEncoding: String?

    /**
     * @return Output encoding for freemarker url escaping
     */
    //@JvmField
    val outputEncoding: String?

    /**
     * @return Flag indicating if error404 file should be generated
     */
    //@JvmField
    val renderError404: Boolean

    /**
     * @return Flag indicating if feed file should be generated
     */
    //@JvmField
    val renderFeed: Boolean

    /**
     * @return Flag indicating if index file should be generated
     */
    //@JvmField
    val renderIndex: Boolean

    /**
     * @return Flag indicating if sitemap file should be generated
     */
    //@JvmField
    val renderSiteMap: Boolean

    /**
     * @return Flag indicating if tag files should be generated
     */
    //@JvmField
    val renderTags: Boolean

    /**
     * @return Flag indicating if tag index file should be generated
     */
    //@JvmField
    val renderTagsIndex: Boolean

    /**
     * @return Flag indicating if the tag value should be sanitized
     */
    //@JvmField
    val sanitizeTag: Boolean

    /**
     * @return Port used when running Jetty server
     */
    //@JvmField
    val serverPort: Int

    /**
     * @return the host url of the site e.g. http://jbake.org
     */
    //@JvmField
    val siteHost: String?

    /**
     * @return Sitemap template file name. Used only when [.getRenderSiteMap] is set to true
     */
    //@JvmField
    val siteMapFileName: String?

    /**
     * @return the source folder of the project
     */
    //@JvmField
    val sourceFolder: File?

    /**
     * @return Tags output path, used only when [.getRenderTags] is true
     */
    //@JvmField
    val tagPathName: String?

    /**
     * @return Encoding to be used for template files
     */
    //@JvmField
    val templateEncoding: String?

    fun getTemplateByDocType(doctype: String?): String?

    fun getTemplateFileByDocType(doctype: String?): File?

    /**
     * @return the template folder
     */
    //@JvmField
    val templateFolder: File?

    /**
     * @return name of folder where template files are looked for
     */
    //@JvmField
    val templateFolderName: String?

    /**
     * @return Locale used for Thymeleaf template rendering
     */
    //@JvmField
    val thymeleafLocale: String?

    /**
     * @return Flag indicating if content matching prefix below should be given extension-less URI's
     */
    //@JvmField
    val uriWithoutExtension: Boolean

    /**
     * @return Flag indicating if image paths should be prepended with [.getSiteHost] value - only has an effect if
     * [.getImgPathUpdate] is set to true
     */
    //@JvmField
    val imgPathPrependHost: Boolean

    /**
     * @return Flag indicating if image paths in content should be updated with absolute path (using URI value of content file),
     * see [.getImgPathUpdate] which allows you to control the absolute path used
     */
    //@JvmField
    val imgPathUpdate: Boolean

    /**
     * @return Version of JBake
     */
    //@JvmField
    val version: String?

    /**
     * Set a property value for the given key
     *
     * @param key   the key for the property
     * @param value the value of the property
     */
    fun setProperty(key: String?, value: Any?)

    /**
     *
     * @param type the documents type
     * @return the the thymeleaf render mode ( defaults to [DefaultJBakeConfiguration.DEFAULT_TYHMELEAF_TEMPLATE_MODE] )
     */
    fun getThymeleafModeByType(type: String?): String?

    //@JvmField
    val serverContextPath: String?

    //@JvmField
    val serverHostname: String?

    /**
     * @return Abbreviated hash of latest git commit
     */
    //@JvmField
    val abbreviatedGitHash: String?

    /**
     * @return Locale to set in the JVM
     */
    //@JvmField
    val jvmLocale: String?

    /**
     *
     * @return TimeZone to use within Freemarker
     */
    //@JvmField
    val freemarkerTimeZone: TimeZone?

    fun asHashMap(): MutableMap<String?, Any?>?

    //@JvmField
    val jbakeProperties: MutableList<Property?>?

    fun addConfiguration(properties: Properties?)
}

