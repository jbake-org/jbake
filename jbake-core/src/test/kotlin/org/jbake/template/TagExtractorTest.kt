package org.jbake.template

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.jbake.app.ContentStore
import org.jbake.app.DocumentList
import org.jbake.app.configuration.JBakeConfiguration
import org.jbake.model.DocumentModel
import org.jbake.template.model.RenderContext
import org.jbake.template.model.TagsExtractor
import org.jbake.template.model.JbakeTemplateModel

class TagExtractorTest : StringSpec({
    fun mockDbWithTags(vararg tags: String): ContentStore {
        val db = mockk<ContentStore>()
        val tagSet = tags.toMutableSet()
        every { db.allTags } returns tagSet

        // For each tag return empty lists for posts/documents (not needed for this test beyond existence)
        for (t in tags) {
            every { db.getPublishedPostsByTag(t) } returns DocumentList<DocumentModel>()
            every { db.getPublishedDocumentsByTag(t) } returns DocumentList<DocumentModel>()
        }
        return db
    }
    fun mockConfig(tagPath: String?, outputExtension: String?): JBakeConfiguration {
        val cfg = mockk<JBakeConfiguration>()
        every { cfg.tagPathName } returns tagPath
        every { cfg.outputExtension } returns outputExtension
        return cfg
    }

    "tags extractor constructs URIs when tagPath and extension present" {
        val cfg = mockConfig("tags", ".html")
        val db = mockDbWithTags("blog")
        val context = RenderContext(config = cfg, db = db)

        val extractor = TagsExtractor()
        val list = extractor.extract(context, "tags")

        list.shouldNotBeNull()
        list.size shouldBe 1

        val tm = list.first() as JbakeTemplateModel
        tm.name shouldBe "blog"
        tm.uri shouldBe "tags/blog.html"
    }

    "tags extractor constructs URIs when tagPath empty" {
        val cfg = mockConfig("", ".html")
        val db = mockDbWithTags("blog")
        val context = RenderContext(config = cfg, db = db)

        val extractor = TagsExtractor()
        val list = extractor.extract(context, "tags")

        val tm = list.firstOrNull { (it as JbakeTemplateModel).name == "blog" } as JbakeTemplateModel?
        tm.shouldNotBeNull()
        tm.uri shouldBe "blog.html"
    }

    "tags extractor constructs URIs when extension is null" {
        val cfg = mockConfig("tags", null)
        val db = mockDbWithTags("news")
        val context = RenderContext(config = cfg, db = db)

        val extractor = TagsExtractor()
        val list = extractor.extract(context, "tags")

        val tm = list.firstOrNull { (it as JbakeTemplateModel).name == "news" } as JbakeTemplateModel?
        tm.shouldNotBeNull()
        tm.uri shouldBe "tags/news"
    }

    "tags extractor trims trailing slash in tagPath" {
        val cfg = mockConfig("tags/", ".html")
        val db = mockDbWithTags("blog")
        val context = RenderContext(config = cfg, db = db)

        val extractor = TagsExtractor()
        val list = extractor.extract(context, "tags")

        val tm = list.firstOrNull { (it as JbakeTemplateModel).name == "blog" } as JbakeTemplateModel?
        tm.shouldNotBeNull()
        tm.uri shouldBe "tags/blog.html"
    }

    "tags extractor handles null tagPath (defaults to no prefix)" {
        val cfg = mockConfig(null, ".html")
        val db = mockDbWithTags("blog")
        val context = RenderContext(config = cfg, db = db)

        val extractor = TagsExtractor()
        val list = extractor.extract(context, "tags")

        val tm = list.firstOrNull { (it as JbakeTemplateModel).name == "blog" } as JbakeTemplateModel?
        tm.shouldNotBeNull()
        tm.uri shouldBe "blog.html"
    }
})
