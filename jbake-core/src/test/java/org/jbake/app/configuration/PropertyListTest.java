package org.jbake.app.configuration;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class PropertyListTest {

    @Test
    public void getPropertyByKey() {

        Property property = PropertyList.getPropertyByKey("archive.file");

        assertThat(property, is(PropertyList.ARCHIVE_FILE));
    }

    @Test
    public void getCustomProperty() {

        Property property = PropertyList.getPropertyByKey("unknown.option");

        assertThat(property.getKey(), is("unknown.option"));
        assertThat(property.getGroup(), is(Property.Group.CUSTOM));
    }
}