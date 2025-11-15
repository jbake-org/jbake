package org.jbake.model;

import java.util.HashMap;

public abstract class BaseModel extends HashMap<String, Object> {

    public String getUri() {
        return (String) get(ModelAttributes.URI);
    }

    public void setUri(String uri) {
        put(ModelAttributes.URI, uri);
    }

    public void setName(String name) {
        put(ModelAttributes.NAME, name);
    }
}
