package org.jbake.model;

import org.jbake.app.DocumentList;
import org.jbake.template.DelegatingTemplateEngine;

import java.io.Writer;
import java.util.Collections;
import java.util.Map;

public class TemplateModel extends BaseModel {

    public TemplateModel() {
    }

    public TemplateModel(TemplateModel model) {
        putAll(model);
    }

    public Map<String, Object> getConfig() {
        @SuppressWarnings("unchecked")
        Map<String, Object> configMap = (Map<String, Object>) get(ModelAttributes.CONFIG);
        return Collections.unmodifiableMap(configMap);
    }

    public void setConfig(Map<String, Object> configModel) {
        put(ModelAttributes.CONFIG, configModel);
    }

    public DocumentModel getContent() {
        return (DocumentModel) get(ModelAttributes.CONTENT);
    }

    public void setContent(DocumentModel content) {
        put(ModelAttributes.CONTENT, content);
    }

    public DelegatingTemplateEngine getRenderer() {
        return (DelegatingTemplateEngine) get(ModelAttributes.RENDERER);
    }

    public void setRenderer(DelegatingTemplateEngine renderingEngine) {
        put(ModelAttributes.RENDERER, renderingEngine);
    }

    public void setNumberOfPages(int numberOfPages) {
        put(ModelAttributes.NUMBER_OF_PAGES, numberOfPages);
    }

    public void setCurrentPageNuber(int currentPageNumber) {
        put(ModelAttributes.CURRENT_PAGE_NUMBERS, currentPageNumber);
    }

    public void setPreviousFilename(String previousFilename) {
        put(ModelAttributes.PREVIOUS_FILENAME, previousFilename);
    }

    public void setNextFileName(String nextFilename) {
        put(ModelAttributes.NEXT_FILENAME, nextFilename);
    }

    public String getTag() {
        return (String) get(ModelAttributes.TAG);
    }

    public void setTag(String tag) {
        put(ModelAttributes.TAG, tag);
    }

    public void setTaggedPosts(DocumentList<DocumentModel> taggedPosts) {
        put(ModelAttributes.TAGGED_POSTS, taggedPosts);
    }

    public void setTaggedDocuments(DocumentList<DocumentModel> taggedDocuments) {
        put(ModelAttributes.TAGGED_DOCUMENTS, taggedDocuments);
    }

    public void setVersion(String version) {
        put(ModelAttributes.VERSION, version);
    }

    public Writer getWriter() {
        return (Writer) get(ModelAttributes.OUT);
    }
}
