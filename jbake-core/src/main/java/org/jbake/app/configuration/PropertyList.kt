package org.jbake.app.configuration

object PropertyList {
    @JvmField
    val ARCHIVE_FILE: Property = Property(
        "archive.file",
        "Output filename for archive file"
    )

    @JvmField
    val ASCIIDOCTOR_ATTRIBUTES: Property = Property(
        "asciidoctor.attributes",
        "attributes to be set when processing input"
    )

    val ASCIIDOCTOR_ATTRIBUTES_EXPORT: Property = Property(
        "asciidoctor.attributes.export",
        "Prefix to be used when exporting JBake properties to Asciidoctor"
    )

    val ASCIIDOCTOR_ATTRIBUTES_EXPORT_PREFIX: Property = Property(
        "asciidoctor.attributes.export.prefix",
        "prefix that should be used when JBake config options are exported"
    )

    val ASCIIDOCTOR_OPTION: Property = Property(
        "asciidoctor.option",
        "default asciidoctor options"
    )

    @JvmField
    val ASSET_FOLDER: Property = Property(
        "asset.folder",
        "folder that contains all asset files"
    )

    val ASSET_IGNORE_HIDDEN: Property = Property(
        "asset.ignore",
        "Flag indicating if hidden asset resources should be ignored"
    )

    val BUILD_TIMESTAMP: Property = Property(
        "build.timestamp",
        "timestamp jbake was build"
    )

    val CLEAR_CACHE: Property = Property(
        "db.clear.cache",
        "clear database cache"
    )

    val CONTENT_FOLDER: Property = Property(
        "content.folder",
        "folder that contains all content files"
    )

    val DATA_FOLDER: Property = Property(
        "data.folder",
        "folder that contains all data files"
    )

    @JvmField
    val DATA_FILE_DOCTYPE: Property = Property(
        "data.file.docType",
        "document type to use for data files"
    )

    val DATE_FORMAT: Property = Property(
        "date.format",
        "default date format used in content files"
    )

    val DB_STORE: Property = Property(
        "db.store",
        "database store (plocal, memory)"
    )

    val DB_PATH: Property = Property(
        "db.path",
        "database path for persistent storage"
    )

    val DEFAULT_STATUS: Property = Property(
        "default.status",
        "default document status"
    )

    val DEFAULT_TYPE: Property = Property(
        "default.type",
        "default document type"
    )

    @JvmField
    val DESTINATION_FOLDER: Property = Property(
        "destination.folder",
        "path to destination folder by default"
    )

    val DRAFT_SUFFIX: Property = Property(
        "draft.suffix",
        "draft content suffix"
    )

    val ERROR404_FILE: Property = Property(
        "error404.file",
        "filename to use for 404 error"
    )

    val FEED_FILE: Property = Property(
        "feed.file",
        "filename to use for feed"
    )

    val FREEMARKER_TIMEZONE: Property = Property(
        "freemarker.timezone",
        "TimeZone to use within Freemarker"
    )

    val GIT_HASH: Property = Property(
        "git.hash",
        "abbreviated git hash"
    )

    val HEADER_SEPARATOR: Property = Property(
        "header.separator",
        "String used to separate the header from the body"
    )

    @JvmField
    val IGNORE_FILE: Property = Property(
        "ignore.file",
        "file used to ignore a directory"
    )

    val IMG_PATH_UPDATE: Property = Property(
        "img.path.update",
        "update image path?"
    )

    val IMG_PATH_PREPEND_HOST: Property = Property(
        "img.path.prepend.host",
        "Prepend site.host to image paths"
    )

    val INDEX_FILE: Property = Property(
        "index.file",
        "filename to use for index file"
    )

    @JvmField
    val JVM_LOCALE: Property = Property(
        "jvm.locale",
        "locale for the jvm"
    )

    val MARKDOWN_EXTENSIONS: Property = Property(
        "markdown.extensions",
        "comma delimited default markdown extensions; for available extensions: http://www.decodified.com/pegdown/api/org/pegdown/Extensions.html"
    )

    val OUTPUT_ENCODING: Property = Property(
        "freemarker.outputencoding",
        "default output_encoding setting for freemarker"
    )

    @JvmField
    val OUTPUT_EXTENSION: Property = Property(
        "output.extension",
        "file extension for output content files"
    )

    @JvmField
    val PAGINATE_INDEX: Property = Property(
        "index.paginate",
        "paginate index?"
    )

    val POSTS_PER_PAGE: Property = Property(
        "index.posts_per_page",
        "number of post per page for pagination"
    )

    val RENDER_ARCHIVE: Property = Property(
        "render.archive",
        "render archive file?"
    )

    val RENDER_ENCODING: Property = Property(
        "render.encoding",
        "character encoding MIME name used in templates. use one of http://www.iana.org/assignments/character-sets/character-sets.xhtml"
    )

