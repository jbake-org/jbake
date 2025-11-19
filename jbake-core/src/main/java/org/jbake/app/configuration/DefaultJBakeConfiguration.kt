package org.jbake.app.configuration

import org.apache.commons.configuration2.CompositeConfiguration
import org.apache.commons.configuration2.MapConfiguration
import org.apache.commons.configuration2.SystemConfiguration
import org.apache.commons.lang3.StringUtils
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
    private val logger: Logger = LoggerFactory.getLogger(DefaultJBakeConfiguration::class.java)
    @JvmField
    var compositeConfiguration: CompositeConfiguration

    /**
     * Some deprecated implementations just need access to the configuration without access to the source folder
     *
     * @param configuration The project configuration
     */
    @Deprecated("use {@link #DefaultJBakeConfiguration(File, CompositeConfiguration)} instead")
    constructor(configuration: CompositeConfiguration) {
        this.compositeConfiguration = configuration
    }

    constructor(sourceFolder: File?, configuration: CompositeConfiguration) {
        this.compositeConfiguration = configuration
        setSourceFolder(sourceFolder)
        setupPaths()
    }

    override fun get(key: String): Any? {
        return compositeConfiguration.getProperty(key)
    }

    override val archiveFileName: String?
        get() = getAsString(PropertyList.ARCHIVE_FILE.key, null)

    private fun getAsBoolean(key: String?): Boolean {
        return compositeConfiguration.getBoolean(key, false)
    }

    private fun getAsFolder(key: String?): File? {
        return get(key) as File?
    }

    private fun getAsInt(key: String?, defaultValue: Int): Int {
        return compositeConfiguration.getInt(key, defaultValue)
    }

    private fun getAsList(key: String?): MutableList<String?>? {
        return compositeConfiguration.getList<String?>(String::class.java, key)
    }

    private fun getAsString(key: String?): String {
        return compositeConfiguration.getString(key)
    }

    private fun getAsString(key: String?, defaultValue: String?): String? {
        return compositeConfiguration.getString(key, defaultValue)
    }

    fun getAsciidoctorAttributes(): MutableList<String?>? {
        return getAsList(PropertyList.ASCIIDOCTOR_ATTRIBUTES.key)
    }

    override fun getAsciidoctorOption(optionKey: String): Any? {
        val subConfig = compositeConfiguration.subset(PropertyList.ASCIIDOCTOR_OPTION.key)

        if (subConfig.containsKey(optionKey)) {
            return subConfig.get(optionKey)
        } else {
            logger.warn("Cannot find asciidoctor option '{}.{}'", PropertyList.ASCIIDOCTOR_OPTION.key, optionKey)
            return null
        }
    }

    fun getAsciidoctorOptionKeys(): MutableList<String> {
        val options: MutableList<String> = ArrayList()
        val subConfig = compositeConfiguration.subset(PropertyList.ASCIIDOCTOR_OPTION.key)

        val iterator = subConfig.getKeys()
        while (iterator.hasNext()) {
            val key = iterator.next()
            options.add(key!!)
        }

        return options
    }

    fun getAssetFolder(): File? {
        return getAsFolder(ASSET_FOLDER_KEY)
    }

    fun setAssetFolder(assetFolder: File?) {
        if (assetFolder != null) {
            setProperty(ASSET_FOLDER_KEY, assetFolder)
        }
    }

    override val assetFolderName: String
        get() = getAsString(PropertyList.ASSET_FOLDER.key)

    fun getAssetIgnoreHidden(): Boolean {
        return getAsBoolean(PropertyList.ASSET_IGNORE_HIDDEN.key)
    }

    fun setAssetIgnoreHidden(assetIgnoreHidden: Boolean) {
        setProperty(PropertyList.ASSET_IGNORE_HIDDEN.key, assetIgnoreHidden)
    }

    fun getAttributesExportPrefixForAsciidoctor(): String? {
        return getAsString(PropertyList.ASCIIDOCTOR_ATTRIBUTES_EXPORT_PREFIX.key, "")
    }

    fun getBuildTimeStamp(): String {
        return getAsString(PropertyList.BUILD_TIMESTAMP.key)
    }

    fun getClearCache(): Boolean {
        return getAsBoolean(PropertyList.CLEAR_CACHE.key)
    }

    fun setClearCache(clearCache: Boolean) {
        setProperty(PropertyList.CLEAR_CACHE.key, clearCache)
    }

    fun getContentFolder(): File? {
        return getAsFolder(CONTENT_FOLDER_KEY)
    }

    fun setContentFolder(contentFolder: File?) {
        if (contentFolder != null) {
            setProperty(CONTENT_FOLDER_KEY, contentFolder)
        }
    }

    override val contentFolderName: String
        get() = getAsString(PropertyList.CONTENT_FOLDER.key)

    fun getDataFolder(): File? {
        return getAsFolder(DATA_FOLDER_KEY)
    }

    fun setDataFolder(dataFolder: File?) {
        if (dataFolder != null) {
            setProperty(DATA_FOLDER_KEY, dataFolder)
        }
    }

    override val dataFolderName: String
        get() = getAsString(PropertyList.DATA_FOLDER.key)

    override val dataFileDocType: String
        get() = getAsString(PropertyList.DATA_FILE_DOCTYPE.key)

    fun setDataFileDocType(dataFileDocType: String?) {
        setProperty(PropertyList.DATA_FILE_DOCTYPE.key, dataFileDocType)
    }

    override val databasePath: String
        get() = getAsString(PropertyList.DB_PATH.key)

    fun setDatabasePath(path: String?) {
        setProperty(PropertyList.DB_PATH.key, path)
    }

    override val databaseStore: String
        get() = getAsString(PropertyList.DB_STORE.key)

    fun setDatabaseStore(storeType: String?) {
        setProperty(PropertyList.DB_STORE.key, storeType)
    }


    override val dateFormat: String
        get() = getAsString(PropertyList.DATE_FORMAT.key)

    override val defaultStatus: String?
        get() = getAsString(PropertyList.DEFAULT_STATUS.key, "")

    fun setDefaultStatus(status: String?) {
        setProperty(PropertyList.DEFAULT_STATUS.key, status)
    }

    override val defaultType: String?
        get() = getAsString(PropertyList.DEFAULT_TYPE.key, "")

    fun setDefaultType(type: String?) {
        setProperty(PropertyList.DEFAULT_TYPE.key, type)
    }

    override var destinationFolder: File
        get() = getAsFolder(DESTINATION_FOLDER_KEY) ?: error("Destination folder must be configured")
        set(value) {
            setProperty(DESTINATION_FOLDER_KEY, value)
        }

    fun getDocumentTypes(): MutableList<String?> {
        val docTypes: MutableList<String?> = ArrayList<String?>()
        val keyIterator = compositeConfiguration.getKeys()
        while (keyIterator.hasNext()) {
            val key = keyIterator.next()
            val matcher: Matcher = TEMPLATE_DOC_PATTERN.matcher(key)
            if (matcher.find()) {
                docTypes.add(matcher.group(1))
            }
        }

        return docTypes
    }

    override val draftSuffix: String?
        get() = getAsString(PropertyList.DRAFT_SUFFIX.key, "")

    override val error404FileName: String
        get() = getAsString(PropertyList.ERROR404_FILE.key)

    fun getExampleProjectByType(templateType: String): String {
        return getAsString("example.project." + templateType)
    }

    override val exportAsciidoctorAttributes: Boolean
        get() = getAsBoolean(PropertyList.ASCIIDOCTOR_ATTRIBUTES_EXPORT.key)

    override val feedFileName: String
        get() = getAsString(PropertyList.FEED_FILE.key)

    override val ignoreFileName: String
        get() = getAsString(PropertyList.IGNORE_FILE.key)

    override val indexFileName: String
        get() = getAsString(PropertyList.INDEX_FILE.key)

    override val keys: MutableIterator<String>
        get() = compositeConfiguration.getKeys()

    override val markdownExtensions: MutableList<String?>?
        get() = getAsList(PropertyList.MARKDOWN_EXTENSIONS.key)

    fun setMarkdownExtensions(vararg extensions: String?) {
        setProperty(PropertyList.MARKDOWN_EXTENSIONS.key, StringUtils.join(extensions, ","))
    }

    override val outputExtension: String
        get() = getAsString(PropertyList.OUTPUT_EXTENSION.key)

    fun setOutputExtension(outputExtension: String?) {
        setProperty(PropertyList.OUTPUT_EXTENSION.key, outputExtension)
    }

    fun getOutputExtensionByDocType(docType: String): String? {
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

    override val prefixForUriWithoutExtension: String
        get() = getAsString(PropertyList.URI_NO_EXTENSION_PREFIX.key)

    fun setPrefixForUriWithoutExtension(prefix: String?) {
        setProperty(PropertyList.URI_NO_EXTENSION_PREFIX.key, prefix)
    }

    override val renderArchive: Boolean
        get() = getAsBoolean(PropertyList.RENDER_ARCHIVE.key)

    override val renderEncoding: String
        get() = getAsString(PropertyList.RENDER_ENCODING.key)

    override val outputEncoding: String
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

    override val siteHost: String?
        get() = getAsString(PropertyList.SITE_HOST.key, "http://www.jbake.org")

    fun setSiteHost(siteHost: String?) {
        setProperty(PropertyList.SITE_HOST.key, siteHost)
    }

    override val siteMapFileName: String
        get() = getAsString(PropertyList.SITEMAP_FILE.key)

    override val sourceFolder: File
        get() = getAsFolder(SOURCE_FOLDER_KEY) ?: error("Source folder must be configured")

    fun setSourceFolder(sourceFolder: File?) {
        setProperty(SOURCE_FOLDER_KEY, sourceFolder)
        setupPaths()
    }

    override val tagPathName: String
        get() = getAsString(PropertyList.TAG_PATH.key)

    override val templateEncoding: String
        get() = getAsString(PropertyList.TEMPLATE_ENCODING.key)

    fun getTemplateByDocType(docType: String): String? {
        val templateKey: String = DOCTYPE_TEMPLATE_PREFIX + docType + DOCTYPE_FILE_POSTFIX
        val templateFileName = getAsString(templateKey)
        if (templateFileName != null) {
            return templateFileName
        }
        logger.warn("Cannot find configuration key '{}' for document type '{}'", templateKey, docType)
        return null
    }

    fun getTemplateFileByDocType(docType: String): File? {
        val templateFileName = getTemplateByDocType(docType)
        if (templateFileName != null) {
            return File(getTemplateFolder(), templateFileName)
        }
        return null
    }

    fun getTemplateFolder(): File? {
        return getAsFolder(TEMPLATE_FOLDER_KEY)
    }

    fun setTemplateFolder(templateFolder: File?) {
        if (templateFolder != null) {
            setProperty(TEMPLATE_FOLDER_KEY, templateFolder)
        }
    }

    override val templateFolderName: String
        get() = getAsString(PropertyList.TEMPLATE_FOLDER.key)

    override val thymeleafLocale: String
        get() = getAsString(PropertyList.THYMELEAF_LOCALE.key)

    override val uriWithoutExtension: Boolean
        get() = getAsBoolean(PropertyList.URI_NO_EXTENSION.key)

    fun setUriWithoutExtension(withoutExtension: Boolean) {
        setProperty(PropertyList.URI_NO_EXTENSION.key, withoutExtension)
    }

    override val version: String
        get() = getAsString(PropertyList.VERSION.key)

    fun setDestinationFolderName(folderName: String?) {
        setProperty(PropertyList.DESTINATION_FOLDER.key, folderName)
        setupDefaultDestination()
    }

    fun setExampleProject(type: String, fileName: String?) {
        val projectKey = "example.project." + type
        setProperty(projectKey, fileName)
    }

    fun setProperty(key: String, value: Any?) {
        compositeConfiguration.setProperty(key, value)
    }

    fun getThymeleafModeByType(type: String): String? {
        val key = "template_" + type + "_thymeleaf_mode"
        return getAsString(key, DEFAULT_TYHMELEAF_TEMPLATE_MODE)
    }

    override val serverContextPath: String
        get() = getAsString(PropertyList.SERVER_CONTEXT_PATH.key)

    override val serverHostname: String
        get() = getAsString(PropertyList.SERVER_HOSTNAME.key)

    override fun asHashMap(): MutableMap<String, Any>? {
        val configModel = HashMap<String, Any>()
        val configKeys = this.getKeys()
        while (configKeys.hasNext()) {
            val key = configKeys.next()
            val valueObject: Any?

            if (key == PropertyList.PAGINATE_INDEX.key) {
                valueObject = this.getPaginateIndex()
            } else {
                valueObject = this.get(key)
            }
            if (valueObject != null) {
                //replace "." in key so you can use dot notation in templates
                configModel[key.replace(".", "_")] = valueObject
            }
        }
        return if (configModel.isEmpty()) null else configModel
    }

    fun setTemplateExtensionForDocType(docType: String, extension: String?) {
        val templateExtensionKey: String = DOCTYPE_TEMPLATE_PREFIX + docType + DOCTYPE_EXTENSION_POSTFIX
        setProperty(templateExtensionKey, extension)
    }

    fun setTemplateFileNameForDocType(docType: String, fileName: String?) {
        val templateKey: String = DOCTYPE_TEMPLATE_PREFIX + docType + DOCTYPE_FILE_POSTFIX
        setProperty(templateKey, fileName)
    }

    private fun setupPaths() {
        setupDefaultDestination()
        setupDefaultAssetFolder()
        setupDefaultTemplateFolder()
        setupDefaultContentFolder()
        setupDefaultDataFolder()
    }

    private fun setupDefaultDestination() {
        val destinationPath = getAsString(PropertyList.DESTINATION_FOLDER.key)

        val destination = File(destinationPath)
        if (destination.isAbsolute()) {
            setDestinationFolder(destination)
        } else {
            setDestinationFolder(File(getSourceFolder(), destinationPath))
        }
    }

    private fun setupDefaultAssetFolder() {
        val assetFolder = getAsString(PropertyList.ASSET_FOLDER.key)


        val asset = File(assetFolder)
        if (asset.isAbsolute()) {
            setAssetFolder(asset)
        } else {
            setAssetFolder(File(getSourceFolder(), assetFolder))
        }
    }

    private fun setupDefaultTemplateFolder() {
        val templateFolder = getAsString(PropertyList.TEMPLATE_FOLDER.key)

        val template = File(templateFolder)
        if (template.isAbsolute()) {
            setTemplateFolder(template)
        } else {
            setTemplateFolder(File(getSourceFolder(), templateFolder))
        }
    }

    private fun setupDefaultDataFolder() {
        val dataFolder = getAsString(PropertyList.DATA_FOLDER.key)

        val data = File(dataFolder)
        if (data.isAbsolute()) {
            setDataFolder(data)
        } else {
            setDataFolder(File(getSourceFolder(), dataFolder))
        }
    }

    private fun setupDefaultContentFolder() {
        setContentFolder(File(getSourceFolder(), getContentFolderName()))
    }

    override val headerSeparator: String
        get() = getAsString(PropertyList.HEADER_SEPARATOR.key)

    fun setHeaderSeparator(headerSeparator: String?) {
        setProperty(PropertyList.HEADER_SEPARATOR.key, headerSeparator)
    }

    override val imgPathPrependHost: Boolean
        get() = getAsBoolean(PropertyList.IMG_PATH_PREPEND_HOST.key)

    fun setImgPathPrependHost(imgPathPrependHost: Boolean) {
        setProperty(PropertyList.IMG_PATH_PREPEND_HOST.key, imgPathPrependHost)
    }

    override val imgPathUpdate: Boolean
        get() = getAsBoolean(PropertyList.IMG_PATH_UPDATE.key)

    fun setImgPathUPdate(imgPathUpdate: Boolean) {
        setProperty(PropertyList.IMG_PATH_UPDATE.key, imgPathUpdate)
    }

    fun getJbakeProperties(): MutableList<Property?> {
        val jbakeKeys: MutableList<Property?> = ArrayList<Property?>()

        for (i in 0..<compositeConfiguration.getNumberOfConfigurations()) {
            val configuration = compositeConfiguration.getConfiguration(i)

            if (configuration !is SystemConfiguration) {
                val it = configuration.getKeys()
                while (it.hasNext()) {
                    val key = it.next()
                    val property = PropertyList.getPropertyByKey(key)
                    if (!jbakeKeys.contains(property)) {
                        jbakeKeys.add(property)
                    }
                }
            }
        }
        Collections.sort<Property?>(jbakeKeys)
        return jbakeKeys
    }

    override fun addConfiguration(properties: Properties?) {
        compositeConfiguration.addConfiguration(MapConfiguration(properties))
    }

    override val abbreviatedGitHash: String
        get() = getAsString(PropertyList.GIT_HASH.key)

    override val jvmLocale: String
        get() = getAsString(PropertyList.JVM_LOCALE.key)

    fun getFreemarkerTimeZone(): TimeZone? {
        val timezone = getAsString(PropertyList.FREEMARKER_TIMEZONE.key)
        if (StringUtils.isNotEmpty(timezone)) {
            return TimeZone.getTimeZone(timezone)
        }
        return null
    }

    companion object {
        const val DEFAULT_TYHMELEAF_TEMPLATE_MODE: String = "HTML"
        private const val SOURCE_FOLDER_KEY = "sourceFolder"
        private const val DESTINATION_FOLDER_KEY = "destinationFolder"
        private const val ASSET_FOLDER_KEY = "assetFolder"
        private const val TEMPLATE_FOLDER_KEY = "templateFolder"
        private const val CONTENT_FOLDER_KEY = "contentFolder"
        private const val DATA_FOLDER_KEY = "dataFolder"
        private val TEMPLATE_DOC_PATTERN: Pattern = Pattern.compile("(?:template\\.)([a-zA-Z0-9-_]+)(?:\\.file)")
        private const val DOCTYPE_FILE_POSTFIX = ".file"
        private const val DOCTYPE_EXTENSION_POSTFIX = ".extension"
        private const val DOCTYPE_TEMPLATE_PREFIX = "template."
    }
}
