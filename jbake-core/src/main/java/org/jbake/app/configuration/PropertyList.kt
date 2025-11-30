package org.jbake.app.configuration

/**
 * Enum of all JBake configuration properties.
 */
enum class PropertyList(
    override val key: String,
    override val description: String,
    override val group: JBakeProperty.Group
)
    : JBakeProperty
{
    // CONTENT_STRUCTURE - Directories and file paths
    CONTENT_FOLDER(         "content.folder",              "Directory that contains all content files.",                                                                                           JBakeProperty.Group.CONTENT_STRUCTURE),
    ASSET_FOLDER(           "asset.folder",                "Directory that contains all asset files.",                                                                                             JBakeProperty.Group.CONTENT_STRUCTURE),
    TEMPLATE_FOLDER(        "template.folder",             "Directory that contains all template files.",                                                                                          JBakeProperty.Group.CONTENT_STRUCTURE),
    DATA_FOLDER(            "data.folder",                 "Directory that contains all data files.",                                                                                              JBakeProperty.Group.CONTENT_STRUCTURE),
    DESTINATION_FOLDER(     "destination.folder",          "Path to destination directory by default.",                                                                                            JBakeProperty.Group.CONTENT_STRUCTURE),
    ASSET_IGNORE_HIDDEN(    "asset.ignore",                "Flag indicating if hidden asset resources should be ignored.",                                                                         JBakeProperty.Group.CONTENT_STRUCTURE),
    IGNORE_FILE(            "ignore.file",                 "File used to ignore a directory.",                                                                                                     JBakeProperty.Group.CONTENT_STRUCTURE),

    // OUTPUT - Output generation and file naming
    OUTPUT_EXTENSION(       "output.extension",            "File extension for output content files.",                                                                                             JBakeProperty.Group.OUTPUT),
    ARCHIVE_FILE(           "archive.file",                "Output filename for archive file.",                                                                                                    JBakeProperty.Group.OUTPUT),
    INDEX_FILE(             "index.file",                  "Filename to use for index file.",                                                                                                      JBakeProperty.Group.OUTPUT),
    FEED_FILE(              "feed.file",                   "Filename to use for feed.",                                                                                                            JBakeProperty.Group.OUTPUT),
    SITEMAP_FILE(           "sitemap.file",                "Filename to use for sitemap file.",                                                                                                    JBakeProperty.Group.OUTPUT),
    ERROR404_FILE(          "error404.file",               "Filename to use for 404 error.",                                                                                                       JBakeProperty.Group.OUTPUT),
    TAG_PATH(               "tag.path",                    "Directory name to use for tag files.",                                                                                                 JBakeProperty.Group.OUTPUT),
    URI_NO_EXTENSION(       "uri.noExtension",             "Enable extension-less URI option?",                                                                                                    JBakeProperty.Group.OUTPUT),
    URI_NO_EXTENSION_PREFIX("uri.noExtension.prefix",      "Set to a prefix path (starting with a slash) for which to generate extension-less URI's (i.e. a dir with index.html in).",           JBakeProperty.Group.OUTPUT),
    IMG_PATH_UPDATE(        "img.path.update",             "Update image path?",                                                                                                                   JBakeProperty.Group.OUTPUT),
    IMG_PATH_PREPEND_HOST(  "img.path.prepend.host",       "Prepend site.host to image paths.",                                                                                                   JBakeProperty.Group.OUTPUT),
    SITE_HOST(              "site.host",                   "Site host.",                                                                                                                           JBakeProperty.Group.OUTPUT),

    // CONTENT - Content processing and parsing
    HEADER_SEPARATOR(       "header.separator",            "String used to separate the header from the body.",                                                                                    JBakeProperty.Group.CONTENT),
    DEFAULT_STATUS(         "default.status",              "Default document status.",                                                                                                             JBakeProperty.Group.CONTENT),
    DEFAULT_TYPE(           "default.type",                "Default document type.",                                                                                                               JBakeProperty.Group.CONTENT),
    DATE_FORMAT(            "date.format",                 "Default date format used in content files.",                                                                                           JBakeProperty.Group.CONTENT),
    DRAFT_SUFFIX(           "draft.suffix",                "Draft content suffix.",                                                                                                                JBakeProperty.Group.CONTENT),
    DATA_FILE_DOCTYPE(      "data.file.docType",           "Document type to use for data files.",                                                                                                 JBakeProperty.Group.CONTENT),
    TAG_SANITIZE(           "tag.sanitize",                "Sanitize tag value before it is used as filename (i.e. replace spaces with hyphens).",                                                  JBakeProperty.Group.CONTENT),
    PAGINATE_INDEX(         "index.paginate",              "Paginate index?",                                                                                                                      JBakeProperty.Group.CONTENT),
    POSTS_PER_PAGE(         "index.posts_per_page",        "Number of post per page for pagination.",                                                                                              JBakeProperty.Group.CONTENT),

    // CONTENT - Format-specific parsers
    MARKDOWN_EXTENSIONS(    "markdown.extensions",         "Comma delimited default markdown extensions; for available extensions: http://www.decodified.com/pegdown/api/org/pegdown/Extensions.html.", JBakeProperty.Group.CONTENT),
    MARKDOWN_MAX_PARSINGTIME("markdown.maxParsingTimeInMillis", "Millis to parse single markdown page. See PegDown Parse configuration for details.",                                              JBakeProperty.Group.CONTENT),
    ASCIIDOCTOR_ATTRIBUTES( "asciidoctor.attributes",      "Attributes to be set when processing input.",                                                                                          JBakeProperty.Group.CONTENT),
    ASCIIDOCTOR_ATTRIBUTES_EXPORT("asciidoctor.attributes.export", "Prefix to be used when exporting JBake properties to Asciidoctor.",                                                        JBakeProperty.Group.CONTENT),
    ASCIIDOCTOR_ATTRIBUTES_EXPORT_PREFIX("asciidoctor.attributes.export.prefix", "Prefix that should be used when JBake config options are exported.",                                    JBakeProperty.Group.CONTENT),
    ASCIIDOCTOR_OPTION(     "asciidoctor.option",          "Default asciidoctor options.",                                                                                                         JBakeProperty.Group.CONTENT),

    // TEMPLATING - Template engine configuration
    TEMPLATE_ENCODING(      "template.encoding",           "Character encoding MIME name used in templates. Use one of http://www.iana.org/assignments/character-sets/character-sets.xhtml.",      JBakeProperty.Group.TEMPLATING),
    RENDER_ENCODING(        "render.encoding",             "Character encoding MIME name used in templates. Use one of http://www.iana.org/assignments/character-sets/character-sets.xhtml.",      JBakeProperty.Group.TEMPLATING),
    TEMPLATE_MASTERINDEX_FILE("template.masterindex.file", "Filename of masterindex template file.",                                                                                               JBakeProperty.Group.TEMPLATING),
    TEMPLATE_FEED_FILE(     "template.feed.file",          "Filename of feed template file.",                                                                                                      JBakeProperty.Group.TEMPLATING),
    TEMPLATE_ARCHIVE_FILE(  "template.archive.file",       "Filename of archive template file.",                                                                                                   JBakeProperty.Group.TEMPLATING),
    TEMPLATE_TAG_FILE(      "template.tag.file",           "Filename of tag template file.",                                                                                                       JBakeProperty.Group.TEMPLATING),
    TEMPLATE_TAGSINDEX_FILE("template.tagsindex.file",     "Filename of tag index template file.",                                                                                                 JBakeProperty.Group.TEMPLATING),
    TEMPLATE_SITEMAP_FILE(  "template.sitemap.file",       "Filename of sitemap template file.",                                                                                                   JBakeProperty.Group.TEMPLATING),
    TEMPLATE_POST_FILE(     "template.post.file",          "Filename of post template file.",                                                                                                      JBakeProperty.Group.TEMPLATING),
    TEMPLATE_PAGE_FILE(     "template.page.file",          "Filename of page template file.",                                                                                                      JBakeProperty.Group.TEMPLATING),

    // TEMPLATING - Engine-specific settings
    FREEMARKER_TIMEZONE(    "freemarker.timezone",         "TimeZone to use within Freemarker.",                                                                                                   JBakeProperty.Group.TEMPLATING),
    OUTPUT_ENCODING(        "freemarker.outputencoding",   "Default output_encoding setting for freemarker.",                                                                                      JBakeProperty.Group.TEMPLATING),
    THYMELEAF_LOCALE(       "thymeleaf.locale",            "Default thymeleaf locale.",                                                                                                            JBakeProperty.Group.TEMPLATING),
    JVM_LOCALE(             "jvm.locale",                  "Locale for the jvm.",                                                                                                                  JBakeProperty.Group.TEMPLATING),

    // TEMPLATING - Example projects
    EXAMPLE_PROJECT_FREEMARKER("example.project.freemarker", "Zip file containing example project structure using freemarker templates.",                                                        JBakeProperty.Group.TEMPLATING),
    EXAMPLE_PROJECT_GROOVY( "example.project.groovy",      "Zip file containing example project structure using groovy templates.",                                                                JBakeProperty.Group.TEMPLATING),
    EXAMPLE_PROJECT_GROOVY_MTE("example.project.groovy-mte", "Zip file containing example project structure using groovy markup templates.",                                                     JBakeProperty.Group.TEMPLATING),
    EXAMPLE_PROJECT_THYMELEAF("example.project.thymeleaf", "Zip file containing example project structure using thymeleaf templates.",                                                             JBakeProperty.Group.TEMPLATING),
    EXAMPLE_PROJECT_JADE(   "example.project.jade",        "Zip file containing example project structure using jade templates.",                                                                  JBakeProperty.Group.TEMPLATING),

    // FEATURES - Feature flags for rendering
    RENDER_INDEX(           "render.index",                "Render index file?",                                                                                                                   JBakeProperty.Group.FEATURES),
    RENDER_ARCHIVE(         "render.archive",              "Render archive file?",                                                                                                                 JBakeProperty.Group.FEATURES),
    RENDER_FEED(            "render.feed",                 "Render feed file?",                                                                                                                    JBakeProperty.Group.FEATURES),
    RENDER_SITEMAP(         "render.sitemap",              "Render sitemap.xml file?",                                                                                                             JBakeProperty.Group.FEATURES),
    RENDER_TAGS(            "render.tags",                 "Render tag files?",                                                                                                                    JBakeProperty.Group.FEATURES),
    RENDER_TAGS_INDEX(      "render.tagsindex",            "Render tag index file?",                                                                                                               JBakeProperty.Group.FEATURES),
    RENDER_ERROR404(        "render.error404",             "Render 404 page?",                                                                                                                     JBakeProperty.Group.FEATURES),

    // SERVER - Development server settings
    SERVER_PORT(            "server.port",                 "Default server port.",                                                                                                                 JBakeProperty.Group.SERVER),
    SERVER_HOSTNAME(        "server.hostname",             "Default server hostname.",                                                                                                             JBakeProperty.Group.SERVER),
    SERVER_CONTEXT_PATH(    "server.contextPath",          "Default server context path.",                                                                                                         JBakeProperty.Group.SERVER),

    // DATABASE - Caching and storage
    DB_STORE(               "db.store",                    "Database store (plocal, memory).",                                                                                                     JBakeProperty.Group.DATABASE),
    DB_PATH(                "db.path",                     "Database path for persistent storage.",                                                                                                JBakeProperty.Group.DATABASE),
    CLEAR_CACHE(            "db.clear.cache",              "Clear database cache.",                                                                                                                JBakeProperty.Group.DATABASE),

    // METADATA - Build information and versioning
    VERSION(                "version",                     "JBake application version.",                                                                                                           JBakeProperty.Group.METADATA),
    BUILD_TIMESTAMP(        "build.timestamp",             "Timestamp jbake was built.",                                                                                                           JBakeProperty.Group.METADATA),
    GIT_HASH(               "git.hash",                    "Abbreviated git hash.",                                                                                                                JBakeProperty.Group.METADATA);

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
        /** Project structure and file paths */
        CONTENT_STRUCTURE,
        /** Output generation and rendering settings */
        OUTPUT,
        /** Content processing and parsing */
        CONTENT,
        /** Template engine specific settings */
        TEMPLATING,
        /** Feature flags for what to render */
        FEATURES,
        /** Development server configuration */
        SERVER,
        /** Database and caching settings */
        DATABASE,
        /** Metadata and versioning */
        METADATA,
        /** User-defined custom properties */
        CUSTOM
    }
}
