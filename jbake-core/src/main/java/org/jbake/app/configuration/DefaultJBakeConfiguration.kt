package org.jbake.app.configuration

import org.apache.commons.configuration2.CompositeConfiguration
import org.apache.commons.configuration2.MapConfiguration
import org.apache.commons.configuration2.SystemConfiguration
import org.apache.commons.lang3.StringUtils
import org.jbake.app.configuration.PropertyList.ASSET_IGNORE_HIDDEN
import org.jbake.app.configuration.PropertyList.TEMPLATE_FOLDER
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * The default implementation of a [JBakeConfiguration]
 */
class DefaultJBakeConfiguration : JBakeConfiguration {

    @JvmField
    var compositeConfiguration: CompositeConfiguration

    /**
     * Some deprecated implementations just need access to the configuration without access to the source directory
     *
     * @param configuration The project configuration
     */
    @Deprecated("use {@link #DefaultJBakeConfiguration(File, CompositeConfiguration)} instead")
    constructor(configuration: CompositeConfiguration) {
        this.compositeConfiguration = configuration
    }

    constructor(sourceDir: File?, configuration: CompositeConfiguration) {
        this.compositeConfiguration = configuration
        setSourceDir(sourceDir)
        // Note: setupPaths() is already called by setSourceDir()
    }

    override fun get(key: String): Any? {
        return compositeConfiguration.getProperty(key)
    }

    override val archiveFileName: String?
        get() = getAsString(PropertyList.ARCHIVE_FILE.key)

    private fun getAsBoolean(key: String): Boolean {
        return compositeConfiguration.getBoolean(key, false)
    }

    private fun getAsDir(key: String): File? {
        return get(key) as File?
    }

    private fun getAsInt(key: String, defaultValue: Int): Int {
        return compositeConfiguration.getInt(key, defaultValue)
    }

    private fun getAsList(key: String): MutableList<String> {
        val list = compositeConfiguration.getList(String::class.java, key)
        return list?.filterNotNull()?.toMutableList() ?: mutableListOf()
    }

    private fun getAsString(key: String): String? {
        return compositeConfiguration.getString(key)
    }

    private fun getAsString(key: String, defaultValue: String?): String? {
        return compositeConfiguration.getString(key, defaultValue)
    }

    override val asciidoctorAttributes: MutableList<String>
        get() = getAsList(PropertyList.ASCIIDOCTOR_ATTRIBUTES.key)

    override fun getAsciidoctorOption(optionKey: String): Any {
        val subConfig = compositeConfiguration.subset(PropertyList.ASCIIDOCTOR_OPTION.key)

        if (subConfig.containsKey(optionKey)) {
            // Use getList to properly handle comma-separated values
            // Commons Configuration will automatically split on commas if configured
            val list = subConfig.getList(optionKey) ?: emptyList()

            // If list is not empty, return it; otherwise check for single value
            if (list.isNotEmpty())
                return list.map { it.toString().trim() }

            // Fallback to getting raw value. TBD: Is this all needed? Can't it always be a list?
            val value = subConfig.get(Any::class.java, optionKey)
            return when (value) {
                is String -> listOf(value)
                is Collection<*> -> value
                null -> emptyList<String>()
                else -> value
            }
        }
        log.warn("Cannot find asciidoctor option '{}.{}'", PropertyList.ASCIIDOCTOR_OPTION.key, optionKey)
        return emptyList<String>()
    }

    override val asciidoctorOptionKeys: MutableList<String>
        get() {
            val options: MutableList<String> = ArrayList()
            val subConfig = compositeConfiguration.subset(PropertyList.ASCIIDOCTOR_OPTION.key)

            val iterator = subConfig.keys
            while (iterator.hasNext()) {
                val key = iterator.next()
                options.add(key)
            }

            return options
        }

    /** If the path is absolute, returns it as-is; otherwise resolves it relative to sourceDir. */
    private fun resolveDirPath(propertyKey: String, errorMessage: String): File {
        val path = getAsString(propertyKey) ?: error(errorMessage)
        val file = File(path)
        return if (file.isAbsolute) file else sourceDir.resolve(file)
    }

