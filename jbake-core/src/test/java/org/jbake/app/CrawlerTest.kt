package org.jbake.app

import com.orientechnologies.orient.core.db.record.OTrackedMap
import org.apache.commons.io.FilenameUtils
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.BaseMatcher
import org.hamcrest.CoreMatchers
import org.hamcrest.Description
import org.jbake.model.DocumentModel
import org.jbake.model.DocumentTypes.addDocumentType
import org.jbake.model.ModelAttributes
import org.jbake.util.DataFileUtil
import org.junit.Assert.*
import org.junit.Test

class CrawlerTest : ContentStoreIntegrationTest() {
    @Test
    fun crawl() {
        val crawler = Crawler(db, config)
        crawler.crawl()

        assertEquals(4, db.getDocumentCount("post"))
        assertEquals(3, db.getDocumentCount("page"))

        val results: DocumentList<DocumentModel> = db.publishedPosts

        assertThat(results.size).isEqualTo(3)

        for (content in results) {
            assertThat(content)
                .containsKey(ModelAttributes.ROOTPATH)
                .containsValue("../../../")
        }

        val allPosts: DocumentList<DocumentModel> = db.getAllContent("post")

        assertThat(allPosts.size).isEqualTo(4)

        for (content in allPosts) {
            if (content!!.title == "Draft Post") {
                assertThat(content).containsKey(ModelAttributes.DATE)
            }
        }

        // covers bug #213
        val publishedPostsByTag: DocumentList<DocumentModel> =
            db.getPublishedPostsByTag("blog")
        assertEquals(3, publishedPostsByTag.size.toLong())
    }

    @Test
    fun crawlDataFiles() {
        val crawler = Crawler(db, config)
        // manually register data doctype
        addDocumentType(config.dataFileDocType)
        db.updateSchema()
        crawler.crawlDataFiles()
        assertEquals(2, db.getDocumentCount("data"))

        val dataFileUtil = DataFileUtil(db, "data")
        val videos = dataFileUtil.get("videos.yaml")
        assertFalse(videos!!.isEmpty())
        assertNotNull(videos["data"])

        // regression test for issue 747
        val authorsFileContents = dataFileUtil.get("authors.yaml")
        assertFalse(authorsFileContents.isEmpty())
        val authorsList = authorsFileContents["authors"]
        assertThat(authorsList).isNotInstanceOf(OTrackedMap::class.java)
        assertThat(authorsList).isInstanceOf(HashMap::class.java)
        val authors = authorsList as HashMap<String, MutableMap<String,  Any>>
        assertThat(authors.get("Joe Bloggs")!!["last_name"]).isEqualTo("Bloggs")
    }

    @Test
    fun renderWithPrettyUrls() {
        config.setUriWithoutExtension(true)
        config.setPrefixForUriWithoutExtension("/blog")

        val crawler = Crawler(db, config)
        crawler.crawl()

        assertEquals(4, db.getDocumentCount("post"))
        assertEquals(3, db.getDocumentCount("page"))

        val documents: DocumentList<DocumentModel> = db.publishedPosts

        for (model in documents) {
            val noExtensionUri = "blog/\\d{4}/" + FilenameUtils.getBaseName(model.file) + "/"

            assertThat<String>(model.noExtensionUri, RegexMatcher.matches(noExtensionUri))
            assertThat(model.uri, RegexMatcher.matches(noExtensionUri + "index\\.html"))
            assertThat(model.rootPath, CoreMatchers.`is`("../../../"))
        }
    }

    private class RegexMatcher(private val regex: String) : BaseMatcher<Any?>() {
        override fun matches(o: Any): Boolean {
            return (o as String).matches(regex.toRegex())
        }

        override fun describeTo(description: Description) {
            description.appendText("matches regex: $regex")
        }

        companion object {
            fun matches(regex: String): RegexMatcher {
                return RegexMatcher(regex)
            }
        }
    }
}
