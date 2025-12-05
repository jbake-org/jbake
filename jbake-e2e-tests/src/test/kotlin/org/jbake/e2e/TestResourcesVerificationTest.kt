package org.jbake.e2e

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test
import java.io.File

/**
 * Simple verification tests to ensure shared test-data fixture is properly configured.
 * These tests run without Docker and verify the test setup.
 */
class TestResourcesVerificationTest {

    private fun getTestDataDir(): File {
        val testDataPath = System.getProperty("jbake.test.data.dir", "../test-data/fixture")
        return File(testDataPath).absoluteFile
    }

    @Test
    fun `should have test data directory`() {
        val testDataDir = getTestDataDir()
        testDataDir.exists() shouldBe true
        testDataDir.isDirectory shouldBe true
        println("Test data directory: ${testDataDir.absolutePath}")
    }

    @Test
    fun `should have jbake properties file`() {
        val testDataDir = getTestDataDir()
        val propsFile = testDataDir.resolve("jbake.properties")
        propsFile.exists() shouldBe true
        propsFile.readText() shouldNotBe ""
    }

    @Test
    fun `should have content directory`() {
        val testDataDir = getTestDataDir()
        val contentDir = testDataDir.resolve("content")
        contentDir.exists() shouldBe true
        contentDir.isDirectory shouldBe true
    }

    @Test
    fun `should have blog posts`() {
        val testDataDir = getTestDataDir()
        val blogDir = testDataDir.resolve("content/blog")
        blogDir.exists() shouldBe true
        blogDir.isDirectory shouldBe true
    }

    @Test
    fun `should have freemarker templates`() {
        val testDataDir = getTestDataDir()
        val templatesDir = testDataDir.resolve("freemarkerTemplates")
        templatesDir.exists() shouldBe true
        templatesDir.isDirectory shouldBe true

        templatesDir.resolve("index.ftl").exists() shouldBe true
        templatesDir.resolve("page.ftl").exists() shouldBe true
        templatesDir.resolve("post.ftl").exists() shouldBe true
    }

    @Test
    fun `should have groovy templates`() {
        val testDataDir = getTestDataDir()
        val templatesDir = testDataDir.resolve("groovyTemplates")
        templatesDir.exists() shouldBe true
        templatesDir.isDirectory shouldBe true

        templatesDir.resolve("index.gsp").exists() shouldBe true
        templatesDir.resolve("page.gsp").exists() shouldBe true
        templatesDir.resolve("post.gsp").exists() shouldBe true
    }

    @Test
    fun `should have thymeleaf templates`() {
        val testDataDir = getTestDataDir()
        val templatesDir = testDataDir.resolve("thymeleafTemplates")
        templatesDir.exists() shouldBe true
        templatesDir.isDirectory shouldBe true

        templatesDir.resolve("index.thyme").exists() shouldBe true
        templatesDir.resolve("page.thyme").exists() shouldBe true
        templatesDir.resolve("post.thyme").exists() shouldBe true
    }

    @Test
    fun `test data fixture should be complete`() {
        val testDataDir = getTestDataDir()

        // Check all essential directories exist
        listOf("content", "freemarkerTemplates", "groovyTemplates", "thymeleafTemplates", "assets")
            .forEach { dir ->
                testDataDir.resolve(dir).exists() shouldBe true
            }
    }
}

