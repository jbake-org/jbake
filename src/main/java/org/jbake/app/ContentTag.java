package org.jbake.app;

import static org.jbake.app.ConfigUtil.Keys.*;

/**
 * Set of blog model entry keys used by jbake.
 * 
 * When a model {@link Content} entry's default value can be set up in jbake configuration
 * {@link ConfigUtil} but with an other key name, then this 
 * latter name is passed as an argument of the model key constructor.
 * 
 */
public enum ContentTag {

    status, 
    type, 
    date, 
    tags, 
    body, 
    uri, 
    summary, 
    summaryLength(SUMMERY_MAX_LENGTH), 
    summaryUnit(SUMMERY_LENGTH_UNIT), 
    ellipsis(SUMMERY_ELLIPSIS), 
    readmore(SUMMERY_READMORE),
    summaryForHome,
    summaryForFeed,
    file,
    cached,
    rendered,
    rootpath, 
    sha1;
    private final String key;
    
    ContentTag() {
        this.key = this.name();
    }

    ContentTag(String key) {
        this.key = key;
    }

    public String key() {
        return key;
    }

}
