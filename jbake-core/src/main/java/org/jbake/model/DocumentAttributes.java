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
    TITLE("title");

    private String label;

    DocumentAttributes(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
