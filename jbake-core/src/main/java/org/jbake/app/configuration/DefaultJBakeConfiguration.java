package org.jbake.app.configuration;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The default implementation of a {@link JBakeConfiguration}
 */
public class DefaultJBakeConfiguration implements JBakeConfiguration {


    private static final String SOURCE_FOLDER_KEY = "sourceFolder";
    private static final String DESTINATION_FOLDER_KEY = "destinationFolder";
    private static final String ASSET_FOLDER_KEY = "assetFolder";
    private static final String TEMPLATE_FOLDER_KEY = "templateFolder";
    private static final String CONTENT_FOLDER_KEY = "contentFolder";
    private static final Pattern TEMPLATE_DOC_PATTERN = Pattern.compile("(?:template\\.)([a-zA-Z0-9-_]+)(?:\\.file)");
    private static final String DOCTYPE_FILE_POSTFIX = ".file";
    private static final String DOCTYPE_EXTENSION_POSTFIX = ".extension";
    private static final String DOCTYPE_TEMPLATE_PREFIX = "template.";
    private Logger logger = LoggerFactory.getLogger(DefaultJBakeConfiguration.class);
    private CompositeConfiguration compositeConfiguration;

    /**
     * Some deprecated implementations just need access to the configuration without access to the source folder
     *
     * @param configuration The project configuration
     * @deprecated use {@link #DefaultJBakeConfiguration(File, CompositeConfiguration)} instead
     */
    @Deprecated
    public DefaultJBakeConfiguration(CompositeConfiguration configuration) {
        this.compositeConfiguration = configuration;
    }

    public DefaultJBakeConfiguration(File sourceFolder, CompositeConfiguration configuration) {
        this.compositeConfiguration = configuration;
        setSourceFolder(sourceFolder);
        setupDefaultDestination();
        setupPathsRelativeToSourceFile();
    }

    @Override
    public Object get(String key) {
        return compositeConfiguration.getProperty(key);
    }

    @Override
    public String getArchiveFileName() {
        return getAsString(JBakeProperty.ARCHIVE_FILE);
    }

    private boolean getAsBoolean(String key) {
        return compositeConfiguration.getBoolean(key, false);
    }

    private File getAsFolder(String key) {
        return (File) get(key);
    }

    private int getAsInt(String key, int defaultValue) {
        return compositeConfiguration.getInt(key, defaultValue);
    }

    private List<String> getAsList(String key) {
        return Arrays.asList(compositeConfiguration.getStringArray(key));
    }

    private String getAsString(String key) {
        return compositeConfiguration.getString(key);
    }

    private String getAsString(String key, String defaultValue) {
        return compositeConfiguration.getString(key, defaultValue);
    }

    @Override
    public List<String> getAsciidoctorAttributes() {
        return getAsList(JBakeProperty.ASCIIDOCTOR_ATTRIBUTES);
    }

    public Object getAsciidoctorOption(String optionKey) {
        Configuration subConfig = compositeConfiguration.subset(JBakeProperty.ASCIIDOCTOR_OPTION);
        Object value = subConfig.getProperty(optionKey);

        if (value == null) {
            logger.warn("Cannot find asciidoctor option '{}.{}'", JBakeProperty.ASCIIDOCTOR_OPTION, optionKey);
            return "";
        }
        return value;
    }

    @Override
    public List<String> getAsciidoctorOptionKeys() {
        List<String> options = new ArrayList<>();
        Configuration subConfig = compositeConfiguration.subset(JBakeProperty.ASCIIDOCTOR_OPTION);

        Iterator<String> iterator = subConfig.getKeys();
        while (iterator.hasNext()) {
            String key = iterator.next();
            options.add(key);
        }

        return options;
    }

    @Override
    public File getAssetFolder() {
        return getAsFolder(ASSET_FOLDER_KEY);
    }

    public void setAssetFolder(File assetFolder) {
        if (assetFolder != null) {
            setProperty(ASSET_FOLDER_KEY, assetFolder);
            setProperty(JBakeProperty.ASSET_FOLDER, assetFolder.getName());
        }
    }

    @Override
    public String getAssetFolderName() {
        return getAsString(JBakeProperty.ASSET_FOLDER);
    }

    @Override
    public boolean getAssetIgnoreHidden() {
        return getAsBoolean(JBakeProperty.ASSET_IGNORE_HIDDEN);
    }

