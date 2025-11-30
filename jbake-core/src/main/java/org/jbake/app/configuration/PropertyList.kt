package org.jbake.app.configuration

/**
 * Enum of all JBake configuration properties.
 */
enum class PropertyList(
    override val key: String,
    override val description: String,
    override val group: JBakeProperty.Group = JBakeProperty.Group.DEFAULT
) : JBakeProperty {

    ARCHIVE_FILE("archive.file", "Output filename for archive file"),
    ASCIIDOCTOR_ATTRIBUTES("asciidoctor.attributes", "attributes to be set when processing input"),
    ASCIIDOCTOR_ATTRIBUTES_EXPORT("asciidoctor.attributes.export", "Prefix to be used when exporting JBake properties to Asciidoctor"),
    ASCIIDOCTOR_ATTRIBUTES_EXPORT_PREFIX("asciidoctor.attributes.export.prefix", "prefix that should be used when JBake config options are exported"),
    ASCIIDOCTOR_OPTION("asciidoctor.option", "default asciidoctor options"),
    ASSET_FOLDER("asset.folder", "folder that contains all asset files"),
    ASSET_IGNORE_HIDDEN("asset.ignore", "Flag indicating if hidden asset resources should be ignored"),
    BUILD_TIMESTAMP("build.timestamp", "timestamp jbake was build"),
    CLEAR_CACHE("db.clear.cache", "clear database cache"),
    CONTENT_FOLDER("content.folder", "folder that contains all content files"),
    DATA_FOLDER("data.folder", "folder that contains all data files"),
    DATA_FILE_DOCTYPE("data.file.docType", "document type to use for data files"),
    DATE_FORMAT("date.format", "default date format used in content files"),
    DB_STORE("db.store", "database store (plocal, memory)"),
    DB_PATH("db.path", "database path for persistent storage"),
    DEFAULT_STATUS("default.status", "default document status"),
    DEFAULT_TYPE("default.type", "default document type"),
    DESTINATION_FOLDER("destination.folder", "path to destination dir by default"),
    DRAFT_SUFFIX("draft.suffix", "draft content suffix"),
    ERROR404_FILE("error404.file", "filename to use for 404 error"),
    FEED_FILE("feed.file", "filename to use for feed"),
    FREEMARKER_TIMEZONE("freemarker.timezone", "TimeZone to use within Freemarker"),
    GIT_HASH("git.hash", "abbreviated git hash"),
    HEADER_SEPARATOR("header.separator", "String used to separate the header from the body"),
    IGNORE_FILE("ignore.file", "file used to ignore a directory"),
    IMG_PATH_UPDATE("img.path.update", "update image path?"),
    IMG_PATH_PREPEND_HOST("img.path.prepend.host", "Prepend site.host to image paths"),
    INDEX_FILE("index.file", "filename to use for index file"),
    JVM_LOCALE("jvm.locale", "locale for the jvm"),
    MARKDOWN_EXTENSIONS("markdown.extensions", "comma delimited default markdown extensions; for available extensions: http://www.decodified.com/pegdown/api/org/pegdown/Extensions.html"),
    OUTPUT_ENCODING("freemarker.outputencoding", "default output_encoding setting for freemarker"),
    OUTPUT_EXTENSION("output.extension", "file extension for output content files"),
    PAGINATE_INDEX("index.paginate", "paginate index?"),
    POSTS_PER_PAGE("index.posts_per_page", "number of post per page for pagination"),
    RENDER_ARCHIVE("render.archive", "render archive file?"),
    RENDER_ENCODING("render.encoding", "character encoding MIME name used in templates. use one of http://www.iana.org/assignments/character-sets/character-sets.xhtml"),
    RENDER_ERROR404("render.error404", "render 404 page?"),
    RENDER_FEED("render.feed", "render feed file?"),
    RENDER_INDEX("render.index", "render index file?"),
    RENDER_SITEMAP("render.sitemap", "render sitemap.xml file?"),
    RENDER_TAGS("render.tags", "render tag files?"),
    RENDER_TAGS_INDEX("render.tagsindex", "render tag index file?"),
    SERVER_PORT("server.port", "default server port"),
    SERVER_HOSTNAME("server.hostname", "default server hostname"),
    SERVER_CONTEXT_PATH("server.contextPath", "default server context path"),
    SITE_HOST("site.host", "site host"),
    SITEMAP_FILE("sitemap.file", "filename to use for sitemap file"),
    TAG_SANITIZE("tag.sanitize", "sanitize tag value before it is used as filename (i.e. replace spaces with hyphens)"),
    TAG_PATH("tag.path", "folder name to use for tag files"),
    TEMPLATE_FOLDER("template.folder", "folder that contains all template files"),
    TEMPLATE_ENCODING("template.encoding", "character encoding MIME name used in templates. use one of http://www.iana.org/assignments/character-sets/character-sets.xhtml"),
    TEMPLATE_MASTERINDEX_FILE("template.masterindex.file", "filename of masterindex template file"),
    TEMPLATE_FEED_FILE("template.feed.file", "filename of feed template file"),
    TEMPLATE_ARCHIVE_FILE("template.archive.file", "filename of archive template file"),
    TEMPLATE_TAG_FILE("template.tag.file", "filename of tag template file"),
    TEMPLATE_TAGSINDEX_FILE("template.tagsindex.file", "filename of tag index template file"),
    TEMPLATE_SITEMAP_FILE("template.sitemap.file", "filename of sitemap template file"),
    TEMPLATE_POST_FILE("template.post.file", "filename of post template file"),
    TEMPLATE_PAGE_FILE("template.page.file", "filename of page template file"),
    EXAMPLE_PROJECT_FREEMARKER("example.project.freemarker", "zip file containing example project structure using freemarker templates"),
    EXAMPLE_PROJECT_GROOVY("example.project.groovy", "zip file containing example project structure using groovy templates"),
    EXAMPLE_PROJECT_GROOVY_MTE("example.project.groovy-mte", "zip file containing example project structure using groovy markup templates"),
    EXAMPLE_PROJECT_THYMELEAF("example.project.thymeleaf", "zip file containing example project structure using thymeleaf templates"),
    EXAMPLE_PROJECT_JADE("example.project.jade", "zip file containing example project structure using jade templates"),
    MARKDOWN_MAX_PARSINGTIME("markdown.maxParsingTimeInMillis", "millis to parse single markdown page. See PegDown Parse configuration for details"),
    THYMELEAF_LOCALE("thymeleaf.locale", "default thymeleafe locale"),
    URI_NO_EXTENSION("uri.noExtension", "enable extension-less URI option?"),
    URI_NO_EXTENSION_PREFIX("uri.noExtension.prefix", "Set to a prefix path (starting with a slash) for which to generate extension-less URI's (i.e. a dir with index.html in)"),
    VERSION("version", "jbake application version");

    override fun toString() = this.key

    companion object {

        /**
         * Find a property by its configuration key.
         * Returns a known PropertyList entry if found, otherwise creates a custom property.
         */
        @JvmStatic
        fun getPropertyByKey(key: String): JBakeProperty {
            return entries.firstOrNull { it.key == key }
                ?: CustomProperty(key, "", JBakeProperty.Group.CUSTOM)
        }
    }
}

/**
 * Represents a custom property not in the standard PropertyList enum.
 * Allows for extensibility while maintaining type safety.
 */
data class CustomProperty(
    override val key: String,
    override val description: String,
    override val group: JBakeProperty.Group = JBakeProperty.Group.CUSTOM
) : JBakeProperty {
    override fun toString() = key
}

interface JBakeProperty {
    val key: String
    val description: String
    val group: Group

    enum class Group {
        DEFAULT, CUSTOM
    }
}
