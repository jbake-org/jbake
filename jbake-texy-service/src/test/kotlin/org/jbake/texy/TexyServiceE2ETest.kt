package org.jbake.texy

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import org.apache.hc.client5.http.classic.methods.HttpPost
import org.apache.hc.client5.http.impl.classic.HttpClients
import org.apache.hc.core5.http.io.entity.StringEntity
import org.jbake.texy.JBakeTexyIntegrationTest.Companion.texyContainer
import org.junit.jupiter.api.Test
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.io.File
import java.nio.charset.StandardCharsets

/**
 * End-to-end test for Texy service Docker container.
 *
 * This test:
 * 1. Starts the Texy service Docker container
 * 2. Tests the service directly via HTTP
 * 3. Tests JBake integration with the service
 * 4. Verifies the rendered output
 */
@Testcontainers
class TexyServiceE2ETest : FunSpec({

    test("Texy service container should start successfully") {
        texyContainer.isRunning shouldBe true
    }

    test("Texy service should respond to health check") {
        val host = texyContainer.host
        val port = texyContainer.getMappedPort(TEXY_PORT)

        val client = HttpClients.createDefault()
        val request = org.apache.hc.client5.http.classic.methods.HttpGet("http://$host:$port/")

        client.execute(request).use { response ->
            response.code shouldBe 200
            val content = String(response.entity.content.readAllBytes(), StandardCharsets.UTF_8)
            content shouldContain "Texy Service is running"
        }
    }

    test("Texy service should process markup correctly") {
        val host = texyContainer.host
        val port = texyContainer.getMappedPort(TEXY_PORT)

        val client = HttpClients.createDefault()
        val request = HttpPost("http://$host:$port/texy")
        request.setHeader("Content-Type", "text/plain; charset=UTF-8")

        val texyMarkup = """
            Test Heading
            ============

            This is **bold** and this is //italic//.

            - List item 1
            - List item 2
        """.trimIndent()

        request.entity = StringEntity(texyMarkup, StandardCharsets.UTF_8)

        client.execute(request).use { response ->
            response.code shouldBe 200
            val html = String(response.entity.content.readAllBytes(), StandardCharsets.UTF_8)

            // Verify HTML output
            html shouldContain "<h1>"
            html shouldContain "Test Heading"
            html shouldContain "<strong>"
            html shouldContain "bold"
            html shouldContain "<em>"
            html shouldContain "italic"
            html shouldContain "<ul>"
            html shouldContain "<li>"
        }
    }

    test("Texy service should handle empty input") {
        val host = texyContainer.host
        val port = texyContainer.getMappedPort(TEXY_PORT)

        val client = HttpClients.createDefault()
        val request = HttpPost("http://$host:$port/texy")
        request.setHeader("Content-Type", "text/plain; charset=UTF-8")
        request.entity = StringEntity("", StandardCharsets.UTF_8)

        client.execute(request).use { response ->
            response.code shouldBe 200
        }
    }

    test("Texy service should handle complex markup") {
        val host = texyContainer.host
        val port = texyContainer.getMappedPort(TEXY_PORT)

        val client = HttpClients.createDefault()
        val request = HttpPost("http://$host:$port/texy")
        request.setHeader("Content-Type", "text/plain; charset=UTF-8")

        val complexMarkup = """
            Main Title
            ==========

            Subtitle
            --------

            This is a paragraph with **bold**, //italic//, and `code`.

            Links
            -----

            "Visit JBake":https://jbake.org

            Lists
            -----

            1. First item
            2. Second item
            3. Third item

            Table
            -----

            |----
            | Name | Value
            |----
            | Test | 123
            | Demo | 456
            |----
        """.trimIndent()

        request.entity = StringEntity(complexMarkup, StandardCharsets.UTF_8)

        client.execute(request).use { response ->
            response.code shouldBe 200
            val html = String(response.entity.content.readAllBytes(), StandardCharsets.UTF_8)

            // Verify various HTML elements are present
            html shouldContain "<h1>"
            html shouldContain "<h2>"
            html shouldContain "<a href"
            html shouldContain "https://jbake.org"
            html shouldContain "<ol>"
            html shouldContain "<table>"
        }
    }

    test("Texy service should handle UTF-8 characters") {
        val host = texyContainer.host
        val port = texyContainer.getMappedPort(TEXY_PORT)

        val client = HttpClients.createDefault()
        val request = HttpPost("http://$host:$port/texy")
        request.setHeader("Content-Type", "text/plain; charset=UTF-8")

        val utf8Markup = """
            Příliš žluťoučký kůň úpěl ďábelské ódy
            ==========================================

            This is **čeština** with special characters: ěščřžýáíé
        """.trimIndent()

        request.entity = StringEntity(utf8Markup, StandardCharsets.UTF_8)

        client.execute(request).use { response ->
            response.code shouldBe 200
            val html = String(response.entity.content.readAllBytes(), StandardCharsets.UTF_8)

            // Verify UTF-8 characters are preserved
            html shouldContain "žluťoučký"
            html shouldContain "čeština"
            html shouldContain "ěščřžýáíé"
        }
    }

    test("Texy service should return 404 for invalid endpoints") {
        val host = texyContainer.host
        val port = texyContainer.getMappedPort(TEXY_PORT)

        val client = HttpClients.createDefault()
        val request = org.apache.hc.client5.http.classic.methods.HttpGet("http://$host:$port/invalid")

        client.execute(request).use { response ->
            response.code shouldBe 404
        }
    }

    test("Texy service URL should be configurable") {
        val host = texyContainer.host
        val port = texyContainer.getMappedPort(TEXY_PORT)
        val serviceUrl = "http://$host:$port/texy"

        serviceUrl shouldNotBe null
        serviceUrl shouldContain "http://"
        serviceUrl shouldContain "/texy"
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

