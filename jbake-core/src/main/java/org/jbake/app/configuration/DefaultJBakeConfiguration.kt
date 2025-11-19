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

    override fun getArchiveFileName(): String {
        return getAsString(PropertyList.ARCHIVE_FILE.getKey())
    }

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

    override fun getAsciidoctorAttributes(): MutableList<String?>? {
        return getAsList(PropertyList.ASCIIDOCTOR_ATTRIBUTES.getKey())
    }

    override fun getAsciidoctorOption(optionKey: String): Any? {
        val subConfig = compositeConfiguration.subset(PropertyList.ASCIIDOCTOR_OPTION.getKey())

        if (subConfig.containsKey(optionKey)) {
            return subConfig.get(optionKey)
        } else {
            logger.warn("Cannot find asciidoctor option '{}.{}'", PropertyList.ASCIIDOCTOR_OPTION.getKey(), optionKey)
            return null
        }
    }

    override fun getAsciidoctorOptionKeys(): MutableList<String> {
        val options: MutableList<String> = ArrayList()
        val subConfig = compositeConfiguration.subset(PropertyList.ASCIIDOCTOR_OPTION.getKey())

        val iterator = subConfig.getKeys()
        while (iterator.hasNext()) {
            val key = iterator.next()
            options.add(key!!)
        }

        return options
    }

    override fun getAssetFolder(): File? {
        return getAsFolder(ASSET_FOLDER_KEY)
    }

    fun setAssetFolder(assetFolder: File?) {
        if (assetFolder != null) {
            setProperty(ASSET_FOLDER_KEY, assetFolder)
        }
    }

    override fun getAssetFolderName(): String {
        return getAsString(PropertyList.ASSET_FOLDER.getKey())
    }

    override fun getAssetIgnoreHidden(): Boolean {
        return getAsBoolean(PropertyList.ASSET_IGNORE_HIDDEN.getKey())
    }

    fun setAssetIgnoreHidden(assetIgnoreHidden: Boolean) {
        setProperty(PropertyList.ASSET_IGNORE_HIDDEN.getKey(), assetIgnoreHidden)
    }

    override fun getAttributesExportPrefixForAsciidoctor(): String? {
        return getAsString(PropertyList.ASCIIDOCTOR_ATTRIBUTES_EXPORT_PREFIX.getKey(), "")
    }

    override fun getBuildTimeStamp(): String {
        return getAsString(PropertyList.BUILD_TIMESTAMP.getKey())
    }

    override fun getClearCache(): Boolean {
        return getAsBoolean(PropertyList.CLEAR_CACHE.getKey())
    }

    fun setClearCache(clearCache: Boolean) {
        setProperty(PropertyList.CLEAR_CACHE.getKey(), clearCache)
    }

    override fun getContentFolder(): File? {
        return getAsFolder(CONTENT_FOLDER_KEY)
    }

    fun setContentFolder(contentFolder: File?) {
        if (contentFolder != null) {
            setProperty(CONTENT_FOLDER_KEY, contentFolder)
        }
    }

    override fun getContentFolderName(): String {
        return getAsString(PropertyList.CONTENT_FOLDER.getKey())
    }

    override fun getDataFolder(): File? {
        return getAsFolder(DATA_FOLDER_KEY)
    }

    fun setDataFolder(dataFolder: File?) {
        if (dataFolder != null) {
            setProperty(DATA_FOLDER_KEY, dataFolder)
        }
    }

    override fun getDataFolderName(): String {
        return getAsString(PropertyList.DATA_FOLDER.getKey())
    }

    override fun getDataFileDocType(): String {
        return getAsString(PropertyList.DATA_FILE_DOCTYPE.getKey())
    }

    fun setDataFileDocType(dataFileDocType: String?) {
        setProperty(PropertyList.DATA_FILE_DOCTYPE.getKey(), dataFileDocType)
    }

    override fun getDatabasePath(): String {
        return getAsString(PropertyList.DB_PATH.getKey())
    }

    fun setDatabasePath(path: String?) {
        setProperty(PropertyList.DB_PATH.getKey(), path)
    }

    override fun getDatabaseStore(): String {
        return getAsString(PropertyList.DB_STORE.getKey())
    }

    fun setDatabaseStore(storeType: String?) {
        setProperty(PropertyList.DB_STORE.getKey(), storeType)
    }


    override fun getDateFormat(): String {
        return getAsString(PropertyList.DATE_FORMAT.getKey())
    }

    override fun getDefaultStatus(): String? {
        return getAsString(PropertyList.DEFAULT_STATUS.getKey(), "")
    }

    fun setDefaultStatus(status: String?) {
        setProperty(PropertyList.DEFAULT_STATUS.getKey(), status)
    }

    override fun getDefaultType(): String? {
        return getAsString(PropertyList.DEFAULT_TYPE.getKey(), "")
    }

    fun setDefaultType(type: String?) {
        setProperty(PropertyList.DEFAULT_TYPE.getKey(), type)
    }

    override fun getDestinationFolder(): File? {
        return getAsFolder(DESTINATION_FOLDER_KEY)
    }

    override fun setDestinationFolder(destinationFolder: File?) {
        if (destinationFolder != null) {
            setProperty(DESTINATION_FOLDER_KEY, destinationFolder)
        }
    }

    override fun getDocumentTypes(): MutableList<String?> {
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

    override fun getDraftSuffix(): String? {
        return getAsString(PropertyList.DRAFT_SUFFIX.getKey(), "")
    }

    override fun getError404FileName(): String {
        return getAsString(PropertyList.ERROR404_FILE.getKey())
    }

    override fun getExampleProjectByType(templateType: String): String {
        return getAsString("example.project." + templateType)
    }

    override fun getExportAsciidoctorAttributes(): Boolean {
        return getAsBoolean(PropertyList.ASCIIDOCTOR_ATTRIBUTES_EXPORT.getKey())
    }

    override fun getFeedFileName(): String {
        return getAsString(PropertyList.FEED_FILE.getKey())
    }

    override fun getIgnoreFileName(): String {
        return getAsString(PropertyList.IGNORE_FILE.getKey())
    }

    override fun getIndexFileName(): String {
        return getAsString(PropertyList.INDEX_FILE.getKey())
    }

    override fun getKeys(): MutableIterator<String> {
        return compositeConfiguration.getKeys()
    }

    override fun getMarkdownExtensions(): MutableList<String?>? {
        return getAsList(PropertyList.MARKDOWN_EXTENSIONS.getKey())
    }

    fun setMarkdownExtensions(vararg extensions: String?) {
        setProperty(PropertyList.MARKDOWN_EXTENSIONS.getKey(), StringUtils.join(extensions, ","))
    }

    override fun getOutputExtension(): String {
        return getAsString(PropertyList.OUTPUT_EXTENSION.getKey())
    }

    fun setOutputExtension(outputExtension: String?) {
        setProperty(PropertyList.OUTPUT_EXTENSION.getKey(), outputExtension)
    }

    override fun getOutputExtensionByDocType(docType: String): String? {
        val templateExtensionKey: String = DOCTYPE_TEMPLATE_PREFIX + docType + DOCTYPE_EXTENSION_POSTFIX
        val defaultOutputExtension = getOutputExtension()
        return getAsString(templateExtensionKey, defaultOutputExtension)
    }

    override fun getPaginateIndex(): Boolean {
        return getAsBoolean(PropertyList.PAGINATE_INDEX.getKey())
    }

    fun setPaginateIndex(paginateIndex: Boolean) {
        setProperty(PropertyList.PAGINATE_INDEX.getKey(), paginateIndex)
    }

    override fun getPostsPerPage(): Int {
        return getAsInt(PropertyList.POSTS_PER_PAGE.getKey(), 5)
    }

    fun setPostsPerPage(postsPerPage: Int) {
        setProperty(PropertyList.POSTS_PER_PAGE.getKey(), postsPerPage)
    }

    override fun getPrefixForUriWithoutExtension(): String {
        return getAsString(PropertyList.URI_NO_EXTENSION_PREFIX.getKey())
    }

    fun setPrefixForUriWithoutExtension(prefix: String?) {
        setProperty(PropertyList.URI_NO_EXTENSION_PREFIX.getKey(), prefix)
    }

    override fun getRenderArchive(): Boolean {
        return getAsBoolean(PropertyList.RENDER_ARCHIVE.getKey())
    }

    override fun getRenderEncoding(): String {
        return getAsString(PropertyList.RENDER_ENCODING.getKey())
    }

    override fun getOutputEncoding(): String {
        return getAsString(PropertyList.OUTPUT_ENCODING.getKey())
    }

    override fun getRenderError404(): Boolean {
        return getAsBoolean(PropertyList.RENDER_ERROR404.getKey())
    }

    override fun getRenderFeed(): Boolean {
        return getAsBoolean(PropertyList.RENDER_FEED.getKey())
    }

    override fun getRenderIndex(): Boolean {
        return getAsBoolean(PropertyList.RENDER_INDEX.getKey())
    }

    override fun getRenderSiteMap(): Boolean {
        return getAsBoolean(PropertyList.RENDER_SITEMAP.getKey())
    }

    override fun getRenderTags(): Boolean {
        return getAsBoolean(PropertyList.RENDER_TAGS.getKey())
    }

    override fun getRenderTagsIndex(): Boolean {
        return compositeConfiguration.getBoolean(PropertyList.RENDER_TAGS_INDEX.getKey(), false)
    }

    fun setRenderTagsIndex(enable: Boolean) {
        compositeConfiguration.setProperty(PropertyList.RENDER_TAGS_INDEX.getKey(), enable)
    }

    override fun getSanitizeTag(): Boolean {
        return getAsBoolean(PropertyList.TAG_SANITIZE.getKey())
    }

    override fun getServerPort(): Int {
        return getAsInt(PropertyList.SERVER_PORT.getKey(), 8080)
    }

    fun setServerPort(port: Int) {
        setProperty(PropertyList.SERVER_PORT.getKey(), port)
    }

    override fun getSiteHost(): String? {
        return getAsString(PropertyList.SITE_HOST.getKey(), "http://www.jbake.org")
    }

    fun setSiteHost(siteHost: String?) {
        setProperty(PropertyList.SITE_HOST.getKey(), siteHost)
    }

    override fun getSiteMapFileName(): String {
        return getAsString(PropertyList.SITEMAP_FILE.getKey())
    }

    override fun getSourceFolder(): File? {
        return getAsFolder(SOURCE_FOLDER_KEY)
    }

    fun setSourceFolder(sourceFolder: File?) {
        setProperty(SOURCE_FOLDER_KEY, sourceFolder)
        setupPaths()
    }

    override fun getTagPathName(): String {
        return getAsString(PropertyList.TAG_PATH.getKey())
    }

    override fun getTemplateEncoding(): String {
        return getAsString(PropertyList.TEMPLATE_ENCODING.getKey())
    }

    override fun getTemplateByDocType(docType: String): String? {
        val templateKey: String = DOCTYPE_TEMPLATE_PREFIX + docType + DOCTYPE_FILE_POSTFIX
        val templateFileName = getAsString(templateKey)
        if (templateFileName != null) {
            return templateFileName
        }
        logger.warn("Cannot find configuration key '{}' for document type '{}'", templateKey, docType)
        return null
    }

    override fun getTemplateFileByDocType(docType: String): File? {
        val templateFileName = getTemplateByDocType(docType)
        if (templateFileName != null) {
            return File(getTemplateFolder(), templateFileName)
        }
        return null
    }

    override fun getTemplateFolder(): File? {
        return getAsFolder(TEMPLATE_FOLDER_KEY)
    }

    fun setTemplateFolder(templateFolder: File?) {
        if (templateFolder != null) {
            setProperty(TEMPLATE_FOLDER_KEY, templateFolder)
        }
    }

    override fun getTemplateFolderName(): String {
        return getAsString(PropertyList.TEMPLATE_FOLDER.getKey())
    }

    override fun getThymeleafLocale(): String {
        return getAsString(PropertyList.THYMELEAF_LOCALE.getKey())
    }

    override fun getUriWithoutExtension(): Boolean {
        return getAsBoolean(PropertyList.URI_NO_EXTENSION.getKey())
    }

    fun setUriWithoutExtension(withoutExtension: Boolean) {
        setProperty(PropertyList.URI_NO_EXTENSION.getKey(), withoutExtension)
    }

    override fun getVersion(): String {
        return getAsString(PropertyList.VERSION.getKey())
    }

    fun setDestinationFolderName(folderName: String?) {
        setProperty(PropertyList.DESTINATION_FOLDER.getKey(), folderName)
        setupDefaultDestination()
    }

    fun setExampleProject(type: String, fileName: String?) {
        val projectKey = "example.project." + type
        setProperty(projectKey, fileName)
    }

    override fun setProperty(key: String, value: Any?) {
        compositeConfiguration.setProperty(key, value)
    }

    override fun getThymeleafModeByType(type: String): String? {
        val key = "template_" + type + "_thymeleaf_mode"
        return getAsString(key, DEFAULT_TYHMELEAF_TEMPLATE_MODE)
    }

    override fun getServerContextPath(): String {
        return getAsString(PropertyList.SERVER_CONTEXT_PATH.getKey())
    }

    override fun getServerHostname(): String {
        return getAsString(PropertyList.SERVER_HOSTNAME.getKey())
    }

    override fun asHashMap(): MutableMap<String, Any>? {
        val configModel = HashMap<String, Any>()
        val configKeys = this.getKeys()
        while (configKeys.hasNext()) {
            val key = configKeys.next()
            val valueObject: Any?

            if (key == PropertyList.PAGINATE_INDEX.getKey()) {
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
        val destinationPath = getAsString(PropertyList.DESTINATION_FOLDER.getKey())

        val destination = File(destinationPath)
        if (destination.isAbsolute()) {
            setDestinationFolder(destination)
        } else {
            setDestinationFolder(File(getSourceFolder(), destinationPath))
        }
    }

    private fun setupDefaultAssetFolder() {
        val assetFolder = getAsString(PropertyList.ASSET_FOLDER.getKey())


        val asset = File(assetFolder)
        if (asset.isAbsolute()) {
            setAssetFolder(asset)
        } else {
            setAssetFolder(File(getSourceFolder(), assetFolder))
        }
    }

    private fun setupDefaultTemplateFolder() {
        val templateFolder = getAsString(PropertyList.TEMPLATE_FOLDER.getKey())

        val template = File(templateFolder)
        if (template.isAbsolute()) {
            setTemplateFolder(template)
        } else {
            setTemplateFolder(File(getSourceFolder(), templateFolder))
        }
    }

    private fun setupDefaultDataFolder() {
        val dataFolder = getAsString(PropertyList.DATA_FOLDER.getKey())

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

    override fun getHeaderSeparator(): String {
        return getAsString(PropertyList.HEADER_SEPARATOR.getKey())
    }

    fun setHeaderSeparator(headerSeparator: String?) {
        setProperty(PropertyList.HEADER_SEPARATOR.getKey(), headerSeparator)
    }

    override fun getImgPathPrependHost(): Boolean {
        return getAsBoolean(PropertyList.IMG_PATH_PREPEND_HOST.getKey())
    }

    fun setImgPathPrependHost(imgPathPrependHost: Boolean) {
        setProperty(PropertyList.IMG_PATH_PREPEND_HOST.getKey(), imgPathPrependHost)
    }

    override fun getImgPathUpdate(): Boolean {
        return getAsBoolean(PropertyList.IMG_PATH_UPDATE.getKey())
    }

    fun setImgPathUPdate(imgPathUpdate: Boolean) {
        setProperty(PropertyList.IMG_PATH_UPDATE.getKey(), imgPathUpdate)
    }

    override fun getJbakeProperties(): MutableList<Property?> {
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

    override fun getAbbreviatedGitHash(): String {
        return getAsString(PropertyList.GIT_HASH.getKey())
    }

    override fun getJvmLocale(): String {
        return getAsString(PropertyList.JVM_LOCALE.getKey())
    }

    override fun getFreemarkerTimeZone(): TimeZone? {
        val timezone = getAsString(PropertyList.FREEMARKER_TIMEZONE.getKey())
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
