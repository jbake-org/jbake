package org.jbake.app.template

import io.kotest.core.spec.style.StringSpec

class ThymeleafTemplateEngineRenderingTest : StringSpec({
    lateinit var helper: TemplateTestHelper

    beforeSpec {
        helper = TemplateTestHelper("thymeleafTemplates", "html")
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

