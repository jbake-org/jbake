package org.jbake.app

import com.orientechnologies.orient.core.db.record.OTrackedMap
import org.apache.commons.io.FilenameUtils
import org.assertj.core.api.Assertions.assertThat
import org.jbake.model.DocumentModel
import org.jbake.model.DocumentTypes.addDocumentType
import org.jbake.model.ModelAttributes
import org.jbake.util.DataFileUtil
import org.junit.Assert.*
import org.junit.Test

class CrawlerTest : ContentStoreIntegrationTest() {

    @Test fun crawl() {
        val crawler = Crawler(db, config)
        crawler.crawl()

        assertEquals(4, db.getDocumentCount("post"))
        assertEquals(3, db.getDocumentCount("page"))

        val results: DocumentList<DocumentModel> = db.publishedPosts

        assertThat(results.size).isEqualTo(3)

        for (content in results)
            assertThat(content).containsKey(ModelAttributes.ROOTPATH).containsValue("../../../")

        val allPosts: DocumentList<DocumentModel> = db.getAllContent("post")

        assertThat(allPosts.size).isEqualTo(4)

        allPosts.filter { it.title == "Draft Post" }
            .forEach { assertThat(it).containsKey(ModelAttributes.DATE) }

        // Covers bug #213
        val publishedPostsByTag: DocumentList<DocumentModel> =
            db.getPublishedPostsByTag("blog")
        assertEquals(3, publishedPostsByTag.size.toLong())
    }

    @Test fun crawlDataFiles() {
        val crawler = Crawler(db, config)

        // Manually register data doctype.
        addDocumentType(config.dataFileDocType)
        db.updateSchema()
        crawler.crawlDataFiles()
        assertEquals(2, db.getDocumentCount("data"))

        val dataFileUtil = DataFileUtil(db, "data")
        val videosYaml = dataFileUtil.get("videos.yaml")
        assertFalse(videosYaml.isEmpty())
        assertNotNull(videosYaml["data"])

        // Regression test for issue #747.
        val authorsFileContents = dataFileUtil.get("authors.yaml")
        assertFalse(authorsFileContents.isEmpty())
        val authorsList = authorsFileContents["authors"]
        assertThat(authorsList).isNotInstanceOf(OTrackedMap::class.java)
        assertThat(authorsList).isInstanceOf(HashMap::class.java)
        val authors = authorsList as HashMap<String, MutableMap<String,  Any>>
        assertThat(authors.get("Joe Bloggs")!!["last_name"]).isEqualTo("Bloggs")
    }

    @Test fun renderWithPrettyUrls() {
        config.setUriWithoutExtension(true)
        config.setPrefixForUriWithoutExtension("/blog")

        Crawler(db, config).crawl()

        assertEquals(4, db.getDocumentCount("post"))
        assertEquals(3, db.getDocumentCount("page"))

        val documents: DocumentList<DocumentModel> = db.publishedPosts

        for (model in documents) {
            val noExtensionUri = "blog/\\d{4}/" + FilenameUtils.getBaseName(model.file) + "/"

            assertThat(model.noExtensionUri).matches(noExtensionUri)
            assertThat(model.uri).matches(noExtensionUri + "index\\.html")
            assertThat(model.rootPath).isEqualTo("../../../")
        }
    }
}