    public void setAssetIgnoreHidden(boolean assetIgnoreHidden) {
        setProperty(JBakeProperty.ASSET_IGNORE_HIDDEN, assetIgnoreHidden);
    }

    @Override
    public String getAttributesExportPrefixForAsciidoctor() {
        return getAsString(JBakeProperty.ASCIIDOCTOR_ATTRIBUTES_EXPORT_PREFIX, "");
    }

    @Override
    public String getBuildTimeStamp() {
        return getAsString(JBakeProperty.BUILD_TIMESTAMP);
    }

    @Override
    public boolean getClearCache() {
        return getAsBoolean(JBakeProperty.CLEAR_CACHE);
    }

    public void setClearCache(boolean clearCache) {
        setProperty(JBakeProperty.CLEAR_CACHE, clearCache);
    }

    public CompositeConfiguration getCompositeConfiguration() {
        return compositeConfiguration;
    }

    public void setCompositeConfiguration(CompositeConfiguration configuration) {
        this.compositeConfiguration = configuration;
    }

    @Override
    public File getContentFolder() {
        return getAsFolder(CONTENT_FOLDER_KEY);
    }

    public void setContentFolder(File contentFolder) {
        if (contentFolder != null) {
            setProperty(CONTENT_FOLDER_KEY, contentFolder);
            setProperty(JBakeProperty.CONTENT_FOLDER, contentFolder.getName());
        }
    }

    @Override
    public String getContentFolderName() {
        return getAsString(JBakeProperty.CONTENT_FOLDER);
    }

    @Override
    public String getDatabasePath() {
        return getAsString(JBakeProperty.DB_PATH);
    }

    public void setDatabasePath(String path) {
        setProperty(JBakeProperty.DB_PATH, path);
    }

    @Override
    public String getDatabaseStore() {
        return getAsString(JBakeProperty.DB_STORE);
    }

    public void setDatabaseStore(String storeType) {
        setProperty(JBakeProperty.DB_STORE, storeType);
    }

    @Override
    public String getDateFormat() {
        return getAsString(JBakeProperty.DATE_FORMAT);
    }

    @Override
    public String getDefaultStatus() {
        return getAsString(JBakeProperty.DEFAULT_STATUS, "");
    }

    public void setDefaultStatus(String status) {
        setProperty(JBakeProperty.DEFAULT_STATUS, status);
    }

    @Override
    public String getDefaultType() {
        return getAsString(JBakeProperty.DEFAULT_TYPE, "");
    }

    public void setDefaultType(String type) {
        setProperty(JBakeProperty.DEFAULT_TYPE, type);
    }

    @Override
    public File getDestinationFolder() {
        return getAsFolder(DESTINATION_FOLDER_KEY);
    }

    public void setDestinationFolder(File destinationFolder) {
        if (destinationFolder != null) {
            setProperty(DESTINATION_FOLDER_KEY, destinationFolder);
            setProperty(JBakeProperty.DESTINATION_FOLDER, destinationFolder.getName());
        }
    }

    @Override
    public List<String> getDocumentTypes() {
        List<String> docTypes = new ArrayList<>();
        Iterator<String> keyIterator = compositeConfiguration.getKeys();
        while (keyIterator.hasNext()) {
            String key = keyIterator.next();
            Matcher matcher = TEMPLATE_DOC_PATTERN.matcher(key);
            if (matcher.find()) {
                docTypes.add(matcher.group(1));
            }
        }

        return docTypes;
    }

    @Override
    public String getDraftSuffix() {
        return getAsString(JBakeProperty.DRAFT_SUFFIX, "");
    }

    @Override
    public String getExampleProjectByType(String templateType) {
        return getAsString("example.project." + templateType);
    }

    @Override
    public boolean getExportAsciidoctorAttributes() {
        return getAsBoolean(JBakeProperty.ASCIIDOCTOR_ATTRIBUTES_EXPORT);
    }

    @Override
    public String getFeedFileName() {
        return getAsString(JBakeProperty.FEED_FILE);
    }

    @Override
    public String getIndexFileName() {
        return getAsString(JBakeProperty.INDEX_FILE);
    }

    @Override
    public Iterator<String> getKeys() {
        return compositeConfiguration.getKeys();
    }