    // Implement interface properties that previously existed as getX() functions
    override var assetDir: File
        get() = resolveDirPath(PropertyList.ASSET_FOLDER.key, "Asset directory must be configured")
        set(value) { setProperty(PropertyList.ASSET_FOLDER.key, value) }

    override val assetDirName: String?
        get() = getAsString(PropertyList.ASSET_FOLDER.key)

    override var assetIgnoreHidden: Boolean
        get() = getAsBoolean(ASSET_IGNORE_HIDDEN.key)
        set(value) { setProperty(ASSET_IGNORE_HIDDEN.key, value) }

    override val attributesExportPrefixForAsciidoctor: String?
        get() = getAsString(PropertyList.ASCIIDOCTOR_ATTRIBUTES_EXPORT_PREFIX.key)

    override val buildTimeStamp: String?
        get() = getAsString(PropertyList.BUILD_TIMESTAMP.key)

    override var clearCache: Boolean
        get() = getAsBoolean(PropertyList.CLEAR_CACHE.key)
        set(value) = setProperty(PropertyList.CLEAR_CACHE.key, value)

    override var contentDir: File
        get() = resolveDirPath(PropertyList.CONTENT_FOLDER.key, "Content directory must be configured")
        set(value) = setProperty(PropertyList.CONTENT_FOLDER.key, value)

    override val contentDirName: String?
        get() = getAsString(PropertyList.CONTENT_FOLDER.key)

    override var dataDir: File
        get() = resolveDirPath(PropertyList.DATA_FOLDER.key, "Data dir must be configured")
        set(value) { setProperty(PropertyList.DATA_FOLDER.key, value) }

    override var dataDirName: String?
        get() = getAsString(PropertyList.DATA_FOLDER.key)
        set(value) { setProperty(PropertyList.DATA_FOLDER.key, value) }

    override val dataFileDocType: String
        get() = getAsString(PropertyList.DATA_FILE_DOCTYPE.key) ?: ""

    override var databasePath: String?
        get() = getAsString(PropertyList.DB_PATH.key)
        set(value) { setProperty(PropertyList.DB_PATH.key, value) }

    override var databaseStore: String
        get() = getAsString(PropertyList.DB_STORE.key) ?: "memory"
        set(value) { setProperty(PropertyList.DB_STORE.key, value) }


    override val dateFormat: String?
        get() = getAsString(PropertyList.DATE_FORMAT.key)

    override val defaultStatus: String?
        get() = getAsString(PropertyList.DEFAULT_STATUS.key)

    fun setDefaultStatus(status: String?) {
        setProperty(PropertyList.DEFAULT_STATUS.key, status)
    }

    override val defaultType: String?
        get() = getAsString(PropertyList.DEFAULT_TYPE.key)

    fun setDefaultType(type: String?) {
        setProperty(PropertyList.DEFAULT_TYPE.key, type)
    }

    override var destinationDir: File
        get() = resolveDirPath(PropertyList.DESTINATION_FOLDER.key, "Destination dir must be configured")
        set(value) = setProperty(PropertyList.DESTINATION_FOLDER.key, value)

    override val documentTypes: MutableList<String>
        get() {
            val docTypes: MutableList<String> = ArrayList()
            val keyIterator = compositeConfiguration.keys
            while (keyIterator.hasNext()) {
                val key = keyIterator.next()
                val matcher: Matcher = TEMPLATE_DOC_PATTERN.matcher(key)
                if (matcher.find())
                    matcher.group(1)?.let { docTypes.add(it) }
            }
            return docTypes
        }

    override val draftSuffix: String?
        get() = getAsString(PropertyList.DRAFT_SUFFIX.key)

    override val error404FileName: String?
        get() = getAsString(PropertyList.ERROR404_FILE.key)

    override fun getExampleProjectByType(templateType: String): String? {
        return getAsString("example.project.$templateType")
    }

    override val exportAsciidoctorAttributes: Boolean
        get() = getAsBoolean(PropertyList.ASCIIDOCTOR_ATTRIBUTES_EXPORT.key)

    override val feedFileName: String
        get() = getAsString(PropertyList.FEED_FILE.key) ?: "feed.xml"

