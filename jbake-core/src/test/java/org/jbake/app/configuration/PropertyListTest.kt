package org.jbake.app.configuration

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class PropertyListTest : StringSpec({

    "getPropertyByKey" {
        val property = PropertyList.getPropertyByKey("archive.file")
        property shouldBe PropertyList.ARCHIVE_FILE
    }

    "getCustomProperty" {
        val property = PropertyList.getPropertyByKey("unknown.option")
        property.key shouldBe "unknown.option"
        property.group shouldBe JBakeProperty.Group.CUSTOM
    }
})
