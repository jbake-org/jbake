package org.jbake.model

object ModelAttributes {
    // Template model keys - these are the keys used in FreeMarker templates

    // ===== DOCUMENT METADATA (from AsciidoctorEngine/MarkupEngine parsing) =====
    // Type: String,                Assigned: ParserContext.body/MarkupEngine.processBody (via ParserContext), Accessed: DocumentModel/HsqldbContentRepository/Neo4jContentRepository
    const val DOC_BODY_RENDERED: String = "body"                              // Rendered HTML content of the document.
    // Type: Date?,                 Assigned: AsciidoctorEngine (revdate parser)/ParserContext.date, Accessed: DocumentModel/Crawler (status transitions)/tests (no date specified)
    const val DOC_DATE: String = "date"                                       // Publication date of the document (Date object).
    // Type: String,                Assigned: BaseModel/TypedBaseModel (DB hydration), Accessed: DocumentModel
    const val DOC_NAME: String = "name"                                       // Document name derived from filename.
    // Type: String?,               Assigned: AsciidoctorEngine (jbake-status)/Crawler.addAdditionalDocumentAttributes, Accessed: DocumentModel/Renderer/Crawler/OrientDBContentRepository (defaults to empty string)
    const val DOC_STATUS: String = "status"                                   // Publication status (draft/published).
    // Type: List<String>,          Assigned: AsciidoctorEngine (jbake-tags)/Parser.setTags, Accessed: DocumentModel/Renderer (defaults to empty list)
    const val DOC_TAGS: String = "tags"                                       // List of tags for current document.
    // Type: String?,               Assigned: AsciidoctorEngine.doctitle/ParserContext, Accessed: DocumentModel
    const val DOC_TITLE: String = "title"                                     // Document title from header or filename.
    // Type: String,                Assigned: AsciidoctorEngine (jbake-type)/Crawler (data files)/Renderer.buildSimpleModel, Accessed: DocumentModel/OrientDBContentRepository (defaults to empty string)
    const val DOC_TYPE: String = "type"                                       // Document type (post/page).

    // ===== FILE SYSTEM INFO (from Crawler processing) =====
    // Type: String?,               Assigned: Crawler.addAdditionalDocumentAttributes, Accessed: DocumentModel/OrientDBContentRepository
    const val FS_DOC_SHA1: String = "sha1"                                    // SHA1 hash of source file for change detection.
    // Type: String?,               Assigned: Crawler.addAdditionalDocumentAttributes, Accessed: DocumentModel
    const val FS_DOC_SOURCE_PATH_ABS: String = "file"                         // Absolute path to source file on disk.
    // Type: String?,               Assigned: Crawler.addAdditionalDocumentAttributes (when uriWithoutExtension true), Accessed: DocumentModel
    const val FS_DOC_OUTPUT_URI_NOEXT: String = "noExtensionUri"              // URI without .html extension.
    // Type: Boolean,               Assigned: Crawler.addAdditionalDocumentAttributes, Accessed: DocumentModel/OrientDBContentRepository
    const val FS_DOC_IS_CACHED_IN_DB: String = "cached"                       // Boolean indicating if document is cached in database.
    // Type: Boolean,               Assigned: Crawler.addAdditionalDocumentAttributes/Db hydration, Accessed: DocumentModel/OrientDBContentRepository
    const val FS_DOC_WAS_RENDERED: String = "rendered"                        // Boolean indicating if document has been rendered.
    // Type: String?,               Assigned: Crawler.addAdditionalDocumentAttributes/DefaultRenderingConfig.model, Accessed: DocumentModel
    const val FS_DOC_OUTPUT_URI: String = "uri"                               // Final output URI for the document.
    // Type: String?,               Assigned: Crawler.addAdditionalDocumentAttributes/db rehydration, Accessed: DocumentModel/OrientDBContentRepository
    const val FS_DOC_SOURCE_REL_URI: String = "sourceuri"                     // URI relative to content directory.
    // Type: String,                Assigned: Crawler.addAdditionalDocumentAttributes/Renderer.buildSimpleModel, Accessed: DocumentModel/CrawlerTests
    const val FS_REL_FROM_DOC_TO_SITEROOT: String = "rootpath"                // Relative path from document to site root.

