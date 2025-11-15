package org.jbake.template.model

import org.jbake.app.DocumentList
import org.jbake.model.BaseModel
import org.jbake.model.DocumentModel
import org.jbake.model.ModelAttributes
import org.jbake.template.DelegatingTemplateEngine
import java.io.Writer

open class TemplateModel : BaseModel {
    constructor()

    constructor(model: TemplateModel?) {
        putAll(model!!)
    }

    var config: MutableMap<String?, Any?>?
        get() = get(ModelAttributes.CONFIG) as MutableMap<String?, Any?>?
        set(configModel) {
            put(ModelAttributes.CONFIG, configModel)
        }

    var content: DocumentModel?
        get() = get(ModelAttributes.CONTENT) as DocumentModel?
        set(content) {
            put(ModelAttributes.CONTENT, content)
        }

    var renderer: DelegatingTemplateEngine?
        get() = get(ModelAttributes.RENDERER) as DelegatingTemplateEngine?
        set(renderingEngine) {
            put(ModelAttributes.RENDERER, renderingEngine)
        }

    fun setNumberOfPages(numberOfPages: Int) {
        put(ModelAttributes.NUMBER_OF_PAGES, numberOfPages)
    }

    fun setCurrentPageNuber(currentPageNumber: Int) {
        put(ModelAttributes.CURRENT_PAGE_NUMBERS, currentPageNumber)
    }

    fun setPreviousFilename(previousFilename: String?) {
        put(ModelAttributes.PREVIOUS_FILENAME, previousFilename)
    }

    fun setNextFileName(nextFilename: String?) {
        put(ModelAttributes.NEXT_FILENAME, nextFilename)
    }

    var tag: String?
        get() = get(ModelAttributes.TAG) as String?
        set(tag) {
            put(ModelAttributes.TAG, tag)
        }

    fun setTaggedPosts(taggedPosts: DocumentList<*>?) {
        put(ModelAttributes.TAGGED_POSTS, taggedPosts)
    }

    fun setTaggedDocuments(taggedDocuments: DocumentList<*>?) {
        put(ModelAttributes.TAGGED_DOCUMENTS, taggedDocuments)
    }

    fun setVersion(version: String?) {
        put(ModelAttributes.VERSION, version)
    }

    val writer: Writer?
        get() = get(ModelAttributes.OUT) as Writer?
}
