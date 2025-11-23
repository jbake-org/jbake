package org.jbake.render

import org.assertj.core.api.Assertions
import org.jbake.app.ContentStore
import org.jbake.app.DocumentList
import org.jbake.app.Renderer
import org.jbake.app.configuration.JBakeConfiguration
import org.jbake.model.DocumentModel
import org.jbake.model.DocumentTypes.addDocumentType
import org.jbake.model.ModelAttributes
import org.jbake.template.RenderingException
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertThrows
import org.mockito.*
import java.util.function.Function
import java.util.stream.Collectors

// Helper functions for Kotlin null-safety with Mockito
fun <T> capture(captor: ArgumentCaptor<T>): T = captor.capture() ?: null as T
@Suppress("UNCHECKED_CAST")
fun <T> any(type: Class<T>): T {
    ArgumentMatchers.any(type)
    return null as T
}
@Suppress("UNCHECKED_CAST")
fun <T> anyNullable(type: Class<T>): T {
    ArgumentMatchers.nullable(type)
    return null as T
}

class DocumentsRendererTest {
    lateinit var documentsRenderer: DocumentsRenderer
    private lateinit var db: ContentStore
    private lateinit var renderer: Renderer
    private lateinit var configuration: JBakeConfiguration
    private lateinit var emptyTemplateModelList: DocumentList<DocumentModel>
    private lateinit var argument: ArgumentCaptor<DocumentModel>

    @Before
    fun setUp() {
        documentsRenderer = DocumentsRenderer()

        db = Mockito.mock(ContentStore::class.java)
        renderer = Mockito.mock(Renderer::class.java)
        configuration = Mockito.mock(JBakeConfiguration::class.java)
        emptyTemplateModelList = DocumentList()
        argument = ArgumentCaptor.forClass(DocumentModel::class.java)
    }

    @Test
    fun shouldReturnZeroIfNothingHasRendered() {
        Mockito.`when`(db.unrenderedContent).thenReturn(emptyTemplateModelList)

        val renderResponse = documentsRenderer.render(renderer, db, configuration)

        Assertions.assertThat(renderResponse).isEqualTo(0)
    }

    @Test
    fun shouldReturnCountOfProcessedDocuments() {
        // given:
        addDocumentType("customType")

        val templateModelList: DocumentList<DocumentModel> = DocumentList()
        templateModelList.add(emptyDocument())
        templateModelList.add(emptyDocument())

        // return given DocumentList for DocumentType 'custom type'
        Mockito.`when`(db.unrenderedContent).thenReturn(templateModelList)
        Mockito.`when`(db.getAllContent(any(String::class.java))).thenReturn(templateModelList)

        // when:
        val renderResponse = documentsRenderer.render(renderer, db, configuration)

        // then:
        Assertions.assertThat(renderResponse).isEqualTo(2)
    }

    @Test
    fun shouldThrowAnExceptionWithCollectedErrorMessages() {
        val fakeExceptionMessage = "fake exception"

        // given
        addDocumentType("customType")

        val templateModelList: DocumentList<DocumentModel> = DocumentList()
        val document = emptyDocument()
        val document2 = emptyDocument()
        templateModelList.add(document)
        templateModelList.add(document2)

        // throw an exception for every call of renderer's render method
        Mockito.doThrow(RuntimeException(fakeExceptionMessage))
            .`when`(renderer).render(anyNullable(DocumentModel::class.java))

        Mockito.`when`(db.unrenderedContent).thenReturn(templateModelList)

        // expect
        val executable: () -> Unit = {
            // when
            documentsRenderer.render(renderer, db, configuration)
        }

        assertThrows(RenderingException::class.java, executable, fakeExceptionMessage + "\n" + fakeExceptionMessage)
    }

    @Test
    fun shouldContainPostNavigation() {
        // given
        addDocumentType("customType")

        val firstTitle = "First Document"
        val firstDoc = simpleDocument(firstTitle, ModelAttributes.Status.PUBLISHED, "page")
        val secondTitle = "Second Document"
        val secondDoc = simpleDocument(secondTitle, ModelAttributes.Status.PUBLISHED, "post")
        val thirdTitle = "Third Document"
        val thirdDoc = simpleDocument(thirdTitle, ModelAttributes.Status.PUBLISHED, "page")
        val fourthTitle = "Fourth Document (draft)"
        val fourthDoc = simpleDocument(fourthTitle, ModelAttributes.Status.DRAFT, "post")
        val fifthTitle = "Fifth Document"
        val fifthDoc = simpleDocument(fifthTitle, ModelAttributes.Status.PUBLISHED, "page")
        val sixthTitle = "Sixth Document"
        val sixthDoc = simpleDocument(sixthTitle, ModelAttributes.Status.PUBLISHED, "post")
        val seventhTitle = "Seventh Document"
        val seventhDoc = simpleDocument(seventhTitle, ModelAttributes.Status.PUBLISHED, "post")

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

        Mockito.`when`(db.unrenderedContent).thenReturn(allDocs)
        Mockito.`when`(db.getAllContent("page")).thenReturn(pageDocs)
        Mockito.`when`(db.getAllContent("post")).thenReturn(postDocs)

        // when
        val renderResponse = documentsRenderer.render(renderer, db, configuration)

        // then
        Mockito.verify(renderer, Mockito.times(7)).render(capture(argument))
        val renderedDocs = asTitleToDocMap(argument.getAllValues())

        // page checks
        assertDocumentNavigation(renderedDocs[fifthTitle], thirdTitle, null)
        assertDocumentNavigation(renderedDocs[thirdTitle], firstTitle, fifthTitle)
        assertDocumentNavigation(renderedDocs[firstTitle], null, thirdTitle)

        // post checks
        assertDocumentNavigation(renderedDocs[seventhTitle], sixthTitle, null)
        assertDocumentNavigation(renderedDocs[sixthTitle], secondTitle, seventhTitle)
        assertDocumentNavigation(renderedDocs[fourthTitle], secondTitle, sixthTitle)
        assertDocumentNavigation(renderedDocs[secondTitle], null, sixthTitle)

        Assertions.assertThat(renderResponse).isEqualTo(7)
    }

    private fun assertDocumentNavigation(
        renderedDoc: MutableMap<String,  Any>?,
        prevDocumentTitle: String?, nextDocumentTitle: String?
    ) {
        Assertions.assertThat(renderedDoc).flatExtracting(
            "previousContent." + ModelAttributes.TITLE,
            "nextContent." + ModelAttributes.TITLE
        )
            .containsExactly(prevDocumentTitle, nextDocumentTitle)
    }

    private fun asTitleToDocMap(values: MutableList<DocumentModel>): MutableMap<String, MutableMap<String,  Any>> {
        return values.stream().collect(
            Collectors.toMap(
            Function { doc: DocumentModel -> doc.get(ModelAttributes.TITLE).toString() },
            Function { doc: DocumentModel -> doc })
            )
    }

    private fun emptyDocument(): DocumentModel {
        return DocumentModel()
    }

    private fun simpleDocument(title: String, status: String?, docType: String): DocumentModel {
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
