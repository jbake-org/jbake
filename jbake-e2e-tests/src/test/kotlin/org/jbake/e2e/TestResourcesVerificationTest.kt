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
        val propsFile = File(testDataDir, "jbake.properties")
        propsFile.exists() shouldBe true
        propsFile.readText() shouldNotBe ""
    }

    @Test
    fun `should have content directory`() {
        val testDataDir = getTestDataDir()
        val contentDir = File(testDataDir, "content")
        contentDir.exists() shouldBe true
        contentDir.isDirectory shouldBe true
    }

    @Test
    fun `should have blog posts`() {
        val testDataDir = getTestDataDir()
        val blogDir = File(testDataDir, "content/blog")
        blogDir.exists() shouldBe true
        blogDir.isDirectory shouldBe true
    }

    @Test
    fun `should have freemarker templates`() {
        val testDataDir = getTestDataDir()
        val templatesDir = File(testDataDir, "freemarkerTemplates")
        templatesDir.exists() shouldBe true
        templatesDir.isDirectory shouldBe true

        File(templatesDir, "index.ftl").exists() shouldBe true
        File(templatesDir, "page.ftl").exists() shouldBe true
        File(templatesDir, "post.ftl").exists() shouldBe true
    }

    @Test
    fun `should have groovy templates`() {
        val testDataDir = getTestDataDir()
        val templatesDir = File(testDataDir, "groovyTemplates")
        templatesDir.exists() shouldBe true
        templatesDir.isDirectory shouldBe true

        File(templatesDir, "index.gsp").exists() shouldBe true
        File(templatesDir, "page.gsp").exists() shouldBe true
        File(templatesDir, "post.gsp").exists() shouldBe true
    }

    @Test
    fun `should have thymeleaf templates`() {
        val testDataDir = getTestDataDir()
        val templatesDir = File(testDataDir, "thymeleafTemplates")
        templatesDir.exists() shouldBe true
        templatesDir.isDirectory shouldBe true

        File(templatesDir, "index.thyme").exists() shouldBe true
        File(templatesDir, "page.thyme").exists() shouldBe true
        File(templatesDir, "post.thyme").exists() shouldBe true
    }

    @Test
    fun `test data fixture should be complete`() {
        val testDataDir = getTestDataDir()

        // Check all essential directories exist
        listOf("content", "freemarkerTemplates", "groovyTemplates", "thymeleafTemplates", "assets")
            .forEach { dir ->
                File(testDataDir, dir).exists() shouldBe true
            }
    }
}

