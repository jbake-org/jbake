package org.jbake.app.configuration;

import java.lang.reflect.Field;

import static org.jbake.app.configuration.Property.Group.CUSTOM;

public abstract class PropertyList {

    public static final Property ARCHIVE_FILE = new Property(
        "archive.file",
        "Output filename for archive file"
    );

    public static final Property ASCIIDOCTOR_ATTRIBUTES = new Property(
        "asciidoctor.attributes",
        "attributes to be set when processing input"
    );

    public static final Property ASCIIDOCTOR_ATTRIBUTES_EXPORT = new Property(
        "asciidoctor.attributes.export",
        "Prefix to be used when exporting JBake properties to Asciidoctor"
    );

    public static final Property ASCIIDOCTOR_ATTRIBUTES_EXPORT_PREFIX = new Property(
        "asciidoctor.attributes.export.prefix",
        "prefix that should be used when JBake config options are exported"
    );

    public static final Property ASCIIDOCTOR_OPTION = new Property(
        "asciidoctor.option",
        "default asciidoctor options"
    );

    public static final Property ASSET_FOLDER = new Property(
        "asset.folder",
        "folder that contains all asset files"
    );

    public static final Property ASSET_IGNORE_HIDDEN = new Property(
        "asset.ignore",
        "Flag indicating if hidden asset resources should be ignored"
    );

    public static final Property BUILD_TIMESTAMP = new Property(
        "build.timestamp",
        "timestamp jbake was build");

    public static final Property CLEAR_CACHE = new Property(
        "db.clear.cache",
        "clear database cache"
    );

    public static final Property CONTENT_FOLDER = new Property(
        "content.folder",
        "folder that contains all content files"
    );

    public static final Property DATA_FOLDER = new Property(
        "data.folder",
        "folder that contains all data files"
    );

    public static final Property DATA_FILE_DOCTYPE = new Property(
        "data.file.docType",
        "document type to use for data files"
    );

    public static final Property DATE_FORMAT = new Property(
        "date.format",
        "default date format used in content files"
    );

    public static final Property DB_STORE = new Property(
        "db.store",
        "database store (plocal, memory)"
    );

    public static final Property DB_PATH = new Property(
        "db.path",
        "database path for persistent storage"
    );

    public static final Property DEFAULT_STATUS = new Property(
        "default.status",
        "default document status"
    );

    public static final Property DEFAULT_TYPE = new Property(
        "default.type",
        "default document type"
    );

    public static final Property DESTINATION_FOLDER = new Property(
        "destination.folder",
        "path to destination folder by default"
    );

    public static final Property DRAFT_SUFFIX = new Property(
        "draft.suffix",
        "draft content suffix"
    );

    public static final Property ERROR404_FILE = new Property(
        "error404.file",
        "filename to use for 404 error"
    );

    public static final Property FEED_FILE = new Property(
        "feed.file",
        "filename to use for feed"
    );

    public static final Property GIT_HASH = new Property(
        "git.hash",
        "abbreviated git hash"
    );

    public static final Property HEADER_SEPARATOR = new Property(
        "header.separator",
        "String used to separate the header from the body"
    );

    public static final Property IGNORE_FILE = new Property(
        "ignore.file",
        "file used to ignore a directory"
    );

    public static final Property IMG_PATH_UPDATE = new Property(
        "img.path.update",
        "update image path?"
    );
    public static final Property RELATIVE_PATH_UPDATE = new Property(
        "relative.path.update",
        "update relative path?"
    );

    public static final Property ASSERT_REPLACE_DOMAINS = new Property(
        "assert.replace.domains",
        "replace these domains with local site?"
    );

    public static final Property RELATIVE_PATH_PREPEND_HOST = new Property(
        "relative.path.prepend.host",
        "Prepend site.host to relative paths"
    );

    public static final Property RELATIVE_TAG_ATTRIBUTE = new Property(
        "relative.tag.attribute",
        "Define tag and it's attribute that may contains relative paths"
    );

    public static final Property IMG_PATH_PREPEND_HOST = new Property(
        "img.path.prepend.host",
        "Prepend site.host to image paths"
    );

    public static final Property INDEX_FILE = new Property(
        "index.file",
        "filename to use for index file"
    );

    public static final Property JVM_LOCALE = new Property(
        "jvm.locale",
        "locale for the jvm"
    );

    public static final Property MARKDOWN_EXTENSIONS = new Property(
        "markdown.extensions",
        "comma delimited default markdown extensions; for available extensions: http://www.decodified.com/pegdown/api/org/pegdown/Extensions.html"
    );

    public static final Property OUTPUT_ENCODING = new Property(
        "freemarker.outputencoding",
        "default output_encoding setting for freemarker"
    );

    public static final Property OUTPUT_EXTENSION = new Property(
        "output.extension",
        "file extension for output content files"
    );

    public static final Property PAGINATE_INDEX = new Property(
        "index.paginate",
        "paginate index?"
    );

    public static final Property POSTS_PER_PAGE = new Property(
        "index.posts_per_page",
        "number of post per page for pagination"
    );

    public static final Property RENDER_ARCHIVE = new Property(
        "render.archive",
        "render archive file?"
    );

