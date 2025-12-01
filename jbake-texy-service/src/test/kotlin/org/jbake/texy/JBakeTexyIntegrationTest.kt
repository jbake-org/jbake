package org.jbake.texy

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import org.jbake.app.Parser
import org.jbake.app.configuration.ConfigUtil
import org.jbake.app.configuration.DefaultJBakeConfiguration
import org.jbake.parser.Engines
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.io.File
import java.nio.file.Files

/**
 * Integration test for JBake with Texy service.
 *
 * Tests the complete workflow:
 * 1. Start Texy service container
 * 2. Configure JBake to use the service
 * 3. Process .texy files
 * 4. Verify rendered output
 */
@Testcontainers
class JBakeTexyIntegrationTest : FunSpec({


    test("TexyEngine should be registered in JBake") {
        val engine = Engines.get("texy")
        engine shouldBe org.jbake.parser.TexyEngine::class.java.let { it.getDeclaredConstructor().newInstance() }::class.java
    }

    test("JBake should process .texy files with Texy service") {
        // Get service URL from container
        val host = texyContainer.host
        val port = texyContainer.getMappedPort(TEXY_PORT)
        val serviceUrl = "http://$host:$port/texy"

        // Create temporary test directory
        val tempDir = Files.createTempDirectory("jbake-texy-test").toFile()
        val contentDir = File(tempDir, "content")
        contentDir.mkdirs()

        try {
            // Create test .texy file
            val texyFile = File(contentDir, "test.texy")
            texyFile.writeText("""
                title=Test Post
                status=published
                type=post
                date=2025-12-01
                ~~~~~~

                Test Heading
                ============

                This is **bold** and //italic// text.
            """.trimIndent())

            // Create minimal JBake configuration
            val configFile = File(tempDir, "jbake.properties")
            configFile.writeText("""
                texy.service.url=$serviceUrl
                texy.connection.timeout=5000
                texy.read.timeout=10000
            """.trimIndent())

            // Load configuration and parse file
            val config = ConfigUtil().loadConfig(tempDir) as DefaultJBakeConfiguration
            config.setProperty("texy.service.url", serviceUrl)

            val parser = Parser(config)
            val document = parser.processFile(texyFile)

            // Verify document was parsed
            document shouldNotBe null
            document?.title shouldBe "Test Post"
            document?.status shouldBe "published"
            document?.type shouldBe "post"

            // Verify body was rendered by Texy service
            val body = document?.body
            body shouldNotBe null
            body!! shouldContain "<h1>"
            body shouldContain "Test Heading"
            body shouldContain "<strong>"
            body shouldContain "bold"
            body shouldContain "<em>"
            body shouldContain "italic"

        } finally {
            // Cleanup
            tempDir.deleteRecursively()
        }
    }

    test("JBake should handle Texy service errors gracefully") {
        // Create temporary test directory
        val tempDir = Files.createTempDirectory("jbake-texy-error-test").toFile()
        val contentDir = File(tempDir, "content")
        contentDir.mkdirs()

        try {
            // Create test .texy file
            val texyFile = File(contentDir, "test.texy")
            texyFile.writeText("""
                title=Test Post
                status=published
                type=post
                date=2025-12-01
                ~~~~~~

                Test content
            """.trimIndent())

            // Create configuration with invalid service URL
            val configFile = File(tempDir, "jbake.properties")
            configFile.writeText("""
                texy.service.url=http://invalid-host:9999/texy
                texy.connection.timeout=1000
                texy.read.timeout=1000
            """.trimIndent())

            // Load configuration and parse file
            val config = ConfigUtil().loadConfig(tempDir) as DefaultJBakeConfiguration

            val parser = Parser(config)
            val document = parser.processFile(texyFile)

            // Document should still be parsed (header extraction)
            // but body rendering should fail gracefully
            document shouldNotBe null
            document?.title shouldBe "Test Post"

            // Body should contain error message wrapped in <pre>
            val body = document?.body
            if (body != null) {
                body shouldContain "<pre>"
                body shouldContain "Error"
            }

        } finally {
            // Cleanup
            tempDir.deleteRecursively()
        }
    }

    test("Texy service should handle multiple requests") {
        val host = texyContainer.host
        val port = texyContainer.getMappedPort(TEXY_PORT)
        val serviceUrl = "http://$host:$port/texy"

        val tempDir = Files.createTempDirectory("jbake-texy-multi-test").toFile()
        val contentDir = File(tempDir, "content")
        contentDir.mkdirs()

        try {
            // Create configuration
            val configFile = File(tempDir, "jbake.properties")
            configFile.writeText("""
                texy.service.url=$serviceUrl
            """.trimIndent())

            val config = ConfigUtil().loadConfig(tempDir) as DefaultJBakeConfiguration
            config.setProperty("texy.service.url", serviceUrl)
            val parser = Parser(config)

            // Process multiple files
            repeat(5) { index ->
                val texyFile = File(contentDir, "test-$index.texy")
                texyFile.writeText("""
                    title=Test Post $index
                    status=published
                    type=post
                    date=2025-12-01
                    ~~~~~~

                    Post Number $index
                    ==================

                    This is test post number **$index**.
                """.trimIndent())

                val document = parser.processFile(texyFile)
                document shouldNotBe null
                document?.title shouldBe "Test Post $index"
                document?.body!! shouldContain "Post Number $index"
                document.body shouldContain "<strong>$index</strong>"
            }

        } finally {
            tempDir.deleteRecursively()
        }
    }
})
{
    companion object {
        private const val TEXY_PORT = 8080

        @Container
        val texyContainer = GenericContainer("jbake/texy-service:latest")
            .withExposedPorts(TEXY_PORT)
            .waitingFor(Wait.forHttp("/").forStatusCode(200))
            .withStartupTimeout(java.time.Duration.ofMinutes(2))
    }
}
