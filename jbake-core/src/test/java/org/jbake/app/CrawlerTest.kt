package org.jbake.app

import com.orientechnologies.orient.core.db.record.OTrackedMap
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldNotBeInstanceOf
import org.apache.commons.io.FilenameUtils
import org.jbake.model.DocumentModel
import org.jbake.model.DocumentTypes.addDocumentType
import org.jbake.model.ModelAttributes
import org.jbake.util.DataFileUtil

class CrawlerTest : StringSpec({

    lateinit var db: ContentStore
    lateinit var config: org.jbake.app.configuration.DefaultJBakeConfiguration

    beforeSpec {
        ContentStoreIntegrationTest.setUpClass()
        db = ContentStoreIntegrationTest.db
        config = ContentStoreIntegrationTest.config
    }

    beforeTest {
        db.startup()
    }

    afterTest {
        db.drop()
    }

    afterSpec {
        ContentStoreIntegrationTest.cleanUpClass()
    }

    "crawlContentDirectory" {
        val crawler = Crawler(db, config)
        crawler.crawlContentDirectory()

        db.getDocumentCount("post") shouldBe 4
        db.getDocumentCount("page") shouldBe 3

        val results: DocumentList<DocumentModel> = db.publishedPosts

        results.size shouldBe 3

        for (content in results) {
            content.containsKey(ModelAttributes.ROOTPATH) shouldBe true
            content.containsValue("../../../") shouldBe true
        }

        val allPosts: DocumentList<DocumentModel> = db.getAllContent("post")

        allPosts.size shouldBe 4

        allPosts.filter { it.title == "Draft Post" }
            .forEach { it.containsKey(ModelAttributes.DATE) shouldBe true }

        // Covers bug #213
        val publishedPostsByTag: DocumentList<DocumentModel> =
            db.getPublishedPostsByTag("blog")
        publishedPostsByTag.size.toLong() shouldBe 3
    }

    "crawlDataFiles" {
        val crawler = Crawler(db, config)

        // Manually register data doctype.
        addDocumentType(config.dataFileDocType)
        db.updateSchema()

        crawler.crawlDataFiles()
        db.getDocumentCount("data") shouldBe 2

        val dataFileUtil = DataFileUtil(db, "data")
        val videosYaml = dataFileUtil.get("videos.yaml")
        videosYaml.isEmpty().shouldBeFalse()
        videosYaml["data"].shouldNotBeNull()

        // Regression test for issue #747.
        val authorsFileContents = dataFileUtil.get("authors.yaml")
        authorsFileContents.isEmpty().shouldBeFalse()
        val authorsList = authorsFileContents["authors"]
        authorsList.shouldNotBeInstanceOf<OTrackedMap<*>>()
        authorsList.shouldBeInstanceOf<HashMap<*, *>>()
        @Suppress("UNCHECKED_CAST")
        val authors = authorsList as HashMap<String, MutableMap<String, Any>>
        authors["Joe Bloggs"]!!["last_name"] shouldBe "Bloggs"
    }

    "renderWithPrettyUrls" {
        config.setUriWithoutExtension(true)
        config.setPrefixForUriWithoutExtension("/blog")

        Crawler(db, config).crawlContentDirectory()

        db.getDocumentCount("post") shouldBe 4
        db.getDocumentCount("page") shouldBe 3

        val documents: DocumentList<DocumentModel> = db.publishedPosts

        for (model in documents) {
            val noExtensionUri = "blog/\\d{4}/" + FilenameUtils.getBaseName(model.file) + "/"

            model.noExtensionUri!!.matches(noExtensionUri.toRegex()) shouldBe true
            model.uri!!.matches((noExtensionUri + "index\\.html").toRegex()) shouldBe true
            model.rootPath shouldBe "../../../"
        }
    }
})

