package org.jbake.app.configuration;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.SystemConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.jbake.app.configuration.PropertyList.*;

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
    private final Logger logger = LoggerFactory.getLogger(DefaultJBakeConfiguration.class);
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
        setupPaths();
    }

    @Override
    public Object get(String key) {
        return compositeConfiguration.getProperty(key);
    }

    @Override
    public String getArchiveFileName() {
        return getAsString(PropertyList.ARCHIVE_FILE.getKey());
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
        return getAsList(ASCIIDOCTOR_ATTRIBUTES.getKey());
    }

    public Object getAsciidoctorOption(String optionKey) {
        Configuration subConfig = compositeConfiguration.subset(ASCIIDOCTOR_OPTION.getKey());
        Object value = subConfig.getProperty(optionKey);

        if (value == null) {
            logger.warn("Cannot find asciidoctor option '{}.{}'", ASCIIDOCTOR_OPTION.getKey(), optionKey);
            return "";
        }
        return value;
    }

    @Override
    public List<String> getAsciidoctorOptionKeys() {
        List<String> options = new ArrayList<>();
        Configuration subConfig = compositeConfiguration.subset(ASCIIDOCTOR_OPTION.getKey());

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
        }
    }

    @Override
    public String getAssetFolderName() {
        return getAsString(ASSET_FOLDER.getKey());
    }

    @Override
    public boolean getAssetIgnoreHidden() {
        return getAsBoolean(ASSET_IGNORE_HIDDEN.getKey());
    }

    public void setAssetIgnoreHidden(boolean assetIgnoreHidden) {
        setProperty(ASSET_IGNORE_HIDDEN.getKey(), assetIgnoreHidden);
    }

    @Override
    public String getAttributesExportPrefixForAsciidoctor() {
        return getAsString(ASCIIDOCTOR_ATTRIBUTES_EXPORT_PREFIX.getKey(), "");
    }

    @Override
    public String getBuildTimeStamp() {
        return getAsString(BUILD_TIMESTAMP.getKey());
    }

    @Override
    public boolean getClearCache() {
        return getAsBoolean(CLEAR_CACHE.getKey());
    }

    public void setClearCache(boolean clearCache) {
        setProperty(CLEAR_CACHE.getKey(), clearCache);
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
        }
    }

    @Override
    public String getContentFolderName() {
        return getAsString(CONTENT_FOLDER.getKey());
    }

    @Override
    public String getDatabasePath() {
        return getAsString(DB_PATH.getKey());
    }

    public void setDatabasePath(String path) {
        setProperty(DB_PATH.getKey(), path);
    }

    @Override
    public String getDatabaseStore() {
        return getAsString(DB_STORE.getKey());
    }

    public void setDatabaseStore(String storeType) {
        setProperty(DB_STORE.getKey(), storeType);
    }

    @Override
    public String getDateFormat() {
        return getAsString(DATE_FORMAT.getKey());
    }

    @Override
    public String getDefaultStatus() {
        return getAsString(DEFAULT_STATUS.getKey(), "");
    }

    public void setDefaultStatus(String status) {
        setProperty(DEFAULT_STATUS.getKey(), status);
    }

    @Override
    public String getDefaultType() {
        return getAsString(DEFAULT_TYPE.getKey(), "");
    }

    public void setDefaultType(String type) {
        setProperty(DEFAULT_TYPE.getKey(), type);
    }

    @Override
    public File getDestinationFolder() {
        return getAsFolder(DESTINATION_FOLDER_KEY);
    }

    public void setDestinationFolder(File destinationFolder) {
        if (destinationFolder != null) {
            setProperty(DESTINATION_FOLDER_KEY, destinationFolder);
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
        return getAsString(DRAFT_SUFFIX.getKey(), "");
    }

    @Override
    public String getExampleProjectByType(String templateType) {
        return getAsString("example.project." + templateType);
    }

    @Override
    public boolean getExportAsciidoctorAttributes() {
        return getAsBoolean(ASCIIDOCTOR_ATTRIBUTES_EXPORT.getKey());
    }

    @Override
    public String getFeedFileName() {
        return getAsString(FEED_FILE.getKey());
    }

    @Override
    public String getIndexFileName() {
        return getAsString(INDEX_FILE.getKey());
    }

    @Override
    public Iterator<String> getKeys() {
        return compositeConfiguration.getKeys();
    }

    @Override
    public List<String> getMarkdownExtensions() {
        return getAsList(MARKDOWN_EXTENSIONS.getKey());
    }

    public void setMarkdownExtensions(String... extensions) {
        setProperty(MARKDOWN_EXTENSIONS.getKey(), StringUtils.join(extensions, ","));
    }

    @Override
    public String getOutputExtension() {
        return getAsString(OUTPUT_EXTENSION.getKey());
    }

    public void setOutputExtension(String outputExtension) {
        setProperty(OUTPUT_EXTENSION.getKey(), outputExtension);
    }

    @Override
    public String getOutputExtensionByDocType(String docType) {
        String templateExtensionKey = DOCTYPE_TEMPLATE_PREFIX + docType + DOCTYPE_EXTENSION_POSTFIX;
        String defaultOutputExtension = getOutputExtension();
        return getAsString(templateExtensionKey, defaultOutputExtension);
    }

    @Override
    public boolean getPaginateIndex() {
        return getAsBoolean(PAGINATE_INDEX.getKey());
    }

    public void setPaginateIndex(boolean paginateIndex) {
        setProperty(PAGINATE_INDEX.getKey(), paginateIndex);
    }

    @Override
    public int getPostsPerPage() {
        return getAsInt(POSTS_PER_PAGE.getKey(), 5);
    }

    public void setPostsPerPage(int postsPerPage) {
        setProperty(POSTS_PER_PAGE.getKey(), postsPerPage);
    }

    @Override
    public String getPrefixForUriWithoutExtension() {
        return getAsString(URI_NO_EXTENSION_PREFIX.getKey());
    }

    public void setPrefixForUriWithoutExtension(String prefix) {
        setProperty(URI_NO_EXTENSION_PREFIX.getKey(), prefix);
    }

    @Override
    public boolean getRenderArchive() {
        return getAsBoolean(RENDER_ARCHIVE.getKey());
    }

    @Override
    public String getRenderEncoding() {
        return getAsString(RENDER_ENCODING.getKey());
    }

    @Override
    public boolean getRenderFeed() {
        return getAsBoolean(RENDER_FEED.getKey());
    }

    @Override
    public boolean getRenderIndex() {
        return getAsBoolean(RENDER_INDEX.getKey());
    }

    @Override
    public boolean getRenderSiteMap() {
        return getAsBoolean(RENDER_SITEMAP.getKey());
    }

    @Override
    public boolean getRenderTags() {
        return getAsBoolean(RENDER_TAGS.getKey());
    }

    @Override
    public boolean getRenderTagsIndex() {
        return compositeConfiguration.getBoolean(RENDER_TAGS_INDEX.getKey(), false);
    }

    public void setRenderTagsIndex(boolean enable) {
        compositeConfiguration.setProperty(RENDER_TAGS_INDEX.getKey(), enable);
    }

    @Override
    public boolean getSanitizeTag() {
        return getAsBoolean(TAG_SANITIZE.getKey());
    }

    @Override
    public int getServerPort() {
        return getAsInt(SERVER_PORT.getKey(), 8080);
    }

    public void setServerPort(int port) {
        setProperty(SERVER_PORT.getKey(), port);
    }

    @Override
    public String getSiteHost() {
        return getAsString(SITE_HOST.getKey(), "http://www.jbake.org");
    }

    public void setSiteHost(String siteHost) {
        setProperty(SITE_HOST.getKey(), siteHost);
    }

    @Override
    public String getSiteMapFileName() {
        return getAsString(SITEMAP_FILE.getKey());
    }

    @Override
    public File getSourceFolder() {
        return getAsFolder(SOURCE_FOLDER_KEY);
    }

    public void setSourceFolder(File sourceFolder) {
        setProperty(SOURCE_FOLDER_KEY, sourceFolder);
        setupPaths();
    }

    @Override
    public String getTagPathName() {
        return getAsString(TAG_PATH.getKey());
    }

    @Override
    public String getTemplateEncoding() {
        return getAsString(TEMPLATE_ENCODING.getKey());
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
        }
    }

    @Override
    public String getTemplateFolderName() {
        return getAsString(TEMPLATE_FOLDER.getKey());
    }

    @Override
    public String getThymeleafLocale() {
        return getAsString(THYMELEAF_LOCALE.getKey());
    }

    @Override
    public boolean getUriWithoutExtension() {
        return getAsBoolean(URI_NO_EXTENSION.getKey());
    }

    public void setUriWithoutExtension(boolean withoutExtension) {
        setProperty(URI_NO_EXTENSION.getKey(), withoutExtension);
    }

    @Override
    public String getVersion() {
        return getAsString(VERSION.getKey());
    }

    public void setDestinationFolderName(String folderName) {
        setProperty(DESTINATION_FOLDER.getKey(), folderName);
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

    @Override
    public String getServerContextPath() {
        return getAsString(SERVER_CONTEXT_PATH.getKey());
    }

    @Override
    public String getServerHostname() {
        return getAsString(SERVER_HOSTNAME.getKey());
    }

    public void setTemplateExtensionForDocType(String docType, String extension) {
        String templateExtensionKey = DOCTYPE_TEMPLATE_PREFIX + docType + DOCTYPE_EXTENSION_POSTFIX;
        setProperty(templateExtensionKey, extension);
    }

    public void setTemplateFileNameForDocType(String docType, String fileName) {
        String templateKey = DOCTYPE_TEMPLATE_PREFIX + docType + DOCTYPE_FILE_POSTFIX;
        setProperty(templateKey, fileName);
    }

    private void setupPaths() {
        setupDefaultDestination();
        setupDefaultAssetFolder();
        setupDefaultTemplateFolder();
        setupDefaultContentFolder();
    }

    private void setupDefaultDestination() {
        String destinationPath = getAsString(DESTINATION_FOLDER.getKey());

        File destination = new File(destinationPath);
        if ( destination.isAbsolute() ) {
            setDestinationFolder(destination);
        } else {
            setDestinationFolder(new File(getSourceFolder(), destinationPath));
        }
    }

    private void setupDefaultAssetFolder() {
        String assetFolder = getAsString(ASSET_FOLDER.getKey());


        File asset = new File(assetFolder);
        if(asset.isAbsolute()) {
            setAssetFolder(asset);
        } else {
            setAssetFolder(new File(getSourceFolder(), assetFolder));
        }
    }

    private void setupDefaultTemplateFolder() {
        String templateFolder = getAsString(TEMPLATE_FOLDER.getKey());

        File template = new File(templateFolder);
        if(template.isAbsolute()) {
            setTemplateFolder(template);
        } else {
            setTemplateFolder(new File(getSourceFolder(), templateFolder));
        }
    }

    private void setupDefaultContentFolder() {
        setContentFolder(new File(getSourceFolder(), getContentFolderName()));
    }

    @Override
    public String getHeaderSeparator() {
        return getAsString(HEADER_SEPARATOR.getKey());
    }

    public void setHeaderSeparator(String headerSeparator) {
        setProperty(HEADER_SEPARATOR.getKey(), headerSeparator);
    }

    @Override
    public boolean getImgPathPrependHost() {
        return getAsBoolean(IMG_PATH_PREPEND_HOST.getKey());
    }

    public void setImgPathPrependHost(boolean imgPathPrependHost) {
        setProperty(IMG_PATH_PREPEND_HOST.getKey(), imgPathPrependHost);
    }

    @Override
    public boolean getImgPathUpdate() {
        return getAsBoolean(IMG_PATH_UPDATE.getKey());
    }

    public void setImgPathUPdate(boolean imgPathUpdate) {
        setProperty(IMG_PATH_UPDATE.getKey(), imgPathUpdate);
    }

    public List<Property> getJbakeProperties() {

        List<Property> jbakeKeys = new ArrayList<>();

        for (int i = 0; i < compositeConfiguration.getNumberOfConfigurations(); i++) {
            Configuration configuration = compositeConfiguration.getConfiguration(i);

            if (!(configuration instanceof SystemConfiguration)) {
                for (Iterator<String> it = configuration.getKeys(); it.hasNext(); ) {
                    String key = it.next();
                    Property property = PropertyList.getPropertyByKey(key);
                    if (!jbakeKeys.contains(property)) {
                        jbakeKeys.add(property);
                    }
                }
            }
        }
        Collections.sort(jbakeKeys);
        return jbakeKeys;
    }
}
