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
    CONTENT_FOLDER(         "content.folder",              "Directory where your content source files live.",                                                                                      JBakeProperty.Group.CONTENT_STRUCTURE),
    ASSET_FOLDER(           "asset.folder",                "Directory where static assets like CSS, JS, and images are stored.",                                                                   JBakeProperty.Group.CONTENT_STRUCTURE),
    TEMPLATE_FOLDER(        "template.folder",             "Directory where your template files live.",                                                                                            JBakeProperty.Group.CONTENT_STRUCTURE),
    DATA_FOLDER(            "data.folder",                 "Directory where data files (JSON, YAML, etc.) are stored for use in templates.",                                                       JBakeProperty.Group.CONTENT_STRUCTURE),
    DESTINATION_FOLDER(     "destination.folder",          "Directory where the baked (rendered) site goes.",                                                                                      JBakeProperty.Group.CONTENT_STRUCTURE),
    ASSET_IGNORE_HIDDEN(    "asset.ignore",                "Whether to skip hidden files (starting with dot) when copying assets.",                                                                JBakeProperty.Group.CONTENT_STRUCTURE),
    IGNORE_FILE(            "ignore.file",                 "Filename that marks a directory to be ignored (like .jbakeignore).",                                                                   JBakeProperty.Group.CONTENT_STRUCTURE),

    // OUTPUT - Output generation and file naming
    OUTPUT_EXTENSION(       "output.extension",            "File extension to use for baked HTML files (like .html).",                                                                             JBakeProperty.Group.OUTPUT),
    ARCHIVE_FILE(           "archive.file",                "Filename for the archive page listing all posts (e.g., archive.html).",                                                                JBakeProperty.Group.OUTPUT),
    INDEX_FILE(             "index.file",                  "Filename for the main index page.",                                                                                                    JBakeProperty.Group.OUTPUT),
    FEED_FILE(              "feed.file",                   "Filename for the RSS/Atom feed.",                                                                                                      JBakeProperty.Group.OUTPUT),
    SITEMAP_FILE(           "sitemap.file",                "Filename for the XML sitemap.",                                                                                                        JBakeProperty.Group.OUTPUT),
    ERROR404_FILE(          "error404.file",               "Filename for the 404 error page.",                                                                                                     JBakeProperty.Group.OUTPUT),
    TAG_PATH(               "tag.path",                    "URL path prefix for tag pages (e.g., 'tags' makes /tags/java.html).",                                                                 JBakeProperty.Group.OUTPUT),
    URI_NO_EXTENSION(       "uri.noExtension",             "Generate URLs without file extensions (pretty URLs)?",                                                                                 JBakeProperty.Group.OUTPUT),
    URI_NO_EXTENSION_PREFIX("uri.noExtension.prefix",      "Path prefix where pretty URLs are used (e.g., /blog/ for pretty URLs only in blog section).",                                         JBakeProperty.Group.OUTPUT),
    IMG_PATH_UPDATE(        "img.path.update",             "Whether to rewrite image paths in content?",                                                                                           JBakeProperty.Group.OUTPUT),
    IMG_PATH_PREPEND_HOST(  "img.path.prepend.host",       "Add site.host to the beginning of image paths (for absolute URLs).",                                                                  JBakeProperty.Group.OUTPUT),
    SITE_HOST(              "site.host",                   "Your site's base URL (e.g., https://example.com).",                                                                                    JBakeProperty.Group.OUTPUT),

    // CONTENT - Content processing and parsing
    HEADER_SEPARATOR(       "header.separator",            "String that separates front matter from content (usually ~~~~).",                                                                      JBakeProperty.Group.CONTENT),
    DEFAULT_STATUS(         "default.status",              "Default status for content (published, draft, etc.).",                                                                                 JBakeProperty.Group.CONTENT),
    DEFAULT_TYPE(           "default.type",                "Default document type (post, page, etc.).",                                                                                            JBakeProperty.Group.CONTENT),
    DATE_FORMAT(            "date.format",                 "Date format pattern for parsing dates in front matter.",                                                                               JBakeProperty.Group.CONTENT),
    DRAFT_SUFFIX(           "draft.suffix",                "Suffix added to draft content filenames.",                                                                                             JBakeProperty.Group.CONTENT),
    DATA_FILE_DOCTYPE(      "data.file.docType",           "What document type to assign to data files.",                                                                                          JBakeProperty.Group.CONTENT),
    TAG_SANITIZE(           "tag.sanitize",                "Clean up tag names for use in filenames (spaces become hyphens, etc.).",                                                               JBakeProperty.Group.CONTENT),
    PAGINATE_INDEX(         "index.paginate",              "Split the index into multiple pages?",                                                                                                 JBakeProperty.Group.CONTENT),
    POSTS_PER_PAGE(         "index.posts_per_page",        "How many posts to show per page when paginating.",                                                                                     JBakeProperty.Group.CONTENT),

    // CONTENT - Format-specific parsers
    MARKDOWN_EXTENSIONS(    "markdown.extensions",         "Which Markdown extensions to enable (comma-separated, e.g., TABLES, FENCED_CODE_BLOCKS).",                                              JBakeProperty.Group.CONTENT),
    MARKDOWN_MAX_PARSINGTIME("markdown.maxParsingTimeInMillis", "Maximum time in milliseconds to spend parsing a single Markdown file.",                                                       JBakeProperty.Group.CONTENT),
    ASCIIDOCTOR_ATTRIBUTES( "asciidoctor.attributes",      "Attributes to pass to AsciiDoctor when rendering.",                                                                                    JBakeProperty.Group.CONTENT),
    ASCIIDOCTOR_ATTRIBUTES_EXPORT("asciidoctor.attributes.export", "Which JBake config properties to export as AsciiDoctor attributes.",                                                       JBakeProperty.Group.CONTENT),
    ASCIIDOCTOR_ATTRIBUTES_EXPORT_PREFIX("asciidoctor.attributes.export.prefix", "Prefix to add when exporting JBake properties to AsciiDoctor.",                                          JBakeProperty.Group.CONTENT),
    ASCIIDOCTOR_OPTION(     "asciidoctor.option",          "Options to pass to AsciiDoctor renderer.",                                                                                             JBakeProperty.Group.CONTENT),

    // TEMPLATING - Template engine configuration
    TEMPLATE_ENCODING(      "template.encoding",           "Character encoding for reading template files (e.g., UTF-8).",                                                                         JBakeProperty.Group.TEMPLATING),
    RENDER_ENCODING(        "render.encoding",             "Character encoding for output HTML files (e.g., UTF-8).",                                                                              JBakeProperty.Group.TEMPLATING),
    TEMPLATE_MASTERINDEX_FILE("template.masterindex.file", "Which template to use for the paginated index pages.",                                                                                JBakeProperty.Group.TEMPLATING),
    TEMPLATE_FEED_FILE(     "template.feed.file",          "Which template to use for RSS/Atom feed.",                                                                                             JBakeProperty.Group.TEMPLATING),
    TEMPLATE_ARCHIVE_FILE(  "template.archive.file",       "Which template to use for the archive page (all posts list).",                                                                        JBakeProperty.Group.TEMPLATING),
    TEMPLATE_TAG_FILE(      "template.tag.file",           "Which template to use for individual tag pages.",                                                                                      JBakeProperty.Group.TEMPLATING),
    TEMPLATE_TAGSINDEX_FILE("template.tagsindex.file",     "Which template to use for the tags index page (all tags list).",                                                                      JBakeProperty.Group.TEMPLATING),
    TEMPLATE_SITEMAP_FILE(  "template.sitemap.file",       "Which template to use for the sitemap.xml.",                                                                                           JBakeProperty.Group.TEMPLATING),
    TEMPLATE_POST_FILE(     "template.post.file",          "Which template to use for blog posts.",                                                                                                JBakeProperty.Group.TEMPLATING),
    TEMPLATE_PAGE_FILE(     "template.page.file",          "Which template to use for static pages.",                                                                                              JBakeProperty.Group.TEMPLATING),

    // TEMPLATING - Engine-specific settings
    FREEMARKER_TIMEZONE(    "freemarker.timezone",         "Timezone for date formatting in Freemarker templates.",                                                                                JBakeProperty.Group.TEMPLATING),
    OUTPUT_ENCODING(        "freemarker.outputencoding",   "Output encoding for Freemarker (usually matches render.encoding).",                                                                    JBakeProperty.Group.TEMPLATING),
    THYMELEAF_LOCALE(       "thymeleaf.locale",            "Locale for Thymeleaf templates (e.g., en_US).",                                                                                        JBakeProperty.Group.TEMPLATING),
    JVM_LOCALE(             "jvm.locale",                  "JVM-wide locale setting (affects date parsing, etc.).",                                                                                JBakeProperty.Group.TEMPLATING),

    // TEMPLATING - Example projects
    EXAMPLE_PROJECT_FREEMARKER("example.project.freemarker", "Path to the example Freemarker project ZIP for jbake -i.",                                                                        JBakeProperty.Group.TEMPLATING),
    EXAMPLE_PROJECT_GROOVY( "example.project.groovy",      "Path to the example Groovy templates project ZIP for jbake -i.",                                                                      JBakeProperty.Group.TEMPLATING),
    EXAMPLE_PROJECT_GROOVY_MTE("example.project.groovy-mte", "Path to the example Groovy Markup Templates project ZIP for jbake -i.",                                                           JBakeProperty.Group.TEMPLATING),
    EXAMPLE_PROJECT_THYMELEAF("example.project.thymeleaf", "Path to the example Thymeleaf project ZIP for jbake -i.",                                                                              JBakeProperty.Group.TEMPLATING),
    EXAMPLE_PROJECT_JADE(   "example.project.jade",        "Path to the example Jade templates project ZIP for jbake -i.",                                                                        JBakeProperty.Group.TEMPLATING),

    // FEATURES - Feature flags for rendering
    RENDER_INDEX(           "render.index",                "Whether to generate the main index page?",                                                                                             JBakeProperty.Group.FEATURES),
    RENDER_ARCHIVE(         "render.archive",              "Whether to generate the archive page (all posts chronologically)?",                                                                    JBakeProperty.Group.FEATURES),
    RENDER_FEED(            "render.feed",                 "Whether to generate RSS/Atom feed?",                                                                                                   JBakeProperty.Group.FEATURES),
    RENDER_SITEMAP(         "render.sitemap",              "Whether to generate sitemap.xml for search engines?",                                                                                  JBakeProperty.Group.FEATURES),
    RENDER_TAGS(            "render.tags",                 "Whether to generate individual pages for each tag?",                                                                                   JBakeProperty.Group.FEATURES),
    RENDER_TAGS_INDEX(      "render.tagsindex",            "Whether to generate the tags index page (list of all tags)?",                                                                          JBakeProperty.Group.FEATURES),
    RENDER_ERROR404(        "render.error404",             "Whether to generate a 404 error page?",                                                                                                JBakeProperty.Group.FEATURES),

    // SERVER - Development server settings
    SERVER_PORT(            "server.port",                 "Port for the development server (default 8820).",                                                                                      JBakeProperty.Group.SERVER),
    SERVER_HOSTNAME(        "server.hostname",             "Hostname for the development server (default localhost).",                                                                             JBakeProperty.Group.SERVER),
    SERVER_CONTEXT_PATH(    "server.contextPath",          "Context path for the dev server (e.g., /blog makes it accessible at localhost:8820/blog).",                                           JBakeProperty.Group.SERVER),

    // DATABASE - Caching and storage
    DB_STORE(               "db.store",                    "Where to store the content database (plocal for disk, memory for RAM).",                                                               JBakeProperty.Group.DATABASE),
    DB_PATH(                "db.path",                     "Path where the persistent database is stored.",                                                                                        JBakeProperty.Group.DATABASE),
    CLEAR_CACHE(            "db.clear.cache",              "Whether to wipe the database cache on each bake.",                                                                                     JBakeProperty.Group.DATABASE),

    // METADATA - Build information and versioning
    VERSION(                "version",                     "JBake version (filled in automatically).",                                                                                             JBakeProperty.Group.METADATA),
    BUILD_TIMESTAMP(        "build.timestamp",             "When JBake was built (filled in automatically).",                                                                                      JBakeProperty.Group.METADATA),
    GIT_HASH(               "git.hash",                    "Git commit hash of JBake build (filled in automatically).",                                                                            JBakeProperty.Group.METADATA);

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
