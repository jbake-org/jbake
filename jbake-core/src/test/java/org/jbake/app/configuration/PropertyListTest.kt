package org.jbake.app.configuration

import org.jbake.app.configuration.PropertyList.getPropertyByKey
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class PropertyListTest : StringSpec({
    "getPropertyByKey" {
        val property = getPropertyByKey("archive.file")

        property shouldBe PropertyList.ARCHIVE_FILE
    }

    "getCustomProperty" {
        val property = getPropertyByKey("unknown.option")

        property.key shouldBe "unknown.option"
        property.group shouldBe Property.Group.CUSTOM
    }
})