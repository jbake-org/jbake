package org.jbake.app

import org.jbake.model.DocumentModel
import java.time.LocalDate


/**
 * Just a playground to debug HSQLDB issues.
 */
object HsqldbDebugMain {

    @JvmStatic fun main(args: Array<String>) {
        log.info("=== HSQLDB Debug Test ===");

        val repo = HsqldbContentRepository("memory", "test-debug")

        log.info("1. Created repository")
        repo.startup()

        log.info("2. Called startup()")
        val doc = DocumentModel()
        doc.sourceUri = "test.html"
        doc.type = "post"
        doc.status = "published"
        doc.title = "Test"
        doc.date = LocalDate.now()
        doc.rendered = false

        log.info("3. Created document");
        repo.addDocument(doc)

        log.info("4. Added document");
        val count = repo.getDocumentCount("post")

        log.info("5. Got count: " + count);

        if (count == 1L) log.info("SUCCESS!")
        else log.info("FAILURE! Expected 1 but got " + count)

        repo.close()
    }

    private val log by org.jbake.util.logger<HsqldbDebugMain>()
}

