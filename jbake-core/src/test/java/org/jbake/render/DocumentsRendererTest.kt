package org.jbake.render

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.*
import org.jbake.app.ContentStore
import org.jbake.app.DocumentList
import org.jbake.app.Renderer
import org.jbake.app.RenderingException
import org.jbake.app.configuration.JBakeConfiguration
import org.jbake.model.DocumentModel
import org.jbake.model.DocumentTypeRegistry.addDocumentType
import org.jbake.model.ModelAttributes

class DocumentsRendererTest : StringSpec({
    lateinit var documentsRenderingTool: DocumentsRenderingTool
    lateinit var db: ContentStore
    lateinit var renderer: Renderer
    lateinit var configuration: JBakeConfiguration
    lateinit var emptyTemplateModelList: DocumentList<DocumentModel>

    beforeTest {
        documentsRenderingTool = DocumentsRenderingTool()
        db = mockk(relaxed = true)
        renderer = mockk(relaxed = true)
        configuration = mockk(relaxed = true)
        emptyTemplateModelList = DocumentList()
    }

    "shouldReturnZeroIfNothingHasRendered" {
        every { db.unrenderedContent } returns emptyTemplateModelList

        val renderResponse = documentsRenderingTool.render(renderer, db, configuration)

        renderResponse shouldBe 0
    }

    "shouldReturnCountOfProcessedDocuments" {
        addDocumentType("customType")

        val templateModelList = DocumentList<DocumentModel>()
        templateModelList.add(DocumentModel())
        templateModelList.add(DocumentModel())

        every { db.unrenderedContent } returns templateModelList
        every { db.getAllContent(any<String>()) } returns templateModelList

        val renderResponse = documentsRenderingTool.render(renderer, db, configuration)

        renderResponse shouldBe 2
    }

    "shouldThrowAnExceptionWithCollectedErrorMessages" {
        val fakeExceptionMessage = "fake exception"

        addDocumentType("customType")

        val templateModelList = DocumentList<DocumentModel>()
        val document = DocumentModel()
        val document2 = DocumentModel()
        templateModelList.add(document)
        templateModelList.add(document2)

        every { renderer.render(any<DocumentModel>()) } throws RuntimeException(fakeExceptionMessage)
        every { db.unrenderedContent } returns templateModelList
        every { db.getAllContent(any<String>()) } returns templateModelList

        val exception = shouldThrow<RenderingException> {
            documentsRenderingTool.render(renderer, db, configuration)
        }
        exception.message shouldContain fakeExceptionMessage
    }

    "shouldContainPostNavigation" {
        addDocumentType("customType")

        val firstTitle = "First Document"
        val firstDoc = createSimpleDocument(firstTitle, ModelAttributes.Status.PUBLISHED, "page")
        val secondTitle = "Second Document"
        val secondDoc = createSimpleDocument(secondTitle, ModelAttributes.Status.PUBLISHED, "post")
        val thirdTitle = "Third Document"
        val thirdDoc = createSimpleDocument(thirdTitle, ModelAttributes.Status.PUBLISHED, "page")
        val fourthTitle = "Fourth Document (draft)"
        val fourthDoc = createSimpleDocument(fourthTitle, ModelAttributes.Status.DRAFT, "post")
        val fifthTitle = "Fifth Document"
        val fifthDoc = createSimpleDocument(fifthTitle, ModelAttributes.Status.PUBLISHED, "page")
        val sixthTitle = "Sixth Document"
        val sixthDoc = createSimpleDocument(sixthTitle, ModelAttributes.Status.PUBLISHED, "post")
        val seventhTitle = "Seventh Document"
        val seventhDoc = createSimpleDocument(seventhTitle, ModelAttributes.Status.PUBLISHED, "post")

        val allDocs = DocumentList<DocumentModel>()
        allDocs.add(seventhDoc)
        allDocs.add(sixthDoc)
        allDocs.add(fifthDoc)
        allDocs.add(fourthDoc)
        allDocs.add(thirdDoc)
        allDocs.add(secondDoc)
        allDocs.add(firstDoc)

        val pageDocs = DocumentList<DocumentModel>()
        pageDocs.add(fifthDoc)
        pageDocs.add(thirdDoc)
        pageDocs.add(firstDoc)

        val postDocs = DocumentList<DocumentModel>()
        postDocs.add(seventhDoc)
        postDocs.add(sixthDoc)
        postDocs.add(fourthDoc)
        postDocs.add(secondDoc)

        every { db.unrenderedContent } returns allDocs
        every { db.getAllContent("page") } returns pageDocs
        every { db.getAllContent("post") } returns postDocs

        val capturedDocs = mutableListOf<DocumentModel>()
        every { renderer.render(capture(capturedDocs)) } just Runs

        val renderResponse = documentsRenderingTool.render(renderer, db, configuration)

        verify(exactly = 7) { renderer.render(any()) }

        val renderedDocs = capturedDocs.associateBy { it.title }

        // page checks
        assertDocumentNavigation(renderedDocs[fifthTitle], thirdTitle, null)
        assertDocumentNavigation(renderedDocs[thirdTitle], firstTitle, fifthTitle)
        assertDocumentNavigation(renderedDocs[firstTitle], null, thirdTitle)

        // post checks
        assertDocumentNavigation(renderedDocs[seventhTitle], sixthTitle, null)
        assertDocumentNavigation(renderedDocs[sixthTitle], secondTitle, seventhTitle)
        assertDocumentNavigation(renderedDocs[fourthTitle], secondTitle, sixthTitle)
        assertDocumentNavigation(renderedDocs[secondTitle], null, sixthTitle)

        renderResponse shouldBe 7
    }
}) {
    companion object {
        fun assertDocumentNavigation(
            renderedDoc: DocumentModel?,
            prevDocumentTitle: String?,
            nextDocumentTitle: String?
        ) {
            renderedDoc.shouldNotBeNull()
            val prevContent = renderedDoc["previousContent"] as? DocumentModel
            val nextContent = renderedDoc["nextContent"] as? DocumentModel

            prevContent?.title shouldBe prevDocumentTitle
            nextContent?.title shouldBe nextDocumentTitle
        }

        fun createSimpleDocument(title: String, status: String?, docType: String): DocumentModel {
            val simpleDoc = DocumentModel()
            val uri = title.replace(" ", "_")
            simpleDoc.noExtensionUri = uri
            simpleDoc.uri = uri
            simpleDoc.type = docType
            simpleDoc.title = title
            simpleDoc.status = status
            return simpleDoc
        }
    }
}

