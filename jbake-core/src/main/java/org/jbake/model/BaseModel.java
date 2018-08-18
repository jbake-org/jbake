package org.jbake.model;

import java.util.HashMap;

public abstract class BaseModel extends HashMap<String, Object> {

    public String getUri() {
        return (String) get(ModelAttributes.URI.toString());
    }

    public void setUri(String uri) {
        put(ModelAttributes.URI.toString(), uri);
    }

    public void setName(String name) {
        put(ModelAttributes.NAME.toString(), name);
    }
}
