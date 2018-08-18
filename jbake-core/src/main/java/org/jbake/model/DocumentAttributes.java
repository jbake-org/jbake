package org.jbake.model;

public enum DocumentAttributes {
    SHA1("sha1"),
    SOURCE_URI("sourceuri"),
    RENDERED("rendered"),
    CACHED("cached"), //TODO: Do we need this?
    STATUS("status"),
    NAME("name"),
    BODY("body"),
    DATE("date"),
    TYPE("type"),
    TAGS("tags"),
    URI("uri"),
    ROOTPATH("rootpath"),
    FILE("file"),
    NO_EXTENSION_URI("noExtensionUri"),
    TITLE("title"),
    TAGGED_POSTS("tagged_posts"),
    TAGGED_DOCUMENTS("tagged_documents"),
    NEXT_CONTENT("nextContent"),
    PREVIOUS_CONTENT("previousContent"),
    CONFIG("config"),
    CONTENT("content"),
    RENDERER("renderer"),
    NUMBER_OF_PAGES("numberOfPages"),
    CURRENT_PAGE_NUMBERS("currentPageNumber"),
    PREVIOUS_FILENAME("previousFileName"),
    NEXT_FILENAME("nextFileName"),
    TAG("tag");

    private String label;

    DocumentAttributes(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