    val RENDER_ERROR404: Property = Property(
        "render.error404",
        "render 404 page?"
    )

    val RENDER_FEED: Property = Property(
        "render.feed",
        "render feed file?"
    )

    val RENDER_INDEX: Property = Property(
        "render.index",
        "render index file?"
    )

    val RENDER_SITEMAP: Property = Property(
        "render.sitemap",
        "render sitemap.xml file?"
    )

    val RENDER_TAGS: Property = Property(
        "render.tags",
        "render tag files?"
    )

    val RENDER_TAGS_INDEX: Property = Property(
        "render.tagsindex",
        "render tag index file?"
    )

    val SERVER_PORT: Property = Property(
        "server.port",
        "default server port"
    )

    val SERVER_HOSTNAME: Property = Property(
        "server.hostname",
        "default server hostname"
    )

    val SERVER_CONTEXT_PATH: Property = Property(
        "server.contextPath",
        "default server context path"
    )

    val SITE_HOST: Property = Property(
        "site.host",
        "site host"
    )

    val SITEMAP_FILE: Property = Property(
        "sitemap.file",
        "filename to use for sitemap file"
    )

    @JvmField
    val TAG_SANITIZE: Property = Property(
        "tag.sanitize",
        "sanitize tag value before it is used as filename (i.e. replace spaces with hyphens)"
    )

    @JvmField
    val TAG_PATH: Property = Property(
        "tag.path",
        "folder name to use for tag files"
    )

    @JvmField
    val TEMPLATE_FOLDER: Property = Property(
        "template.folder",
        "folder that contains all template files"
    )

    val TEMPLATE_ENCODING: Property = Property(
        "template.encoding",
        "character encoding MIME name used in templates. use one of http://www.iana.org/assignments/character-sets/character-sets.xhtml"
    )

    val TEMPLATE_MASTERINDEX_FILE: Property = Property(
        "template.masterindex.file",
        "filename of masterindex template file"
    )

    val TEMPLATE_FEED_FILE: Property = Property(
        "template.feed.file",
        "filename of feed template file"
    )

    val TEMPLATE_ARCHIVE_FILE: Property = Property(
        "template.archive.file",
        "filename of archive template file"
    )

    val TEMPLATE_TAG_FILE: Property = Property(
        "template.tag.file",
        "filename of tag template file"
    )

    val TEMPLATE_TAGSINDEX_FILE: Property = Property(
        "template.tagsindex.file",
        "filename of tag index template file"
    )

    val TEMPLATE_SITEMAP_FILE: Property = Property(
        "template.sitemap.file",
        "filename of sitemap template file"
    )

    val TEMPLATE_POST_FILE: Property = Property(
        "template.post.file",
        "filename of post template file"
    )

    val TEMPLATE_PAGE_FILE: Property = Property(
        "template.page.file",
        "filename of page template file"
    )

    val EXAMPLE_PROJECT_FREEMARKER: Property = Property(
        "example.project.freemarker",
        "zip file containing example project structure using freemarker templates"
    )

    val EXAMPLE_PROJECT_GROOVY: Property = Property(
        "example.project.groovy",
        "zip file containing example project structure using groovy templates"
    )

    val EXAMPLE_PROJECT_GROOVY_MTE: Property = Property(
        "example.project.groovy-mte",
        "zip file containing example project structure using groovy markup templates"
    )

    val EXAMPLE_PROJECT_THYMELEAF: Property = Property(
        "example.project.thymeleaf",
        "zip file containing example project structure using thymeleaf templates"
    )

    val EXAMPLE_PROJECT_JADE: Property = Property(
        "example.project.jade",
        "zip file containing example project structure using jade templates"
    )

    val MARKDOWN_MAX_PARSINGTIME: Property = Property(
        "markdown.maxParsingTimeInMillis",
        "millis to parse single markdown page. See PegDown Parse configuration for details"
    )

    val THYMELEAF_LOCALE: Property = Property(
        "thymeleaf.locale",
        "default thymeleafe locale"
    )

    val URI_NO_EXTENSION: Property = Property(
        "uri.noExtension",
        "enable extension-less URI option?"
    )

    val URI_NO_EXTENSION_PREFIX: Property = Property(
        "uri.noExtension.prefix",
        "Set to a prefix path (starting with a slash) for which to generate extension-less URI's (i.e. a folder with index.html in)"
    )

    val VERSION: Property = Property(
        "version",
        "jbake application version"
    )

    @JvmStatic
    fun getPropertyByKey(key: String): Property {
        for (field in PropertyList::class.java.getFields()) {
            try {
                val property = field.get(null) as Property
                if (property.key == key)
                    return property
            } catch (e: IllegalAccessException) {
                return Property(key, "", Property.Group.CUSTOM)
            }
        }
        return Property(key, "", Property.Group.CUSTOM)
    }
}
