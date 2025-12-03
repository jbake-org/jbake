package org.jbake.app

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.apache.commons.io.FilenameUtils
import org.jbake.model.DocumentModel
import org.jbake.model.DocumentTypeRegistry.addDocumentType
import org.jbake.model.ModelAttributes
import org.jbake.util.DataFileUtil

class CrawlerTestHsqldb : CrawlerTestBase(DatabaseType.HSQLDB)
class CrawlerTestNeo4j : CrawlerTestBase(DatabaseType.NEO4J)
class CrawlerTestOrientdb : CrawlerTestBase(DatabaseType.ORIENTDB)

abstract class CrawlerTestBase(dbType: DatabaseType) : StringSpec({

    lateinit var db: ContentStore
    lateinit var config: org.jbake.app.configuration.DefaultJBakeConfiguration

    beforeSpec {
        ContentStoreIntegrationTest.setUpClass(dbType)
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
        }

    "crawlDataFiles" {
            val crawler = Crawler(db, config)
            addDocumentType(config.dataFileDocType)
            db.updateSchema()

            crawler.crawlDataFiles()
            db.getDocumentCount("data") shouldBe 2

            val dataFileUtil = DataFileUtil(db, "data")
            val videosYaml = dataFileUtil.get("videos.yaml")
            videosYaml.isEmpty().shouldBeFalse()
            videosYaml["data"].shouldNotBeNull()

            val authorsFileContents = dataFileUtil.get("authors.yaml")
            authorsFileContents.isEmpty().shouldBeFalse()
            val authorsList = authorsFileContents["authors"]
            authorsList.shouldBeInstanceOf<Map<*, *>>()
            @Suppress("UNCHECKED_CAST")
            val authors = authorsList as Map<String, MutableMap<String, Any>>
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
