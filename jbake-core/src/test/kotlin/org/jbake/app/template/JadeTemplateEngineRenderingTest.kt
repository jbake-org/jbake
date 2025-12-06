package org.jbake.app.template

import io.kotest.core.spec.style.StringSpec

class JadeTemplateEngineRenderingTest : StringSpec({
    lateinit var helper: TemplateTestHelper

    beforeSpec {
        helper = TemplateTestHelper("jadeTemplates", "jade")
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