    @Override
    public List<String> getMarkdownExtensions() {
        return getAsList(JBakeProperty.MARKDOWN_EXTENSIONS);
    }

    public void setMarkdownExtensions(String... extensions) {
        setProperty(JBakeProperty.MARKDOWN_EXTENSIONS, StringUtils.join(extensions, ","));
    }

    @Override
    public String getOutputExtension() {
        return getAsString(JBakeProperty.OUTPUT_EXTENSION);
    }

    public void setOutputExtension(String outputExtension) {
        setProperty(JBakeProperty.OUTPUT_EXTENSION, outputExtension);
    }

    @Override
    public String getOutputExtensionByDocType(String docType) {
        String templateExtensionKey = DOCTYPE_TEMPLATE_PREFIX + docType + DOCTYPE_EXTENSION_POSTFIX;
        String defaultOutputExtension = getOutputExtension();
        return getAsString(templateExtensionKey, defaultOutputExtension);
    }

    @Override
    public boolean getPaginateIndex() {
        return getAsBoolean(JBakeProperty.PAGINATE_INDEX);
    }

    public void setPaginateIndex(boolean paginateIndex) {
        setProperty(JBakeProperty.PAGINATE_INDEX, paginateIndex);
    }

    @Override
    public int getPostsPerPage() {
        return getAsInt(JBakeProperty.POSTS_PER_PAGE, 5);
    }

    public void setPostsPerPage(int postsPerPage) {
        setProperty(JBakeProperty.POSTS_PER_PAGE, postsPerPage);
    }

    @Override
    public String getPrefixForUriWithoutExtension() {
        return getAsString(JBakeProperty.URI_NO_EXTENSION_PREFIX);
    }

    public void setPrefixForUriWithoutExtension(String prefix) {
        setProperty(JBakeProperty.URI_NO_EXTENSION_PREFIX, prefix);
    }

    @Override
    public boolean getRenderArchive() {
        return getAsBoolean(JBakeProperty.RENDER_ARCHIVE);
    }

    @Override
    public String getRenderEncoding() {
        return getAsString(JBakeProperty.RENDER_ENCODING);
    }

    @Override
    public boolean getRenderFeed() {
        return getAsBoolean(JBakeProperty.RENDER_FEED);
    }

    @Override
    public boolean getRenderIndex() {
        return getAsBoolean(JBakeProperty.RENDER_INDEX);
    }

    @Override
    public boolean getRenderSiteMap() {
        return getAsBoolean(JBakeProperty.RENDER_SITEMAP);
    }

    @Override
    public boolean getRenderTags() {
        return getAsBoolean(JBakeProperty.RENDER_TAGS);
    }

    @Override
    public boolean getRenderTagsIndex() {
        return compositeConfiguration.getBoolean(JBakeProperty.RENDER_TAGS_INDEX, false);
    }

    public void setRenderTagsIndex(boolean enable) {
        compositeConfiguration.setProperty(JBakeProperty.RENDER_TAGS_INDEX, enable);
    }

    @Override
    public boolean getSanitizeTag() {
        return getAsBoolean(JBakeProperty.TAG_SANITIZE);
    }

    @Override
    public int getServerPort() {
        return getAsInt(JBakeProperty.SERVER_PORT, 8080);
    }

    public void setServerPort(int port) {
        setProperty(JBakeProperty.SERVER_PORT, port);
    }

    @Override
    public String getSiteHost() {
        return getAsString(JBakeProperty.SITE_HOST, "http://www.jbake.org");
    }

    public void setSiteHost(String siteHost) {
        setProperty(JBakeProperty.SITE_HOST, siteHost);
    }

    @Override
    public String getSiteMapFileName() {
        return getAsString(JBakeProperty.SITEMAP_FILE);
    }

    @Override
    public File getSourceFolder() {
        return getAsFolder(SOURCE_FOLDER_KEY);
    }

    public void setSourceFolder(File sourceFolder) {
        setProperty(SOURCE_FOLDER_KEY, sourceFolder);
        setupPathsRelativeToSourceFile();
    }

    @Override
    public String getTagPathName() {
        return getAsString(JBakeProperty.TAG_PATH);
    }

    @Override
    public String getTemplateEncoding() {
        return getAsString(JBakeProperty.TEMPLATE_ENCODING);
    }

