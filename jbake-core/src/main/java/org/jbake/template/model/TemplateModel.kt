package org.jbake.template.model

import org.jbake.app.DocumentList
import org.jbake.model.BaseModel
import org.jbake.model.DocumentModel
import org.jbake.model.ModelAttributes
import org.jbake.template.DelegatingTemplateEngine
import java.io.Writer

open class TemplateModel : BaseModel {
    constructor()

    constructor(model: TemplateModel) {
        putAll(model)
    }

    var config: MutableMap<String, Any>
        get() = get(ModelAttributes.CONFIG) as MutableMap<String, Any>
        set(configModel) {
            put(ModelAttributes.CONFIG, configModel)
        }

    var content: DocumentModel
        get() = get(ModelAttributes.CONTENT) as DocumentModel
        set(content) {
            put(ModelAttributes.CONTENT, content)
        }

    var renderer: DelegatingTemplateEngine?
        get() = get(ModelAttributes.RENDERER) as DelegatingTemplateEngine?
        set(renderingEngine) {
            put(ModelAttributes.RENDERER, renderingEngine!!)
        }

    var numberOfPages: Int
        get() = get(ModelAttributes.NUMBER_OF_PAGES) as Int? ?: 0
        set(numberOfPages) {
            put(ModelAttributes.NUMBER_OF_PAGES, numberOfPages)
        }

    var currentPageNumber: Int
        get() = get(ModelAttributes.CURRENT_PAGE_NUMBERS) as Int? ?: 0
        set(currentPageNumber) {
            put(ModelAttributes.CURRENT_PAGE_NUMBERS, currentPageNumber)
        }

    var previousFilename: String
        get() = get(ModelAttributes.PREVIOUS_FILENAME) as String? ?: ""
        set(previousFilename) {
            put(ModelAttributes.PREVIOUS_FILENAME, previousFilename)
        }

    var nextFileName: String
        get() = get(ModelAttributes.NEXT_FILENAME) as String? ?: ""
        set(nextFileName) {
            put(ModelAttributes.NEXT_FILENAME, nextFileName)
        }

    var tag: String?
        get() = get(ModelAttributes.TAG) as String?
        set(tag) {
            put(ModelAttributes.TAG, tag!!)
        }

    var taggedPosts: DocumentList<*>
        get() = get(ModelAttributes.TAGGED_POSTS) as DocumentList<*>
        set(taggedPosts) {
            put(ModelAttributes.TAGGED_POSTS, taggedPosts)
        }

    var taggedDocuments: DocumentList<*>
        get() = get(ModelAttributes.TAGGED_DOCUMENTS) as DocumentList<*>
        set(taggedDocuments) {
            put(ModelAttributes.TAGGED_DOCUMENTS, taggedDocuments)
        }

    var version: String?
        get() = get(ModelAttributes.VERSION) as String?
        set(version) {
            put(ModelAttributes.VERSION, version!!)
        }

    val writer: Writer?
        get() = get(ModelAttributes.OUT) as Writer?
}
