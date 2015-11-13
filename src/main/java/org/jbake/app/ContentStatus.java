package org.jbake.app;

public enum ContentStatus { 
    draft,
    published,
    publishedDate("published-date");

    private final String key;
    
    ContentStatus() {
        this.key = this.name();
    }

    ContentStatus(String key) {
        this.key = key;
    }

    public String key() {
        return key;
    }

}
