package org.jbake.app.configuration

object PropertyList {

    @JvmField val ARCHIVE_FILE = Property("archive.file", "Output filename for archive file")
    @JvmField val ASCIIDOCTOR_ATTRIBUTES = Property("asciidoctor.attributes", "attributes to be set when processing input")
    @JvmField val ASCIIDOCTOR_ATTRIBUTES_EXPORT = Property("asciidoctor.attributes.export", "Prefix to be used when exporting JBake properties to Asciidoctor")
    @JvmField val ASCIIDOCTOR_ATTRIBUTES_EXPORT_PREFIX = Property("asciidoctor.attributes.export.prefix", "prefix that should be used when JBake config options are exported")
    @JvmField val ASCIIDOCTOR_OPTION = Property("asciidoctor.option", "default asciidoctor options")
    @JvmField val ASSET_FOLDER = Property("asset.folder", "folder that contains all asset files")
    @JvmField val ASSET_IGNORE_HIDDEN = Property("asset.ignore", "Flag indicating if hidden asset resources should be ignored")
    @JvmField val BUILD_TIMESTAMP = Property("build.timestamp", "timestamp jbake was build")
    @JvmField val CLEAR_CACHE = Property("db.clear.cache", "clear database cache")
    @JvmField val CONTENT_FOLDER = Property("content.folder", "folder that contains all content files")
    @JvmField val DATA_FOLDER = Property("data.folder", "folder that contains all data files")
    @JvmField val DATA_FILE_DOCTYPE = Property("data.file.docType", "document type to use for data files")
    @JvmField val DATE_FORMAT = Property("date.format", "default date format used in content files")
    @JvmField val DB_STORE = Property("db.store", "database store (plocal, memory)")
    @JvmField val DB_PATH = Property("db.path", "database path for persistent storage")
    @JvmField val DEFAULT_STATUS = Property("default.status", "default document status")
    @JvmField val DEFAULT_TYPE = Property("default.type", "default document type")
    @JvmField val DESTINATION_FOLDER = Property("destination.folder", "path to destination dir by default")
    @JvmField val DRAFT_SUFFIX = Property("draft.suffix", "draft content suffix")
    @JvmField val ERROR404_FILE = Property("error404.file", "filename to use for 404 error")
    @JvmField val FEED_FILE = Property("feed.file", "filename to use for feed")
    @JvmField val FREEMARKER_TIMEZONE = Property("freemarker.timezone", "TimeZone to use within Freemarker")
    @JvmField val GIT_HASH = Property("git.hash", "abbreviated git hash")
    @JvmField val HEADER_SEPARATOR = Property("header.separator", "String used to separate the header from the body")
    @JvmField val IGNORE_FILE = Property("ignore.file", "file used to ignore a directory")
    @JvmField val IMG_PATH_UPDATE = Property("img.path.update", "update image path?")
    @JvmField val IMG_PATH_PREPEND_HOST = Property("img.path.prepend.host", "Prepend site.host to image paths")
    @JvmField val INDEX_FILE = Property("index.file", "filename to use for index file")
    @JvmField val JVM_LOCALE = Property("jvm.locale", "locale for the jvm")
    @JvmField val MARKDOWN_EXTENSIONS = Property("markdown.extensions", "comma delimited default markdown extensions; for available extensions: http://www.decodified.com/pegdown/api/org/pegdown/Extensions.html")
    @JvmField val OUTPUT_ENCODING = Property("freemarker.outputencoding", "default output_encoding setting for freemarker")
    @JvmField val OUTPUT_EXTENSION = Property("output.extension", "file extension for output content files")
    @JvmField val PAGINATE_INDEX = Property("index.paginate", "paginate index?")
    @JvmField val POSTS_PER_PAGE = Property("index.posts_per_page", "number of post per page for pagination")
    @JvmField val RENDER_ARCHIVE = Property("render.archive", "render archive file?")
    @JvmField val RENDER_ENCODING = Property("render.encoding", "character encoding MIME name used in templates. use one of http://www.iana.org/assignments/character-sets/character-sets.xhtml")
    @JvmField val RENDER_ERROR404 = Property("render.error404", "render 404 page?")
    @JvmField val RENDER_FEED = Property("render.feed", "render feed file?")
    @JvmField val RENDER_INDEX = Property("render.index", "render index file?")
    @JvmField val RENDER_SITEMAP = Property("render.sitemap", "render sitemap.xml file?")
    @JvmField val RENDER_TAGS = Property("render.tags", "render tag files?")
    @JvmField val RENDER_TAGS_INDEX = Property("render.tagsindex", "render tag index file?")
    @JvmField val SERVER_PORT = Property("server.port", "default server port")
    @JvmField val SERVER_HOSTNAME = Property("server.hostname", "default server hostname")
    @JvmField val SERVER_CONTEXT_PATH = Property("server.contextPath", "default server context path")
    @JvmField val SITE_HOST = Property("site.host", "site host")
    @JvmField val SITEMAP_FILE = Property("sitemap.file", "filename to use for sitemap file")
    @JvmField val TAG_SANITIZE = Property("tag.sanitize", "sanitize tag value before it is used as filename (i.e. replace spaces with hyphens)")
    @JvmField val TAG_PATH = Property("tag.path", "folder name to use for tag files")
    @JvmField val TEMPLATE_FOLDER = Property("template.folder", "folder that contains all template files")
    @JvmField val TEMPLATE_ENCODING = Property("template.encoding", "character encoding MIME name used in templates. use one of http://www.iana.org/assignments/character-sets/character-sets.xhtml")
    @JvmField val TEMPLATE_MASTERINDEX_FILE = Property("template.masterindex.file", "filename of masterindex template file")
    @JvmField val TEMPLATE_FEED_FILE = Property("template.feed.file", "filename of feed template file")
    @JvmField val TEMPLATE_ARCHIVE_FILE = Property("template.archive.file", "filename of archive template file")
    @JvmField val TEMPLATE_TAG_FILE = Property("template.tag.file", "filename of tag template file")
    @JvmField val TEMPLATE_TAGSINDEX_FILE = Property("template.tagsindex.file", "filename of tag index template file")
    @JvmField val TEMPLATE_SITEMAP_FILE = Property("template.sitemap.file", "filename of sitemap template file")
    @JvmField val TEMPLATE_POST_FILE = Property("template.post.file", "filename of post template file")
    @JvmField val TEMPLATE_PAGE_FILE = Property("template.page.file", "filename of page template file")
    @JvmField val EXAMPLE_PROJECT_FREEMARKER = Property("example.project.freemarker", "zip file containing example project structure using freemarker templates")
    @JvmField val EXAMPLE_PROJECT_GROOVY = Property("example.project.groovy", "zip file containing example project structure using groovy templates")
    @JvmField val EXAMPLE_PROJECT_GROOVY_MTE = Property("example.project.groovy-mte", "zip file containing example project structure using groovy markup templates")
    @JvmField val EXAMPLE_PROJECT_THYMELEAF = Property("example.project.thymeleaf", "zip file containing example project structure using thymeleaf templates")
    @JvmField val EXAMPLE_PROJECT_JADE = Property("example.project.jade", "zip file containing example project structure using jade templates")
    @JvmField val MARKDOWN_MAX_PARSINGTIME = Property("markdown.maxParsingTimeInMillis", "millis to parse single markdown page. See PegDown Parse configuration for details")
    @JvmField val THYMELEAF_LOCALE = Property("thymeleaf.locale", "default thymeleafe locale")
    @JvmField val URI_NO_EXTENSION = Property("uri.noExtension", "enable extension-less URI option?")
    @JvmField val URI_NO_EXTENSION_PREFIX = Property("uri.noExtension.prefix", "Set to a prefix path (starting with a slash) for which to generate extension-less URI's (i.e. a dir with index.html in)")
    @JvmField val VERSION = Property("version", "jbake application version")

    @JvmStatic
    fun getPropertyByKey(key: String): Property {
        // With @JvmField, properties are now accessible as public static fields
        for (field in PropertyList::class.java.fields) {
            runCatching {
                val value = field.get(null)
                if (value is Property && value.key == key)
                    return value
            }
            // Ignore inaccessible fields.
        }
        // Return custom property if no match found
        return Property(key, "", Property.Group.CUSTOM)
    }
}
