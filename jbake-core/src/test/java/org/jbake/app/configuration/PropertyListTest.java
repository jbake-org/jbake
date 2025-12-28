package org.jbake.app.configuration;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PropertyListTest {

    @Test
    void getPropertyByKey() {
        Property property = PropertyList.getPropertyByKey("archive.file");

        assertThat(property).isEqualTo(PropertyList.ARCHIVE_FILE);
    }

    @Test
    void getCustomProperty() {
        Property property = PropertyList.getPropertyByKey("unknown.option");

        assertThat(property.getKey()).isEqualTo("unknown.option");
        assertThat(property.getGroup()).isEqualTo(Property.Group.CUSTOM);
    }
}
