package org.jbake.app.template

import io.kotest.core.spec.style.StringSpec

class GroovyMarkupTemplateEngineRenderingTest : StringSpec({
    lateinit var helper: TemplateTestHelper

    beforeSpec {
        helper = TemplateTestHelper("groovyMarkupTemplates", "tpl")
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

    // Override expected outputs for Groovy templates
    beforeTest {
        helper.expectedInOutput["post"] = mutableListOf(
            "<h2>Second Post</h2>",
            "<p class=\"post-date\">28",
            "2013</p>",
            "Lorem ipsum dolor sit amet",
            "<h5>Published Posts</h5>",
            "blog/2012/first-post.html"
        )
    }
})