    override val ignoreDirMarkerFileName: String?
        get() = getAsString(PropertyList.IGNORE_FILE.key)

    override val indexFileName: String?
        get() = getAsString(PropertyList.INDEX_FILE.key)

    // keys property from interface can be nullable iterator of nullable Strings
    override val keys: MutableIterator<String>
        get() = compositeConfiguration.keys as MutableIterator<String>

    override val markdownExtensions: MutableList<String>
        get() = getAsList(PropertyList.MARKDOWN_EXTENSIONS.key)

    fun setMarkdownExtensions(vararg extensions: String?) {
        setProperty(PropertyList.MARKDOWN_EXTENSIONS.key, StringUtils.join(extensions, ","))
    }

    override val outputExtension: String?
        get() = getAsString(PropertyList.OUTPUT_EXTENSION.key)

    fun setOutputExtension(outputExtension: String?) {
        setProperty(PropertyList.OUTPUT_EXTENSION.key, outputExtension)
    }

    override fun getOutputExtensionByDocType(docType: String): String? {
        val templateExtensionKey: String = DOCTYPE_TEMPLATE_PREFIX + docType + DOCTYPE_EXTENSION_POSTFIX
        val defaultOutputExtension = outputExtension
        return getAsString(templateExtensionKey, defaultOutputExtension)
    }

    override val paginateIndex: Boolean
        get() = getAsBoolean(PropertyList.PAGINATE_INDEX.key)

    fun setPaginateIndex(paginateIndex: Boolean) {
        setProperty(PropertyList.PAGINATE_INDEX.key, paginateIndex)
    }

    override val postsPerPage: Int
        get() = getAsInt(PropertyList.POSTS_PER_PAGE.key, 5)

    fun setPostsPerPage(postsPerPage: Int) {
        setProperty(PropertyList.POSTS_PER_PAGE.key, postsPerPage)
    }

    override val prefixForUriWithoutExtension: String?
        get() = getAsString(PropertyList.URI_NO_EXTENSION_PREFIX.key)

    fun setPrefixForUriWithoutExtension(prefix: String?) {
        setProperty(PropertyList.URI_NO_EXTENSION_PREFIX.key, prefix)
    }

    override val renderArchive: Boolean
        get() = getAsBoolean(PropertyList.RENDER_ARCHIVE.key)

    override val renderEncoding: String?
        get() = getAsString(PropertyList.RENDER_ENCODING.key)

    override val outputEncoding: String?
        get() = getAsString(PropertyList.OUTPUT_ENCODING.key)

    override val renderError404: Boolean
        get() = getAsBoolean(PropertyList.RENDER_ERROR404.key)

    override val renderFeed: Boolean
        get() = getAsBoolean(PropertyList.RENDER_FEED.key)

    override val renderIndex: Boolean
        get() = getAsBoolean(PropertyList.RENDER_INDEX.key)

    override val renderSiteMap: Boolean
        get() = getAsBoolean(PropertyList.RENDER_SITEMAP.key)

    override val renderTags: Boolean
        get() = getAsBoolean(PropertyList.RENDER_TAGS.key)

    override val renderTagsIndex: Boolean
        get() = compositeConfiguration.getBoolean(PropertyList.RENDER_TAGS_INDEX.key, false)

    fun setRenderTagsIndex(enable: Boolean) {
        compositeConfiguration.setProperty(PropertyList.RENDER_TAGS_INDEX.key, enable)
    }

    override val sanitizeTag: Boolean
        get() = getAsBoolean(PropertyList.TAG_SANITIZE.key)

    override val serverPort: Int
        get() = getAsInt(PropertyList.SERVER_PORT.key, 8080)

    fun setServerPort(port: Int) {
        setProperty(PropertyList.SERVER_PORT.key, port)
    }

    override var siteHost: String?
        get() = getAsString(PropertyList.SITE_HOST.key)
        set(value) { setProperty(PropertyList.SITE_HOST.key, value) }

    override val siteMapFileName: String?
        get() = getAsString(PropertyList.SITEMAP_FILE.key)

