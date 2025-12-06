package org.jbake.app

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.jbake.TestUtils
import org.jbake.app.configuration.ConfigUtil
import org.jbake.app.configuration.DefaultJBakeConfiguration
import org.jbake.model.DocumentModel
import java.io.File

/**
 * Test to verify that the author field from AsciiDoc documents is correctly parsed, stored, and retrieved through the entire pipeline.
 */
class AuthorFieldTest : StringSpec({

    lateinit var tempDir: File
    lateinit var config: DefaultJBakeConfiguration
    lateinit var db: ContentStore

    beforeTest {
        tempDir = java.nio.file.Files.createTempDirectory("jbake-author-test").toFile()
        val rootPath = TestUtils.testResourcesAsSourceDir
        config = ConfigUtil().loadConfig(rootPath) as DefaultJBakeConfiguration

        // Use HSQLDB (default)
        db = ContentStore(config.databaseStore, config.databasePath!!)
        db.startup()
        db.drop()
        db.updateSchema()
    }

    afterTest {
        db.close()
        tempDir.deleteRecursively()
    }

    "author field should survive parse-save-retrieve cycle" {
        // Create a DocumentModel manually with author field
        val parsedDoc = DocumentModel().apply {
            sourceUri = "test-author.adoc"
            type = "post"
            status = "published"
            title = "Test Document"
            put("author", "Jane Doe")
            put("email", "jane@example.com")
            body = "<p>Test content</p>"
        }
        println("=== Before Saving ===")
        println("Keys: ${parsedDoc.keys.sorted()}")
        println("Author: ${parsedDoc["author"]}")

        // Verify author is present before saving
        parsedDoc.containsKey("author") shouldBe true
        parsedDoc["author"] shouldBe "Jane Doe"

        // Save to database
        db.addDocument(parsedDoc)

        // Retrieve from database
        val retrievedDocs = db.getDocumentByUri(parsedDoc.sourceUri!!)
        retrievedDocs.size shouldBe 1

        val retrievedDoc = retrievedDocs[0]
        println("\n=== After Retrieval ===")
        println("Keys: ${retrievedDoc.keys.sorted()}")
        println("Author: ${retrievedDoc.getOrDefault("author", "MISSING!")}")
        println("Email: ${retrievedDoc.getOrDefault("email", "NOT PRESENT")}")

        // THIS IS THE KEY ASSERTION: author field should still be present after save/retrieve
        retrievedDoc.containsKey("author") shouldBe true
        retrievedDoc["author"] shouldBe "Jane Doe"
    }
})

