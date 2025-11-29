package org.jbake.e2e

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import org.jsoup.Jsoup
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.testcontainers.DockerClientFactory
import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.DockerImageName
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import java.util.stream.Stream

/**
 * End-to-end tests for JBake using TestContainers.
 * Tests various template engines and JBake configurations.
 * Uses shared test-data fixture from the project root.
 *
 * Note: These tests require Docker to be running. If Docker is not available,
 * the tests will be skipped with a warning message.
 */
class JBakeEndToEndTest {

    companion object {
        private const val JBAKE_IMAGE = "jbake/jbake:2.7.0-SNAPSHOT"
        private lateinit var outputDir: Path
        private lateinit var testDataDir: File

        @JvmStatic
        @BeforeAll
        fun setup() {
            outputDir = Files.createTempDirectory("jbake-e2e-")

            // Get test data directory from system property or default location
            val testDataPath = System.getProperty("jbake.test.data.dir", "../test-data/fixture")
            testDataDir = File(testDataPath).absoluteFile

            if (!testDataDir.exists()) {
                throw IllegalStateException("Test data directory not found: ${testDataDir.absolutePath}")
            }

            // Check if Docker is available using Testcontainers' built-in detection
            // This properly handles system Docker, rootless Docker, and various socket configurations
            try {
                val dockerAvailable = DockerClientFactory.instance().isDockerAvailable
                System.setProperty("jbake.test.docker.available", dockerAvailable.toString())
                if (dockerAvailable) {
                    println("Docker is available. E2E tests will run.")
                    println("Docker info: ${DockerClientFactory.instance().info}")
                } else {
                    println("WARNING: Docker not available. E2E tests will be skipped.")
                    println("To run E2E tests, ensure Docker is installed and running.")
                }
            } catch (e: Exception) {
                println("WARNING: Cannot detect Docker: ${e.message}")
                println("E2E tests will be skipped. Install Docker to run these tests.")
                System.setProperty("jbake.test.docker.available", "false")
            }

            println("Output directory: $outputDir")
            println("Test data directory: ${testDataDir.absolutePath}")
        }

        @JvmStatic
        @AfterAll
        fun cleanup() {
            // Optionally clean up output directory
            // outputDir.toFile().deleteRecursively()
        }

        @JvmStatic
        fun templateEngineProvider(): Stream<Arguments> {
            return Stream.of(
                Arguments.of("freemarker", "freemarkerTemplates"),
                Arguments.of("groovy", "groovyTemplates"),
                Arguments.of("thymeleaf", "thymeleafTemplates")
            )
        }

        @JvmStatic
        fun bakeOptionsProvider(): Stream<Arguments> {
            return Stream.of(
                Arguments.of("freemarker", "freemarkerTemplates", emptyList<String>()),
                Arguments.of("groovy", "groovyTemplates", emptyList<String>()),
                Arguments.of("thymeleaf", "thymeleafTemplates", emptyList<String>())
            )
        }
    }

    /**
     * Checks if Docker is available and skips the test if not.
     * This should be called at the beginning of each E2E test method.
     */
    private fun requireDockerAvailable() {
        assumeTrue(
            System.getProperty("jbake.test.docker.available", "false").toBoolean(),
            "Docker is not available. Skipping E2E tests. Run 'docker info' to verify Docker is running."
        )
    }

    @ParameterizedTest(name = "Test JBake with {0} template engine")
    @MethodSource("templateEngineProvider")
    fun `should successfully bake site with different template engines`(
        templateEngine: String,
        templateDir: String
    ) {
        // Skip if Docker is not available
        requireDockerAvailable()

        // Arrange
        val testOutputDir = outputDir.resolve(templateEngine)
        Files.createDirectories(testOutputDir)

        val templatesPath = File(testDataDir, templateDir)

        // Act
        val container = createJBakeContainer(testDataDir, templatesPath, testOutputDir)

        try {
            container.start()
            // Use the new mount paths: content, templates, and output are in separate non-nested directories
            val result = container.execInContainer("jbake", "/jbake/content", "/jbake/output", "-t", "/jbake/templates")
            println("JBake output: ${result.stdout}")
            if (result.exitCode != 0) {
                println("JBake error: ${result.stderr}")
            }

            // Assert - Verify output files were created
            // Output is directly in testOutputDir since we mounted it to /jbake/output
            verifyBasicStructure(testOutputDir.toFile())
            verifyIndexPage(testOutputDir.toFile())
            verifyAboutPage(testOutputDir.toFile())
            verifyBlogPosts(testOutputDir.toFile())

        } finally {
            container.stop()
        }
    }

