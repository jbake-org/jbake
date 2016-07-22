package org.jbake.model;

public enum DocumentAttributes {
    SHA1("sha1"),
    SOURCE_URI("sourceuri"),
    RENDERED("rendered"),
    CACHED("cached");

    private String label;

    DocumentAttributes(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
