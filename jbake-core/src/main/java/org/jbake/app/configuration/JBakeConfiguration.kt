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

    /** Output filename for archive file, is only used when [.getRenderArchive] is true. */
    val archiveFileName: String?

    /** attributes to be set when processing input. */
    val asciidoctorAttributes: MutableList<String>

    /** Get an asciidoctor option by its key. */
    fun getAsciidoctorOption(optionKey: String): Any

    /** Get a list of asciidoctor options. */
    val asciidoctorOptionKeys: MutableList<String>

    /** the directory where assets are stored, they are copied directly in output directory and not processed. */
    val assetDir: File

    /** name of directory for assets. */
    val assetDirName: String?

    /** Flag indicating if hidden asset resources should be ignored. */
    val assetIgnoreHidden: Boolean

    /** Prefix to be used when exporting JBake properties to Asciidoctor. */
    val attributesExportPrefixForAsciidoctor: String?

    /** Timestamp that records when JBake build was made. */
    val buildTimeStamp: String?

    /** Flag indicating to flash the database cache. */
    val clearCache: Boolean

    /** the content directory. */
    val contentDir: File

    /** name of Dir where content (that's to say files to be transformed) resides in. */
    val contentDirName: String?

    /** the data directory. */
    val dataDir: File

    /** name of Dir where data files reside in. */
    val dataDirName: String?

    /** docType for data files. */
    val dataFileDocType: String

    /** Dir to store database files in. */
    val databasePath: String?

    /** name to identify if database is kept in memory (memory) or persisted to disk (plocal). */
    val databaseStore: String

    /** How date is formated. */
    val dateFormat: String?

    /** Default status to use (in order to avoid putting it in all files). */
    val defaultStatus: String?

    /** Default type to use (in order to avoid putting it in all files). */
    val defaultType: String?

    /** The destination directory to render and copy files to. */
    var destinationDir: File

    val documentTypes: MutableList<String>

    /** Suffix used to identify draft files. */
    val draftSuffix: String?

    /** Output filename for error404 file, is only used when [.getRenderError404] is true. */
    val error404FileName: String?

    /**
     * Get name for example project name by given template type
     *
     * @return example project name
     */
    fun getExampleProjectByType(templateType: String): String?

    /** Flag indicating if JBake properties should be made available to Asciidoctor. */
    val exportAsciidoctorAttributes: Boolean

    /** Output filename for feed file, is only used when [.getRenderFeed] is true. */
    val feedFileName: String


    /** String used to separate the header from the body. */
    val headerSeparator: String?

    /** Filename to use to ignore a directory in addition to ".jbakeignore". */
    val ignoreDirMarkerFileName: String?

    /** Output filename for index, is only used when [.getRenderIndex] is true. */
    val indexFileName: String?

    /** Get an iterator of available configuration keys. */
    val keys: MutableIterator<String>

    /**
     * A list of markdown extensions
     * `markdown.extension=HARDWRAPS,AUTOLINKS,FENCED_CODE_BLOCKS,DEFINITIONS`
     */
    val markdownExtensions: MutableList<String>

    /** file extension to be used for all output files. */
    val outputExtension: String?

    fun getOutputExtensionByDocType(docType: String): String?

    /** Flag indicating if there should be pagination when rendering index. */
    val paginateIndex: Boolean

    /** How many posts per page on index. */
    val postsPerPage: Int

    /** URI prefix for content that should be given extension-less output URI's. */
    val prefixForUriWithoutExtension: String?

    /** Flag indicating if archive file should be generated. */
    val renderArchive: Boolean

    /** Encoding used when rendering files. */
    val renderEncoding: String?

    /** Output encoding for freemarker url escaping. */
    val outputEncoding: String?

    /** Flag indicating if error404 file should be generated. */
    val renderError404: Boolean

    /** Flag indicating if feed file should be generated. */
    val renderFeed: Boolean

    /** Flag indicating if index file should be generated. */
    val renderIndex: Boolean

    /** Flag indicating if sitemap file should be generated. */
    val renderSiteMap: Boolean

    /** Flag indicating if tag files should be generated. */
    val renderTags: Boolean

    /** Flag indicating if tag index file should be generated. */
    val renderTagsIndex: Boolean

    /** Flag indicating if the tag value should be sanitized. */
    val sanitizeTag: Boolean

    /** Port used when running Jetty server. */
    val serverPort: Int

    /** the host url of the site e.g. http://jbake.org. */
    val siteHost: String?

    /** Sitemap template file name. Used only when [.getRenderSiteMap] is set to true. */
    val siteMapFileName: String?

    /**
     * the source directory of the project
     * /// TODO: This is nullable - see Main.kt around line 140.
     */
    val sourceDir: File?

    /** Tags output path, used only when [.getRenderTags] is true. */
    val tagPathName: String?

    /** Encoding to be used for template files. */
    val templateEncoding: String?

    fun getTemplateByDocType(doctype: String): String?

    fun getTemplateFileByDocType(doctype: String): File?

    /** The template directory. */
    val templateDir: File

    /** Name of directory where template files are looked for. */
    val templateDirName: String?

    /** Locale used for Thymeleaf template rendering. */
    val thymeleafLocale: String?

    /** Flag indicating if content matching prefix below should be given extension-less URI's. */
    val uriWithoutExtension: Boolean

    /**
     * Flag indicating if image paths should be prepended with [.getSiteHost] value - only has an effect if [imgPathUpdate] is set to true.
     */
    val imgPathPrependHost: Boolean

    /**
     * Flag indicating if image paths in the content should be updated with absolute path (using URI value of content file),
     * See [imgPathPrependHost] which allows you to control the absolute path used.
     */
    val imgPathUpdate: Boolean

    val jbakeVersion: String?

    /**
     * Set a property value for the given key
     */
    fun setProperty(key: String, value: Any?)

    /**
     * @return The thymeleaf render mode. Default: [DefaultJBakeConfiguration.DEFAULT_TYHMELEAF_TEMPLATE_MODE]
     */
    fun getThymeleafModeByType(documentsType: String): String

    val serverContextPath: String

    val serverHostname: String

    /** Abbreviated hash of latest git commit. */
    val abbreviatedGitHash: String?

    /** Locale to set in the JVM. */
    val jvmLocale: String?

    /** TimeZone to use within Freemarker */
    val freemarkerTimeZone: TimeZone

    fun asHashMap(): MutableMap<String, Any>
    val jbakeProperties: MutableList<JBakeProperty>

    fun addConfiguration(properties: Properties)
}