    @ParameterizedTest(name = "Test JBake with {0} and options {2}")
    @MethodSource("bakeOptionsProvider")
    fun `should successfully bake with different options`(
        templateEngine: String,
        templateDir: String,
        options: List<String>
    ) {
        // Skip if Docker is not available
        requireDockerAvailable()

        // Arrange
        val testName = "${templateEngine}-${options.joinToString("-").ifEmpty { "default" }}"
        val testOutputDir = outputDir.resolve(testName)
        Files.createDirectories(testOutputDir)

        val templatesPath = File(testDataDir, templateDir)

        // Act
        val container = createJBakeContainer(testDataDir, templatesPath, testOutputDir)

        try {
            container.start()

            val command = mutableListOf("jbake", "/jbake/content", "/jbake/output", "-t", "/jbake/templates").apply {
                addAll(options)
            }

            val result = container.execInContainer(*command.toTypedArray())

            // Assert
            result.exitCode shouldBe 0

            // Output is directly in testOutputDir
            verifyBasicStructure(testOutputDir.toFile())

        } finally {
            container.stop()
        }
    }

    @ParameterizedTest(name = "Verify content rendering with {0}")
    @MethodSource("templateEngineProvider")
    fun `should correctly render content with template engine`(
        templateEngine: String,
        templateDir: String
    ) {
        requireDockerAvailable()

        // Arrange
        val testOutputDir = outputDir.resolve("content-$templateEngine")
        Files.createDirectories(testOutputDir)

        val templatesPath = File(testDataDir, templateDir)

        // Act
        val container = createJBakeContainer(testDataDir, templatesPath, testOutputDir)

        try {
            container.start()
            val result = container.execInContainer("jbake", "/jbake/content", "/jbake/output", "-t", "/jbake/templates")

            // Assert - Verify content rendering
            result.exitCode shouldBe 0
            // Output is directly in testOutputDir
            verifyContentRendering(testOutputDir.toFile(), templateEngine)

        } finally {
            container.stop()
        }
    }

    private fun createJBakeContainer(
        fixtureDir: File,
        templatesPath: File,
        outputDir: Path
    ): GenericContainer<*> {
        // Avoid nested mounts to prevent "read-only file system" errors
        // See: https://github.com/testcontainers/testcontainers-java/issues/11212
        // Mount to separate non-nested paths
        return GenericContainer(DockerImageName.parse(JBAKE_IMAGE))
            .withFileSystemBind(fixtureDir.absolutePath, "/jbake/content", org.testcontainers.containers.BindMode.READ_ONLY)
            .withFileSystemBind(templatesPath.absolutePath, "/jbake/templates", org.testcontainers.containers.BindMode.READ_ONLY)
            .withFileSystemBind(outputDir.toAbsolutePath().toString(), "/jbake/output", org.testcontainers.containers.BindMode.READ_WRITE)
            .withStartupTimeout(Duration.ofMinutes(2))
    }

    private fun verifyBasicStructure(outputDir: File) {
        outputDir.exists() shouldBe true
        outputDir.isDirectory shouldBe true

        // Check that some output was generated
        val files = outputDir.listFiles()
        files shouldNotBe null
        files!!.isNotEmpty() shouldBe true
    }

    private fun verifyIndexPage(outputDir: File) {
        val indexFile = File(outputDir, "index.html")
        indexFile.exists() shouldBe true

        val content = indexFile.readText()
        content shouldContain "Blog Posts"
        content shouldContain "JBake E2E Test Site"
    }

    private fun verifyAboutPage(outputDir: File) {
        val aboutFile = File(outputDir, "about.html")
        aboutFile.exists() shouldBe true

        val content = aboutFile.readText()
        content shouldContain "About This Site"
        content shouldContain "sample JBake site"
    }

    private fun verifyBlogPosts(outputDir: File) {
        val blogDir = File(outputDir, "blog/2023")
        blogDir.exists() shouldBe true

        val firstPost = File(blogDir, "first-post.html")
        firstPost.exists() shouldBe true

        val firstPostContent = firstPost.readText()
        firstPostContent shouldContain "My First Post"
        firstPostContent shouldContain "testing"

        val secondPost = File(blogDir, "second-post.html")
        secondPost.exists() shouldBe true

        val secondPostContent = secondPost.readText()
        secondPostContent shouldContain "Second Post"
        secondPostContent shouldContain "Static Site Generators"
    }

    private fun verifyContentRendering(outputDir: File, templateEngine: String) {
        val indexFile = File(outputDir, "index.html")
        val doc = Jsoup.parse(indexFile, "UTF-8")

        // Verify basic HTML structure
        doc.select("header").isNotEmpty() shouldBe true
        doc.select("footer").isNotEmpty() shouldBe true

        // Verify navigation
        val navLinks = doc.select("nav a")
        navLinks.size shouldBe 2

        // Verify posts are listed
        val postLinks = doc.select("article h2 a, article h1 a")
        postLinks.size shouldBe 2

        println("Successfully verified content rendering for $templateEngine")
    }
}

