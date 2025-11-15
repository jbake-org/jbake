package org.jbake.model;

public abstract class ModelAttributes {
    public static final String SHA1 = "sha1";
    public static final String SOURCE_URI = "sourceuri";
    public static final String RENDERED = "rendered";
    public static final String CACHED = "cached";
    public static final String STATUS = "status";
    public static final String NAME = "name";
    public static final String BODY = "body";
    public static final String DATE = "date";
    public static final String TYPE = "type";
    public static final String TAGS = "tags";
    public static final String URI = "uri";
    public static final String ROOTPATH = "rootpath";
    public static final String FILE = "file";
    public static final String NO_EXTENSION_URI = "noExtensionUri";
    public static final String TITLE = "title";
    public static final String TAGGED_POSTS = "tagged_posts";
    public static final String TAGGED_DOCUMENTS = "tagged_documents";
    public static final String NEXT_CONTENT = "nextContent";
    public static final String PREVIOUS_CONTENT = "previousContent";
    public static final String CONFIG = "config";
    public static final String CONTENT = "content";
    public static final String RENDERER = "renderer";
    public static final String NUMBER_OF_PAGES = "numberOfPages";
    public static final String CURRENT_PAGE_NUMBERS = "currentPageNumber";
    public static final String PREVIOUS_FILENAME = "previousFileName";
    public static final String NEXT_FILENAME = "nextFileName";
    public static final String TAG = "tag";
    public static final String VERSION = "version";
    public static final String OUT = "out";
    public static final String ALLTAGS = "alltags";
    public static final String PUBLISHED_DATE = "published_date";
    public static final String DB = "db";
    public static final String DATA = "data";

    private ModelAttributes() {
    }

    /**
     * Possible values of the {@link ModelAttributes#STATUS} property
     *
     * @author ndx
     */
    public abstract static class Status {
        public static final String PUBLISHED_DATE = "published-date";
        public static final String PUBLISHED = "published";
        public static final String DRAFT = "draft";

        private Status() {
        }
    }
}