    override val sourceDir: File
        get() = getAsDir(SOURCE_FOLDER_KEY) ?: error("Source dir must be configured")

    fun setSourceDir(sourceDir: File?) {
        setProperty(SOURCE_FOLDER_KEY, sourceDir)
        // Note: setupPaths() removed - paths are computed lazily when Dir properties are accessed
    }

    override val tagPathName: String?
        get() = getAsString(PropertyList.TAG_PATH.key)

    override val templateEncoding: String?
        get() = getAsString(PropertyList.TEMPLATE_ENCODING.key)

    override fun getTemplateByDocType(doctype: String): String? {
        val templateKey: String = DOCTYPE_TEMPLATE_PREFIX + doctype + DOCTYPE_FILE_POSTFIX
        val templateFileName = getAsString(templateKey)
        if (templateFileName != null && templateFileName.isNotEmpty()) {
            return templateFileName
        }
        log.warn("Cannot find configuration key '{}' for document type '{}'", templateKey, doctype)
        return null
    }

    override fun getTemplateFileByDocType(doctype: String): File? {
        val templateFileName = getTemplateByDocType(doctype)
        if (!templateFileName.isNullOrEmpty()) {
            return File(templateDir, templateFileName)
        }
        return null
    }

    override var templateDir: File
        get() = resolveDirPath(TEMPLATE_FOLDER.key, "Template dir must be configured")
        set(value) = setProperty(TEMPLATE_FOLDER.key, value)

    override val templateDirName: String?
        get() = getAsString(TEMPLATE_FOLDER.key)

    override val thymeleafLocale: String?
        get() = getAsString(PropertyList.THYMELEAF_LOCALE.key)

    override val uriWithoutExtension: Boolean
        get() = getAsBoolean(PropertyList.URI_NO_EXTENSION.key)

    fun setUriWithoutExtension(withoutExtension: Boolean) {
        setProperty(PropertyList.URI_NO_EXTENSION.key, withoutExtension)
    }

    override val jbakeVersion: String?
        get() = getAsString(PropertyList.VERSION.key)

    fun setDestinationDirName(folderName: String?) {
        setProperty(PropertyList.DESTINATION_FOLDER.key, folderName)
        // Note: destinationDir is computed lazily when accessed
    }

    fun setExampleProject(type: String, fileName: String?) {
        val projectKey = "example.project.$type"
        setProperty(projectKey, fileName)
    }

    override fun setProperty(key: String, value: Any?) {
        compositeConfiguration.setProperty(key, value)
    }

    override fun getThymeleafModeByType(documentsType: String): String {
        val key = "template_" + documentsType + "_thymeleaf_mode"
        return getAsString(key, DEFAULT_TYHMELEAF_TEMPLATE_MODE)!!
    }

    override val serverContextPath: String
        get() = getAsString(PropertyList.SERVER_CONTEXT_PATH.key)!!

    override val serverHostname: String
        get() = getAsString(PropertyList.SERVER_HOSTNAME.key)!!

    override fun asHashMap(): MutableMap<String, Any> {
        val configModel = HashMap<String, Any>()
        val configKeys = this.keys
        while (configKeys.hasNext()) {
            val key = configKeys.next()

            val valueObject =
                if (key == PropertyList.PAGINATE_INDEX.key) this.paginateIndex
                else this.get(key)

            if (valueObject != null) {
                //replace "." in key so you can use dot notation in templates
                configModel[key.replace(".", "_")] = valueObject
            }
        }
        return configModel
    }

    fun setTemplateExtensionForDocType(docType: String, extension: String?) {
        val templateExtensionKey: String = DOCTYPE_TEMPLATE_PREFIX + docType + DOCTYPE_EXTENSION_POSTFIX
        setProperty(templateExtensionKey, extension)
    }

    fun setTemplateFileNameForDocType(docType: String, fileName: String?) {
        val templateKey: String = DOCTYPE_TEMPLATE_PREFIX + docType + DOCTYPE_FILE_POSTFIX
        setProperty(templateKey, fileName)
    }

    internal fun setupPaths() {
        setupDefaultDestinationDir()
        setupDefaultAssetDir()
        setupDefaultTemplateDir()
        setupDefaultContentDir()
        setupDefaultDataDir()
    }

