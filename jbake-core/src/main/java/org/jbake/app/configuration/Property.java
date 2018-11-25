package org.jbake.app.configuration;

import java.util.Objects;

public class Property implements Comparable {

    private final String key;
    private final String description;
    private final Group group;

    public Property(String key, String description) {
        this(key, description, Group.DEFAULT);
    }

    public Property(String key, String description, Group group) {
        this.key = key;
        this.description = description;
        this.group = group;
    }

    public String getKey() {
        return key;
    }

    public String getDescription() {
        return description;
    }

    public Group getGroup() {
        return group;
    }

    @Override
    public String toString() {
        return getKey();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Property property = (Property) o;
        return Objects.equals(key, property.key) &&
            Objects.equals(description, property.description) &&
            group == property.group;
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, description, group);
    }

    @Override
    public int compareTo(Object o) {
        Property other = (Property) o;
        int result = this.getGroup().compareTo(other.getGroup());

        if (result == 0) {
            result = this.getKey().compareTo(other.getKey());
        }
        return result;
    }

    public enum Group {
        DEFAULT, CUSTOM
    }



}
