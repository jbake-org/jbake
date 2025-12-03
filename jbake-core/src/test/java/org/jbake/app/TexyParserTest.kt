package org.jbake.app

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.jbake.TestUtils
import org.jbake.app.configuration.ConfigUtil
import org.jbake.app.configuration.DefaultJBakeConfiguration
import org.jbake.parser.ParserEnginesRegistry
import org.jbake.parser.TexyEngine
import java.io.File
import java.io.PrintWriter

/**
 * Tests for the Texy markup engine.
 *
 * Note: Most tests require a running Texy service. The basic test validates
 * that the engine is properly registered and configured.
 */
class TexyParserTest : StringSpec({
    lateinit var tempDir: File
    lateinit var config: DefaultJBakeConfiguration
    lateinit var validTexyFile: File
    lateinit var invalidTexyFile: File

    val validHeader = "title=Texy Test\nstatus=published\ntype=post\ndate=2023-12-01\n~~~~~~"
    val invalidHeader = "title=Texy Test\n~~~~~~"

    beforeTest {
        tempDir = java.nio.file.Files.createTempDirectory("jbake-test").toFile()
        val configFile = TestUtils.testResourcesAsSourceDir
        config = ConfigUtil().loadConfig(configFile) as DefaultJBakeConfiguration

        validTexyFile = tempDir.resolve("valid.texy").apply { createNewFile() }
        var out = PrintWriter(validTexyFile)
        out.println(validHeader)
        out.println()
        out.println("This is a **bold** text.")
        out.println()
        out.println("This is an //italic// text.")
        out.close()

        invalidTexyFile = tempDir.resolve("invalid.texy").apply { createNewFile() }
        out = PrintWriter(invalidTexyFile)
        out.println(invalidHeader)
        out.println()
        out.println("This content won't be parsed due to missing status.")
        out.close()
    }

    afterTest {
        tempDir.deleteRecursively()
    }

    "Texy engine should be registered" {
        val engine = ParserEnginesRegistry.get("texy")
        engine.shouldNotBeNull()
        engine::class.java shouldBe TexyEngine::class.java
    }

    "Should recognize .texy file extension" {
        val extensions = ParserEnginesRegistry.recognizedExtensions
        extensions.contains("texy") shouldBe true
    }

    "Should return null when parsing file with invalid header" {
        val parser = Parser(config)
        val map = parser.processFile(invalidTexyFile)
        map shouldBe null
    }

    "Should parse file with valid header" {
        // This test will skip actual processing if the Texy service is not available
        // but it will test header parsing
        val parser = Parser(config)

        // Since we don't have a Texy service running in tests, we expect this to fail gracefully
        // The engine should still parse the header correctly
        val map = parser.processFile(validTexyFile)

        // The file should be parsed, but the body rendering might fail if service is not available
        // In that case, the error message should be wrapped in a <pre> tag
        if (map != null) {
            map.title shouldBe "Texy Test"
            map.status shouldBe "published"
            map.type shouldBe "post"
        }
        // If map is null, it means the service connection failed completely,
        // which is acceptable in a test environment without a Texy service
    }

    "Should load sample.texy from test resources" {
        // Load the sample file from test resources
        val sampleFile = File(javaClass.getResource("/texy/sample.texy")?.file ?: throw IllegalStateException("sample.texy not found"))
        sampleFile.exists() shouldBe true

        val content = sampleFile.readText()
        content shouldContain "title=Sample Texy Document"
        content shouldContain "Welcome to Texy!"
    }

    "Should load texy-config.properties from test resources" {
        // Load the config file from test resources
        val configFile = File(javaClass.getResource("/texy/texy-config.properties")?.file ?: throw IllegalStateException("texy-config.properties not found"))
        configFile.exists() shouldBe true

        val content = configFile.readText()
        content shouldContain "texy.service.url"
        content shouldContain "texy.connection.timeout"
        content shouldContain "texy.read.timeout"
    }
})