    internal fun setupDefaultDestinationDir() {
        val destinationPath = getAsString(PropertyList.DESTINATION_FOLDER.key) ?: ""

        val destination = File(destinationPath)
        destinationDir =
            if (destination.isAbsolute) destination
            else File(sourceDir, destinationPath)
    }

    /// TODO This is weird logic, review
    internal fun setupDefaultAssetDir() {
        val assetDirTmp = getAsString(PropertyList.ASSET_FOLDER.key) ?: ""

        val asset = File(assetDirTmp)
        assetDir =
            if (asset.isAbsolute) asset
            else sourceDir.resolve( asset)
    }

    internal fun setupDefaultTemplateDir() {
        val templateDirPath = File(getAsString(TEMPLATE_FOLDER.key) ?: "")

        this.templateDir =
            if (templateDirPath.isAbsolute) templateDirPath
            else sourceDir.resolve(templateDirPath)
    }

    internal fun setupDefaultDataDir() {
        val data = File(getAsString(PropertyList.DATA_FOLDER.key) ?: "")

        dataDir =
            if (data.isAbsolute) data
            else sourceDir.resolve(data)
    }

    internal fun setupDefaultContentDir() {
        val content = File(getAsString(PropertyList.CONTENT_FOLDER.key) ?: "")

        contentDir =
            if (content.isAbsolute) content
            else sourceDir.resolve(content)
    }

    override var headerSeparator: String?
        get() = getAsString(PropertyList.HEADER_SEPARATOR.key)
        set(value) { setProperty(PropertyList.HEADER_SEPARATOR.key, value) }

    override var imgPathPrependHost: Boolean
        get() = getAsBoolean(PropertyList.IMG_PATH_PREPEND_HOST.key)
        set(value) { setProperty(PropertyList.IMG_PATH_PREPEND_HOST.key, value) }

    override val imgPathUpdate: Boolean
        get() = getAsBoolean(PropertyList.IMG_PATH_UPDATE.key)


    override val jbakeProperties: MutableList<Property>
        get() {
            val jbakeKeys: MutableList<Property> = ArrayList()

            for (i in 0 until compositeConfiguration.numberOfConfigurations) {
                val configuration = compositeConfiguration.getConfiguration(i)

                if (configuration !is SystemConfiguration) {
                    val keys = configuration.keys
                    while (keys.hasNext()) {
                        val property = PropertyList.getPropertyByKey(keys.next())
                        if (!jbakeKeys.contains(property))
                            jbakeKeys.add(property)
                    }
                }
            }
            jbakeKeys.sort()
            return jbakeKeys
        }

    override fun addConfiguration(properties: Properties) {
        compositeConfiguration.addConfiguration(MapConfiguration(properties))
    }

    override val abbreviatedGitHash: String?
        get() = getAsString(PropertyList.GIT_HASH.key)

    override val jvmLocale: String?
        get() = getAsString(PropertyList.JVM_LOCALE.key)

    override val freemarkerTimeZone: TimeZone
        get() {
            val timezone = getAsString(PropertyList.FREEMARKER_TIMEZONE.key)
            return if (timezone == null || StringUtils.isEmpty(timezone))
                    TimeZone.getDefault()
                else TimeZone.getTimeZone(timezone)
        }

    companion object {
        const val DEFAULT_TYHMELEAF_TEMPLATE_MODE: String = "HTML"
        // SOURCE_FOLDER_KEY is special - sourceDir is not a configuration property, it's constructor parameter
        private const val SOURCE_FOLDER_KEY = "source.folder"
        private val TEMPLATE_DOC_PATTERN: Pattern = "template\\.([a-zA-Z0-9-_]+)\\.file".toRegex().toPattern()
        private const val DOCTYPE_FILE_POSTFIX = ".file"
        private const val DOCTYPE_EXTENSION_POSTFIX = ".extension"
        private const val DOCTYPE_TEMPLATE_PREFIX = "template."
    }

    private val log: Logger = LoggerFactory.getLogger(DefaultJBakeConfiguration::class.java)
}
