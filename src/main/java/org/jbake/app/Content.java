package org.jbake.app;

import static org.jbake.app.ContentTag.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Content model used by jbake.
 */
public class Content {
    
    private final Map<String, Object> content;
    
    public Map<String, Object> getContentAsMap() {
        return content;
    }
    
    public Content() {
        this.content = new HashMap<String, Object>();
    }

    public Content(final Map<String, Object> contents) {
        this.content = new HashMap<String, Object>(contents);
    }

    public void putAll(final Content content) {
        this.content.putAll(content.content);
    }
    
    public void putAll(final Map<String, Object> content) {
        this.content.putAll(content);
    }

    public void put(final ContentTag key, final Object value) {
        put(key.name(), value);
    }
    
    public void put(final String key, final Object value) {
        if (value == null) {
            throw new IllegalArgumentException("Content doesn't accept key with a null value, key here is: " + key);
        }
        
        content.put(key, value);
    }

    public Object get(final ContentTag key) {
        return get(key.name());
    }
    
    public Object get(final String key) {
        return content.get(key);
    }
    
    public boolean containsKey(final ContentTag key) {
        return containsKey(key.name());
    }
    
    public boolean containsKey(final String key) {
        return content.containsKey(key);
    }
    
    public String getString(final ContentTag key, final String defaultValue)
    {
        return getString(key.name(), defaultValue);
    }

    public String getString(final String key, final String defaultValue)
    {
        Object value = content.get(key);

        if (value instanceof String)
        {
            return (String) value;
        }
        else if (value == null)
        {
            return defaultValue;
        }
        else
        {
            throw new IllegalArgumentException('\'' + key + "' doesn't map to a String object");
        }
    }

    public int getInt(final ContentTag key, final int defaultValue) 
    {
        return getInt(key.name(), defaultValue);
    }

    public int getInt(final String key, final int defaultValue)
    {
        Integer i = getInteger(key, null);

        if (i == null)
        {
            return defaultValue;
        }

        return i.intValue();
    }

    public Integer getInteger(final String key, final Integer defaultValue)
    {
        Object value = content.get(key);

        if (value == null)
        {
            return defaultValue;
        }
        return Integer.valueOf(value.toString());
    }
    
    public boolean getBoolean(final ContentTag key, final boolean defaultValue) {
        return getBoolean(key.name(), defaultValue);
    }
    
    public boolean getBoolean(final String key, final boolean defaultValue) {
        Object value = content.get(key);

        if (value == null)
        {
            return defaultValue;
        }
        else
        return Boolean.valueOf(key);
    }
    
    public void setStatus(final String s) {
        put(status, s);
    }
    
    public void setStatus(final ContentStatus s) {
        setStatus(s.key());
    }

    public ContentStatus getStatus() {
        try {
            return ContentStatus.valueOf(getString(status, null));
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("A content status must have a valid value.", e);
        } catch (NullPointerException e) {
            throw new IllegalStateException("A content status must be provided.", e);
        }
    }
    
    public void tryToSetupStatusIfNeededWithDefaultValue(final ContentStatus defaultStatus) {
        if (containsKey(status)) {
            return; // source is providing a status so no need to use default
        }
        if (defaultStatus == null) {
            return; // the status stays null: it should be catch lately
        }
        setStatus(defaultStatus);
        return;
    }
    
    public boolean isWithValidHeader() {
        return get(type) != null && get(status) != null;
    }
    
    @Override
    public int hashCode() {
        return content.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return content.equals(obj);
    }

    @Override
    public String toString() {
        return content.toString();
    }

    
}
