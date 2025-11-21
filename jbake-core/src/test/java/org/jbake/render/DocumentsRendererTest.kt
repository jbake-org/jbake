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
import org.junit.jupiter.api.function.Executable
import org.mockito.*
import java.util.function.Function
import java.util.stream.Collectors

class DocumentsRendererTest {
    lateinit var documentsRenderer: DocumentsRenderer
    private lateinit var db: ContentStore
    private lateinit var renderer: Renderer
    private lateinit var configuration: JBakeConfiguration
    private lateinit var emptyTemplateModelList: DocumentList<DocumentModel>

    @Captor
    private val argument: ArgumentCaptor<DocumentModel>? = null

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        documentsRenderer = DocumentsRenderer()

        db = Mockito.mock<ContentStore>(ContentStore::class.java)
        renderer = Mockito.mock<Renderer>(Renderer::class.java)
        configuration = Mockito.mock<JBakeConfiguration?>(JBakeConfiguration::class.java)
        emptyTemplateModelList = DocumentList<DocumentModel>()
    }

    @Test
    fun shouldReturnZeroIfNothingHasRendered() {
        Mockito.`when`<DocumentList<DocumentModel>>(db!!.unrenderedContent).thenReturn(emptyTemplateModelList)

        val renderResponse = documentsRenderer!!.render(renderer!!, db!!, configuration)

        Assertions.assertThat(renderResponse).isEqualTo(0)
    }

    @Test
    fun shouldReturnCountOfProcessedDocuments() {
        // given:

        addDocumentType("customType")

        val templateModelList: DocumentList<DocumentModel> = DocumentList<DocumentModel>()
        templateModelList.add(emptyDocument())
        templateModelList.add(emptyDocument())

        // return given DocumentList for DocumentType 'custom type'
        Mockito.`when`<DocumentList<DocumentModel>>(db!!.unrenderedContent).thenReturn(templateModelList)
        Mockito.`when`<DocumentList<DocumentModel>>(db!!.getAllContent(ArgumentMatchers.any<String>()))
            .thenReturn(templateModelList)

        // when:
        val renderResponse = documentsRenderer!!.render(renderer!!, db!!, configuration)

        // then:
        Assertions.assertThat(renderResponse).isEqualTo(2)
    }

    @Test
    fun shouldThrowAnExceptionWithCollectedErrorMessages() {
        val fakeExceptionMessage = "fake exception"

        // expect
        org.junit.jupiter.api.Assertions.assertThrows<RenderingException?>(
            RenderingException::class.java, Executable {
                // given
                addDocumentType("customType")

                val templateModelList: DocumentList<DocumentModel> = DocumentList<DocumentModel>()
                val document = emptyDocument()
                val document2 = emptyDocument()
                templateModelList.add(document)
                templateModelList.add(document2)

                // throw an exception for every call of renderer's render method
                Mockito.doThrow(Exception(fakeExceptionMessage)).`when`<Renderer?>(renderer).render(
                    ArgumentMatchers.any<DocumentModel>(
                        DocumentModel::class.java
                    )
                )
                Mockito.`when`<DocumentList<DocumentModel>>(db!!.unrenderedContent).thenReturn(templateModelList)

                // when
                val renderResponse = documentsRenderer!!.render(renderer!!, db!!, configuration)

                // then
                Assertions.assertThat(renderResponse).isEqualTo(2)
            },
            fakeExceptionMessage + "\n" + fakeExceptionMessage
        )
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

        Mockito.`when`<DocumentList<DocumentModel>>(db!!.unrenderedContent).thenReturn(allDocs)
        Mockito.`when`<DocumentList<DocumentModel>>(db!!.getAllContent("page")).thenReturn(pageDocs)
        Mockito.`when`<DocumentList<DocumentModel>>(db!!.getAllContent("post")).thenReturn(postDocs)

        // when
        val renderResponse = documentsRenderer!!.render(renderer!!, db!!, configuration)

        // then
        Mockito.verify<Renderer?>(renderer, Mockito.times(7)).render(argument!!.capture()!!)
        val renderedDocs = asTitleToDocMap(argument.getAllValues())

        // page checks
        assertDocumentNavigation(renderedDocs.get(fifthTitle), thirdTitle, null)
        assertDocumentNavigation(renderedDocs.get(thirdTitle), firstTitle, fifthTitle)
        assertDocumentNavigation(renderedDocs.get(firstTitle), null, thirdTitle)

        // post checks
        assertDocumentNavigation(renderedDocs.get(seventhTitle), sixthTitle, null)
        assertDocumentNavigation(renderedDocs.get(sixthTitle), secondTitle, seventhTitle)
        assertDocumentNavigation(renderedDocs.get(fourthTitle), secondTitle, sixthTitle)
        assertDocumentNavigation(renderedDocs.get(secondTitle), null, sixthTitle)

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
        return values.stream()
            .collect(
                Collectors.toMap(
                    Function { doc: DocumentModel? -> doc!!.get(ModelAttributes.TITLE).toString() },
                    Function { doc: DocumentModel? -> doc })
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
