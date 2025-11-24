package org.jbake.template

import org.assertj.core.api.Assertions.assertThat
import org.jbake.app.ContentStore
import org.jbake.app.DocumentList
import org.jbake.app.configuration.JBakeConfiguration
import org.jbake.model.DocumentModel
import org.jbake.template.model.RenderContext
import org.jbake.template.model.TagsExtractor
import org.jbake.template.model.TemplateModel
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class TagExtractorTest {

    private fun mockDbWithTags(vararg tags: String): ContentStore {
        val db = mock(ContentStore::class.java)
        val tagSet = tags.toMutableSet()
        `when`(db.allTags).thenReturn(tagSet)

        // For each tag return empty lists for posts/documents (not needed for this test beyond existence)
        for (t in tags) {
            `when`(db.getPublishedPostsByTag(t)).thenReturn(DocumentList<DocumentModel>())
            `when`(db.getPublishedDocumentsByTag(t)).thenReturn(DocumentList<DocumentModel>())
        }
        return db
    }

    private fun mockConfig(tagPath: String?, outputExtension: String?): JBakeConfiguration {
        val cfg = mock(JBakeConfiguration::class.java)
        `when`(cfg.tagPathName).thenReturn(tagPath)
        `when`(cfg.outputExtension).thenReturn(outputExtension)
        return cfg
    }

    @Test
    fun `tags extractor constructs URIs when tagPath and extension present`() {
        val cfg = mockConfig("tags", ".html")
        val db = mockDbWithTags("blog")
        val context = RenderContext(config = cfg, db = db)

        val extractor = TagsExtractor()
        val list = extractor.extract(context, "tags")

        assertThat(list).isNotNull()
        assertThat(list.size).isEqualTo(1)

        val tm = list.first() as TemplateModel
        assertThat(tm.name).isEqualTo("blog")
        assertThat(tm.uri).isEqualTo("tags/blog.html")
    }

    @Test
    fun `tags extractor constructs URIs when tagPath empty`() {
        val cfg = mockConfig("", ".html")
        val db = mockDbWithTags("blog")
        val context = RenderContext(config = cfg, db = db)

        val extractor = TagsExtractor()
        val list = extractor.extract(context, "tags")

        val tm = list.first() as TemplateModel
        assertThat(tm.uri).isEqualTo("blog.html")
    }

    @Test
    fun `tags extractor constructs URIs when extension is null`() {
        val cfg = mockConfig("tags", null)
        val db = mockDbWithTags("news")
        val context = RenderContext(config = cfg, db = db)

        val extractor = TagsExtractor()
        val list = extractor.extract(context, "tags")

        val tm = list.first() as TemplateModel
        assertThat(tm.uri).isEqualTo("tags/news")
    }
}
