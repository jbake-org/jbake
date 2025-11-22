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
        val crawler = Crawler(db, config)
        crawler.crawl()

        Assert.assertEquals(4, db.getDocumentCount("post"))
        Assert.assertEquals(3, db.getDocumentCount("page"))

        val results: DocumentList<DocumentModel> = db.publishedPosts

        Assertions.assertThat(results.size).isEqualTo(3)

        for (content in results) {
            Assertions.assertThat(content)
                .containsKey(ModelAttributes.ROOTPATH)
                .containsValue("../../../")
        }

        val allPosts: DocumentList<DocumentModel> = db.getAllContent("post")

        Assertions.assertThat(allPosts.size).isEqualTo(4)

        for (content in allPosts) {
            if (content!!.title == "Draft Post") {
                Assertions.assertThat(content).containsKey(ModelAttributes.DATE)
            }
        }

        // covers bug #213
        val publishedPostsByTag: DocumentList<DocumentModel> =
            db.getPublishedPostsByTag("blog")
        Assert.assertEquals(3, publishedPostsByTag.size.toLong())
    }

    @Test
    fun crawlDataFiles() {
        val crawler = Crawler(db, config)
        // manually register data doctype
        addDocumentType(config.dataFileDocType)
        db.updateSchema()
        crawler.crawlDataFiles()
        Assert.assertEquals(2, db.getDocumentCount("data"))

        val dataFileUtil = DataFileUtil(db, "data")
        val videos = dataFileUtil.get("videos.yaml")
        Assert.assertFalse(videos!!.isEmpty())
        Assert.assertNotNull(videos["data"])

        // regression test for issue 747
        val authorsFileContents = dataFileUtil.get("authors.yaml")
        Assert.assertFalse(authorsFileContents.isEmpty())
        val authorsList = authorsFileContents["authors"]
        Assertions.assertThat<Any?>(authorsList).isNotInstanceOf(OTrackedMap::class.java)
        Assertions.assertThat<Any?>(authorsList).isInstanceOf(HashMap::class.java)
        val authors = authorsList as HashMap<String, MutableMap<String,  Any>>
        Assertions.assertThat<Any?>(authors.get("Joe Bloggs")!!["last_name"]).isEqualTo("Bloggs")
    }

    @Test
    fun renderWithPrettyUrls() {
        config.setUriWithoutExtension(true)
        config.setPrefixForUriWithoutExtension("/blog")

        val crawler = Crawler(db, config)
        crawler.crawl()

        Assert.assertEquals(4, db.getDocumentCount("post"))
        Assert.assertEquals(3, db.getDocumentCount("page"))

        val documents: DocumentList<DocumentModel> = db.publishedPosts

        for (model in documents) {
            val noExtensionUri = "blog/\\d{4}/" + FilenameUtils.getBaseName(model.file) + "/"

            Assert.assertThat<String>(model.noExtensionUri, RegexMatcher.matches(noExtensionUri))
            Assert.assertThat(model.uri, RegexMatcher.matches(noExtensionUri + "index\\.html"))
            Assert.assertThat(model.rootPath, CoreMatchers.`is`("../../../"))
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
