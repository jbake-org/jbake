package org.jbake.template

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.apache.commons.configuration2.CompositeConfiguration
import org.jbake.app.ContentStore
import org.jbake.app.configuration.DefaultJBakeConfiguration
import org.jbake.template.model.RenderContext

class TypedModelExtractorAdapterTest : StringSpec({

    afterTest {
        ModelExtractorsRegistry.instance.reset()
    }

    "typed extractor should be usable via adapter and extractAndTransform" {
        val key = "typed_test_key"

        // Register a simple typed extractor that ignores context and returns a string
        val typed = object : TypedModelExtractor<String> {
            override fun extract(context: RenderContext, key: String) = "hello-typed"
        }

        ModelExtractorsRegistry.instance.registerEngine(key, typed)

        // Provide minimal legacy model map expected by the adapter (config is required)
        val model = mutableMapOf<String, Any>()
        model["config"] = DefaultJBakeConfiguration(CompositeConfiguration())

        // Use a lightweight ContentStore instance; adapter will pass it through but typed extractor doesn't use it
        val db = ContentStore("memory", "testdb")

        val adapted: Any = ModelExtractorsRegistry.instance.extractAndTransform(db, key, model, TemplateEngineAdapter.NoopAdapter())

        adapted shouldBe "hello-typed"
    }
})
