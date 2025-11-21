package org.jbake.app

import com.orientechnologies.orient.core.db.record.OTrackedMap
import org.apache.commons.io.FilenameUtils
import org.assertj.core.api.Assertions
import org.hamcrest.BaseMatcher
import org.hamcrest.CoreMatchers
import org.hamcrest.Description
import org.jbake.model.DocumentModel
import org.jbake.model.DocumentTypes.addDocumentType
import org.jbake.model.ModelAttributes
import org.jbake.util.DataFileUtil
import org.junit.Assert
import org.junit.Test

class CrawlerTest : ContentStoreIntegrationTest() {
    @Test
    fun crawl() {
        val crawler = Crawler(ContentStoreIntegrationTest.Companion.db, ContentStoreIntegrationTest.Companion.config)
        crawler.crawl()

        Assert.assertEquals(4, db.getDocumentCount("post"))
        Assert.assertEquals(3, db.getDocumentCount("page"))

        val results: DocumentList<DocumentModel> = db.publishedPosts

        Assertions.assertThat(results.size).isEqualTo(3)

        for (content in results) {
            Assertions.assertThat<String?, Any?>(content)
                .containsKey(ModelAttributes.ROOTPATH)
                .containsValue("../../../")
        }

        val allPosts: DocumentList<DocumentModel> = db.getAllContent("post")

        Assertions.assertThat(allPosts.size).isEqualTo(4)

        for (content in allPosts) {
            if (content!!.title == "Draft Post") {
                Assertions.assertThat<String?, Any?>(content).containsKey(ModelAttributes.DATE)
            }
        }

        // covers bug #213
        val publishedPostsByTag: DocumentList<DocumentModel> =
            db.getPublishedPostsByTag("blog")
        Assert.assertEquals(3, publishedPostsByTag.size.toLong())
    }

    @Test
    fun crawlDataFiles() {
        val crawler = Crawler(ContentStoreIntegrationTest.Companion.db, ContentStoreIntegrationTest.Companion.config)
        // manually register data doctype
        addDocumentType(ContentStoreIntegrationTest.Companion.config.getDataFileDocType())
        db.updateSchema()
        crawler.crawlDataFiles()
        Assert.assertEquals(2, db.getDocumentCount("data"))

        val dataFileUtil = DataFileUtil(ContentStoreIntegrationTest.Companion.db, "data")
        val videos = dataFileUtil.get("videos.yaml")
        Assert.assertFalse(videos!!.isEmpty())
        Assert.assertNotNull(videos.get("data"))

        // regression test for issue 747
        val authorsFileContents = dataFileUtil.get("authors.yaml")
        Assert.assertFalse(authorsFileContents!!.isEmpty())
        val authorsList = authorsFileContents.get("authors")
        Assertions.assertThat<Any?>(authorsList).isNotInstanceOf(OTrackedMap::class.java)
        Assertions.assertThat<Any?>(authorsList).isInstanceOf(HashMap::class.java)
        val authors = authorsList as HashMap<String, MutableMap<String,  Any>>
        Assertions.assertThat<Any?>(authors.get("Joe Bloggs")!!.get("last_name")).isEqualTo("Bloggs")
    }

    @Test
    fun renderWithPrettyUrls() {
        ContentStoreIntegrationTest.Companion.config.setUriWithoutExtension(true)
        ContentStoreIntegrationTest.Companion.config.setPrefixForUriWithoutExtension("/blog")

        val crawler = Crawler(ContentStoreIntegrationTest.Companion.db, ContentStoreIntegrationTest.Companion.config)
        crawler.crawl()

        Assert.assertEquals(4, db.getDocumentCount("post"))
        Assert.assertEquals(3, db.getDocumentCount("page"))

        val documents: DocumentList<DocumentModel> = db.publishedPosts

        for (model in documents) {
            val noExtensionUri = "blog/\\d{4}/" + FilenameUtils.getBaseName(model!!.file) + "/"

            Assert.assertThat<String>(model.noExtensionUri, RegexMatcher.Companion.matches(noExtensionUri))
            Assert.assertThat<String>(model.uri, RegexMatcher.Companion.matches(noExtensionUri + "index\\.html"))
            Assert.assertThat<String>(model.rootPath, CoreMatchers.`is`<String>("../../../"))
        }
    }

    private class RegexMatcher(private val regex: String) : BaseMatcher<Any?>() {
        override fun matches(o: Any): Boolean {
            return (o as String).matches(regex.toRegex())
        }

        override fun describeTo(description: Description) {
            description.appendText("matches regex: " + regex)
        }

        companion object {
            fun matches(regex: String): RegexMatcher {
                return RegexMatcher(regex)
            }
        }
    }
}
