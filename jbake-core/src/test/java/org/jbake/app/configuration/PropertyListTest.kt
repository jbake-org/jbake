package org.jbake.app.configuration

import org.assertj.core.api.Assertions.assertThat
import org.jbake.app.configuration.PropertyList.getPropertyByKey
import org.junit.Test

class PropertyListTest {
    @Test
    fun getPropertyByKey() {
        val property = getPropertyByKey("archive.file")

        assertThat(property).isEqualTo(PropertyList.ARCHIVE_FILE)
    }

    @Test
    fun getCustomProperty() {
        val property = getPropertyByKey("unknown.option")

        assertThat(property.key).isEqualTo("unknown.option")
        assertThat(property.group).isEqualTo(Property.Group.CUSTOM)
    }
}