    @Override
    public File getTemplateFileByDocType(String docType) {
        String templateKey = DOCTYPE_TEMPLATE_PREFIX + docType + DOCTYPE_FILE_POSTFIX;
        String templateFileName = getAsString(templateKey);
        if (templateFileName != null) {
            return new File(getTemplateFolder(), templateFileName);
        }
        logger.warn("Cannot find configuration key '{}' for document type '{}'", templateKey, docType);
        return null;
    }

    @Override
    public File getTemplateFolder() {
        return getAsFolder(TEMPLATE_FOLDER_KEY);
    }

    public void setTemplateFolder(File templateFolder) {
        if (templateFolder != null) {
            setProperty(TEMPLATE_FOLDER_KEY, templateFolder);
            setProperty(JBakeProperty.TEMPLATE_FOLDER, templateFolder.getName());
        }
    }

    @Override
    public String getTemplateFolderName() {
        return getAsString(JBakeProperty.TEMPLATE_FOLDER);
    }

    @Override
    public String getThymeleafLocale() {
        return getAsString(JBakeProperty.THYMELEAF_LOCALE);
    }

    @Override
    public boolean getUriWithoutExtension() {
        return getAsBoolean(JBakeProperty.URI_NO_EXTENSION);
    }

    public void setUriWithoutExtension(boolean withoutExtension) {
        setProperty(JBakeProperty.URI_NO_EXTENSION, withoutExtension);
    }

    @Override
    public String getVersion() {
        return getAsString(JBakeProperty.VERSION);
    }

    public void setDestinationFolderName(String folderName) {
        setProperty(JBakeProperty.DESTINATION_FOLDER, folderName);
        setupDefaultDestination();
    }

    public void setExampleProject(String type, String fileName) {
        String projectKey = "example.project." + type;
        setProperty(projectKey, fileName);
    }

    @Override
    public void setProperty(String key, Object value) {
        compositeConfiguration.setProperty(key, value);
    }

    public void setTemplateExtensionForDocType(String docType, String extension) {
        String templateExtensionKey = DOCTYPE_TEMPLATE_PREFIX + docType + DOCTYPE_EXTENSION_POSTFIX;
        setProperty(templateExtensionKey, extension);
    }

    public void setTemplateFileNameForDocType(String docType, String fileName) {
        String templateKey = DOCTYPE_TEMPLATE_PREFIX + docType + DOCTYPE_FILE_POSTFIX;
        setProperty(templateKey, fileName);
    }

    private void setupDefaultAssetFolder() {
        String assetFolder = getAsString(JBakeProperty.ASSET_FOLDER);
        setAssetFolder(new File(getSourceFolder(), assetFolder));
    }

    private void setupDefaultContentFolder() {
        setContentFolder(new File(getSourceFolder(), getContentFolderName()));
    }

    private void setupDefaultDestination() {
        String destinationPath = getAsString(JBakeProperty.DESTINATION_FOLDER);
        setDestinationFolder(new File(getSourceFolder(), destinationPath));
    }

    private void setupDefaultTemplateFolder() {
        String destinationPath = getAsString(JBakeProperty.TEMPLATE_FOLDER);
        setTemplateFolder(new File(getSourceFolder(), destinationPath));
    }

    private void setupPathsRelativeToSourceFile() {
        setupDefaultAssetFolder();
        setupDefaultTemplateFolder();
        setupDefaultContentFolder();
    }

    @Override
    public String getHeaderSeparator() {
        return getAsString(JBakeProperty.HEADER_SEPARATOR);
    }

    public void setHeaderSeparator(String headerSeparator) {
        setProperty(JBakeProperty.HEADER_SEPARATOR, headerSeparator);
    }

    @Override
    public boolean getImgPathPrependHost() {
        return getAsBoolean(JBakeProperty.IMG_PATH_PREPEND_HOST);
    }

    public void setImgPathPrependHost(boolean imgPathPrependHost) {
        setProperty(JBakeProperty.IMG_PATH_PREPEND_HOST, imgPathPrependHost);
    }

    @Override
    public boolean getImgPathUpdate() {
        return getAsBoolean(JBakeProperty.IMG_PATH_UPDATE);
    }

    public void setImgPathUPdate(boolean imgPathUpdate) {
        setProperty(JBakeProperty.IMG_PATH_UPDATE, imgPathUpdate);
    }
}