    // ===== TEMPLATE CONTEXT (from Renderer) =====
    // Type: DocumentModel,         Assigned: Renderer.render/Renderer.renderIndexPaging/Renderer.renderTags/DefaultRenderingConfig.model/TemplateModel.fromContext, Accessed: TemplateModel
    const val TMPL_CONTENT_MODEL: String = "content"                          // Current document model being rendered.
    // Type: ContentStore,          Assigned: DelegatingTemplateEngine (eager model)/FreemarkerTemplateEngine.LazyModel, Accessed: FreemarkerTemplateEngine adapters
    const val TMPL_DB_ACCESS: String = "db"                                   // ContentStore instance for database queries in templates.
    // Type: Writer,                Assigned: DelegatingTemplateEngine.renderDocument (writer injection), Accessed: TemplateModel
    const val TMPL_OUT_WRITER: String = "out"                                 // Writer for template output.
    // Type: Map<String, Any>,      Assigned: Renderer.renderTags/Renderer.renderTagsIndex/TemplateModel.fromContext/FreemarkerTemplateEngine (merged config), Accessed: TemplateModel/FreemarkerTemplateEngine
    const val TMPL_JBAKE_CONFIG: String = "config"                            // JBake configuration map for templates.
    // Type: DelegatingTemplateEngine?, Assigned: Renderer.render/Renderer.renderIndexPaging/Renderer.renderTags/DefaultRenderingConfig.model, Accessed: TemplateModel
    const val TMPL_ENGINE: String = "renderer"                                // Template engine instance.

    // ===== NAVIGATION (from pagination) =====
    // Type: Int,                  Assigned: Renderer.renderIndexPaging/DefaultRenderingConfig.model/TemplateModel.fromContext, Accessed: TemplateModel
    const val PAGI_CUR_PAGE_NUMBER: String = "currentPageNumber"              // Current page number in pagination.
    // Type: DocumentModel?,       Assigned: DocumentsRenderingTool.getContentForNav, Accessed: DocumentModel/Renderer
    const val PAGI_NEXT_CONTENT: String = "nextContent"                       // Next document in chronological order.
    // Type: String?,              Assigned: Renderer.renderIndexPaging/DefaultRenderingConfig.model/TemplateModel.fromContext, Accessed: TemplateModel
    const val PAGI_NEXT_FILENAME: String = "nextFileName"                     // Filename of next page in pagination.
    // Type: DocumentModel?,       Assigned: DocumentsRenderingTool.getContentForNav, Accessed: DocumentModel/Renderer
    const val PAGI_PREV_CONTENT: String = "previousContent"                   // Previous document in chronological order.
    // Type: String?,              Assigned: Renderer.renderIndexPaging/DefaultRenderingConfig.model/TemplateModel.fromContext, Accessed: TemplateModel
    const val PAGI_PREV_FILENAME: String = "previousFileName"                 // Filename of previous page in pagination.
    // Type: Int,                  Assigned: Renderer.renderIndexPaging/DefaultRenderingConfig.model/TemplateModel.fromContext, Accessed: TemplateModel
    const val PAGI_TOTAL_PAGES_COUNT: String = "numberOfPages"                // Total number of pages in pagination.

    // ===== TAG FILTERING =====
    // Type: List<String>,          Assigned: Renderer.renderTagsIndex/TagsExtractor.collectTags, Accessed: FreemarkerTemplateEngine/tag templates
    const val TAGS_ALL: String = "alltags"                                    // List of all unique tags across all documents.
    // Type: String?,               Assigned: Renderer.renderTags/TemplateModel.fromContext, Accessed: TemplateModel/Renderer/tag templates
    const val TAGS_CURRENT_TAG: String = "tag"                                // Current tag being filtered on.
    // Type: DocumentList<DocumentModel>, Assigned: TagsExtractor.populateTagContext/TemplateModel.fromContext, Accessed: TemplateModel
    const val TAGS_DOCS_TAGGED_CUR: String = "tagged_documents"               // Documents tagged with current tag.
    // Type: DocumentList<DocumentModel>, Assigned: TagsExtractor.populateTagContext/TemplateModel.fromContext, Accessed: TemplateModel
    const val TAGS_POSTS_TAGGED_CUR: String = "tagged_posts"                  // Posts tagged with current tag.

    // ===== DATA ACCESS =====
    // Type: Map<String, Any>,      Assigned: FreemarkerTemplateEngine (DataFileUtil adapter)/TemplateModel.fromContext, Accessed: FreemarkerTemplateEngine
    const val DATA_FILES: String = "data"                                     // Data files loaded from data directory.

    // ===== GLOBAL =====
    // Type: String,                Assigned: FreemarkerTemplateEngine (date adapter), Accessed: FreemarkerTemplateEngine/templates
    const val GLOB_PUBLISHING_DATE_FORMATTED: String = "published_date"       // Formatted publication date string.
    // Type: String,                Assigned: Renderer.render/DefaultRenderingConfig.model/TemplateModel.fromContext, Accessed: TemplateModel
    const val GLOB_JBAKE_VERSION: String = "version"                          // JBake version string.


    /**
     * Possible values of the [ModelAttributes.DOC_STATUS] property
     */
    object Status {
        const val PUBLISHED_DATE: String = "published-date"
        const val PUBLISHED: String = "published"
        const val DRAFT: String = "draft"
    }
}
