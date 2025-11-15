package org.jbake.app.configuration;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PropertyListTest {

    @Test
    public void getPropertyByKey() {
        Property property = PropertyList.getPropertyByKey("archive.file");

        assertThat(property).isEqualTo(PropertyList.ARCHIVE_FILE);
    }

    @Test
    public void getCustomProperty() {
        Property property = PropertyList.getPropertyByKey("unknown.option");

        assertThat(property.getKey()).isEqualTo("unknown.option");
        assertThat(property.getGroup()).isEqualTo(Property.Group.CUSTOM);
    }
}
