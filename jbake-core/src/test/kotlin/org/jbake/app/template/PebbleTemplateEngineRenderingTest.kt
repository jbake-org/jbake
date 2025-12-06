package org.jbake.app.template

import io.kotest.core.spec.style.StringSpec

class PebbleTemplateEngineRenderingTest : StringSpec({
    lateinit var helper: TemplateTestHelper

    beforeSpec {
        helper = TemplateTestHelper("pebbleTemplates", "peb")
        helper.setupClass()
    }

    beforeTest {
        helper.setupTest()
    }

    afterTest {
        helper.teardownTest()
    }

    afterSpec {
        helper.teardownClass()
    }
})