    public static final Property RENDER_ENCODING = new Property(
        "render.encoding",
        "character encoding MIME name used in templates. use one of http://www.iana.org/assignments/character-sets/character-sets.xhtml"
    );

    public static final Property RENDER_ERROR404 = new Property(
        "render.error404",
        "render 404 page?"
    );

    public static final Property RENDER_FEED = new Property(
        "render.feed",
        "render feed file?"
    );

    public static final Property RENDER_INDEX = new Property(
        "render.index",
        "render index file?"
    );

    public static final Property RENDER_SITEMAP = new Property(
        "render.sitemap",
        "render sitemap.xml file?"
    );

    public static final Property RENDER_TAGS = new Property(
        "render.tags",
        "render tag files?"
    );

    public static final Property RENDER_TAGS_INDEX = new Property(
        "render.tagsindex",
        "render tag index file?"
    );

    public static final Property SERVER_PORT = new Property(
        "server.port",
        "default server port"
    );

    public static final Property SERVER_HOSTNAME = new Property(
        "server.hostname",
        "default server hostname"
    );

    public static final Property SERVER_CONTEXT_PATH = new Property(
        "server.contextPath",
        "default server context path"
    );

    public static final Property SITE_HOST = new Property(
        "site.host",
        "site host"
    );

    public static final Property SITEMAP_FILE = new Property(
        "sitemap.file",
        "filename to use for sitemap file"
    );

    public static final Property TAG_SANITIZE = new Property(
        "tag.sanitize",
        "sanitize tag value before it is used as filename (i.e. replace spaces with hyphens)"
    );

    public static final Property TAG_PATH = new Property(
        "tag.path",
        "folder name to use for tag files"
    );

    public static final Property TEMPLATE_FOLDER = new Property(
        "template.folder",
        "folder that contains all template files"
    );

    public static final Property TEMPLATE_ENCODING = new Property(
        "template.encoding",
        "character encoding MIME name used in templates. use one of http://www.iana.org/assignments/character-sets/character-sets.xhtml"
    );

    public static final Property TEMPLATE_MASTERINDEX_FILE = new Property(
        "template.masterindex.file",
        "filename of masterindex template file"
    );

    public static final Property TEMPLATE_FEED_FILE = new Property(
        "template.feed.file",
        "filename of feed template file"
    );

    public static final Property TEMPLATE_ARCHIVE_FILE = new Property(
        "template.archive.file",
        "filename of archive template file"
    );

    public static final Property TEMPLATE_TAG_FILE = new Property(
        "template.tag.file",
        "filename of tag template file"
    );

    public static final Property TEMPLATE_TAGSINDEX_FILE = new Property(
        "template.tagsindex.file",
        "filename of tag index template file"
    );

    public static final Property TEMPLATE_SITEMAP_FILE = new Property(
        "template.sitemap.file",
        "filename of sitemap template file"
    );

    public static final Property TEMPLATE_POST_FILE = new Property(
        "template.post.file",
        "filename of post template file"
    );

    public static final Property TEMPLATE_PAGE_FILE = new Property(
        "template.page.file",
        "filename of page template file"
    );

    public static final Property EXAMPLE_PROJECT_FREEMARKER = new Property(
        "example.project.freemarker",
        "zip file containing example project structure using freemarker templates"
    );

    public static final Property EXAMPLE_PROJECT_GROOVY = new Property(
        "example.project.groovy",
        "zip file containing example project structure using groovy templates"
    );

    public static final Property EXAMPLE_PROJECT_GROOVY_MTE = new Property(
        "example.project.groovy-mte",
        "zip file containing example project structure using groovy markup templates"
    );

    public static final Property EXAMPLE_PROJECT_THYMELEAF = new Property(
        "example.project.thymeleaf",
        "zip file containing example project structure using thymeleaf templates"
    );

    public static final Property EXAMPLE_PROJECT_JADE = new Property(
        "example.project.jade",
        "zip file containing example project structure using jade templates"
    );

    public static final Property MARKDOWN_MAX_PARSINGTIME = new Property(
        "markdown.maxParsingTimeInMillis",
        "millis to parse single markdown page. See PegDown Parse configuration for details"
    );

    public static final Property THYMELEAF_LOCALE = new Property(
        "thymeleaf.locale",
        "default thymeleafe locale"
    );

    public static final Property URI_NO_EXTENSION = new Property(
        "uri.noExtension",
        "enable extension-less URI option?"
    );

    public static final Property URI_NO_EXTENSION_PREFIX = new Property(
        "uri.noExtension.prefix",
        "Set to a prefix path (starting with a slash) for which to generate extension-less URI's (i.e. a folder with index.html in)"
    );

    public static final Property VERSION = new Property(
        "version",
        "jbake application version"
    );

    private PropertyList() {
    }

    public static Property getPropertyByKey(String key) {

        for (Field field : PropertyList.class.getFields()) {
            try {
                Property property = (Property) field.get(null);

                if (property.getKey().equals(key)) {
                    return property;
                }
            } catch (IllegalAccessException e) {
                return new Property(key, "", CUSTOM);
            }
        }
        return new Property(key, "", CUSTOM);
    }
}
