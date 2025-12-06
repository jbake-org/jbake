@file:Suppress("UnusedVariable")

package org.jbake.examples

import org.jbake.app.ContentStore
import org.jbake.app.configuration.JBakeConfiguration
import org.jbake.model.DocumentModel
import org.jbake.template.DelegatingTemplateEngine
import org.jbake.template.model.PaginationContext
import org.jbake.template.model.RenderContext
import org.jbake.template.model.TagContext
import org.jbake.template.model.TypedTemplateModel
import java.io.File

/**
 * Examples demonstrating how to use the new type-safe architecture.
 */
@Suppress("unused")
object RenderContextExamples {

    /**
     * Example 1: Creating a simple render context for rendering a document.
     */
    fun createSimpleContext(config: JBakeConfiguration, db: ContentStore, document: DocumentModel, renderer: DelegatingTemplateEngine)
        = RenderContext(config, db, document, renderer)

    /**
     * Example 2: Using apply{} for construction (idiomatic Kotlin).
     */
    fun createContextWithApply(config: JBakeConfiguration, db: ContentStore)
        = RenderContext(
            config = config,
            db = db,
            version = "100.0.0",
            customData = mapOf("customKey" to "customValue"),
            content = DocumentModel().apply {
                title = "My Page"
                body = "<p>Hello World</p>"
                type = "page"
            },
        )

    /**
     * Example 3: Creating a context with pagination for index pages.
     */
    fun createPaginatedContext(config: JBakeConfiguration, db: ContentStore, currentPage: Int, totalPages: Int)
        = RenderContext(
            config = config,
            db = db,
            pagination = PaginationContext(
                currentPage = currentPage,
                totalPages = totalPages,
                previousFilename = if (currentPage > 1) "index${currentPage - 1}.html" else null,
                nextFilename = if (currentPage < totalPages) "index${currentPage + 1}.html" else null
            )
        )

    /**
     * Example 4: Creating a context for tag pages.
     */
    fun createTagContext(config: JBakeConfiguration, db: ContentStore, tagName: String)
        = RenderContext(
            config = config,
            db = db,
            tag = TagContext(
                name = tagName,
                taggedPosts = db.getPublishedPostsByTag(tagName),
                taggedDocuments = db.getPublishedDocumentsByTag(tagName)
            )
        )

    /**
     * Example 5: Modifying an existing context using copy() (data class feature).
     */
    fun modifyContext(original: RenderContext, newDocument: DocumentModel) =
        original.copy(content = newDocument)

    /**
     * Example 6: Accessing lazy-loaded collections.
     */
    fun accessCollections(context: RenderContext) {
        // These are evaluated lazily - only query database when accessed
        val posts = context.publishedPosts
        val pages = context.publishedPages
        val tags = context.allTags

        println("Found ${posts.size} posts, ${pages.size} pages, and ${tags.size} tags")
    }

    /**
     * Example 7: Using TypedTemplateModel for template rendering.
     */
    fun createTypedModel(context: RenderContext): TypedTemplateModel {
        return TypedTemplateModel(context)
    }

    /**
     * Example 8: Converting between old and new for backward compatibility.
     */
    fun backwardCompatibility(context: RenderContext) {
        // Convert to legacy map format if needed for old templates
        @Suppress("DEPRECATION")
        val legacyMap = context.toLegacyMap()

        // Access via map (old way - not recommended)
        val config = legacyMap["config"]
        val content = legacyMap["content"]
    }

    /**
     * Example 9: Complete rendering workflow with the new architecture.
     */
    fun renderWorkflow(
        config: JBakeConfiguration,
        db: ContentStore,
        document: DocumentModel,
        renderer: DelegatingTemplateEngine,
        outputFile: File
    ) {
        // 1. Create the render context (idiomatic Kotlin)
        val context = RenderContext(
            config = config,
            db = db,
            content = document,
            renderer = renderer
        )

        // 2. Access data type-safely
        val title = context.content?.title ?: "Untitled"
        val publishedPosts = context.publishedPosts

        // 3. Render (using the new renderWithContext method in Renderer)
        // renderer.renderWithContext(context, outputFile, templateName)

        println("Rendering $title with ${publishedPosts.size} posts available")
    }

    /**
     * Example 10: Custom data for template-specific needs.
     */
    fun customTemplateData(config: JBakeConfiguration, db: ContentStore)
        = RenderContext(
            config = config,
            db = db,
            customData = mapOf(
                "siteTitle" to "My Awesome Blog",
                "author" to "John Doe",
                "year" to 2024
            )
        )
}

