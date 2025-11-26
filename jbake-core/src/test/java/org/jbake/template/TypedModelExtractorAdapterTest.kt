package org.jbake.template

import org.apache.commons.configuration2.CompositeConfiguration
import org.assertj.core.api.Assertions.assertThat
import org.jbake.app.ContentStore
import org.jbake.app.configuration.DefaultJBakeConfiguration
import org.jbake.template.TemplateEngineAdapter.NoopAdapter
import org.junit.After
import org.junit.Test

class TypedModelExtractorAdapterTest {

    @After
    fun tearDown() {
        ModelExtractors.instance.reset()
    }

    @Test fun `typed extractor should be usable via adapter and extractAndTransform`() {
        val key = "typed_test_key"

        // Register a simple typed extractor that ignores context and returns a string
        val typed = object : TypedModelExtractor<String> {
            override fun extract(context: org.jbake.template.model.RenderContext, key: String): String {
                return "hello-typed"
            }
        }

        ModelExtractors.instance.registerEngine(key, typed)

        // Provide minimal legacy model map expected by the adapter (config is required)
        val model = mutableMapOf<String, Any>()
        model["config"] = DefaultJBakeConfiguration(CompositeConfiguration())

        // Use a lightweight ContentStore instance; adapter will pass it through but typed extractor doesn't use it
        val db = ContentStore("memory", "testdb")

        val adapted: Any? = ModelExtractors.instance.extractAndTransform(db, key, model, NoopAdapter())

        assertThat(adapted).isEqualTo("hello-typed")
    }
}

