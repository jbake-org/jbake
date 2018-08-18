package org.jbake.template.model;

import org.jbake.app.DocumentList;
import org.jbake.model.BaseModel;
import org.jbake.model.DocumentModel;
import org.jbake.model.ModelAttributes;
import org.jbake.template.DelegatingTemplateEngine;

import java.util.Map;

public class TemplateModel extends BaseModel {

    public Map<String, Object> getConfig() {
        return (Map<String, Object>) get(ModelAttributes.CONFIG.toString());
    }

    public void setConfig(Map<String, Object> configModel) {
        put(ModelAttributes.CONFIG.toString(), configModel);
    }

    public void setContent(DocumentModel content) {
        put(ModelAttributes.CONTENT.toString(), content);
    }

    public DocumentModel getContent() {
        return (DocumentModel) get(ModelAttributes.CONTENT.toString());
    }

    public void setRenderer(DelegatingTemplateEngine renderingEngine) {
        put(ModelAttributes.RENDERER.toString(), renderingEngine);
    }

    public void setNumberOfPages(int numberOfPages) {
        put(ModelAttributes.NUMBER_OF_PAGES.toString(), numberOfPages);
    }

    public void setCurrentPageNuber(int currentPageNumber) {
        put(ModelAttributes.CURRENT_PAGE_NUMBERS.toString(), currentPageNumber);
    }

    public void setPreviousFilename(String previousFilename) {
        put(ModelAttributes.PREVIOUS_FILENAME.toString(), previousFilename);
    }

    public void setNextFileName(String nextFilename) {
        put(ModelAttributes.NEXT_FILENAME.toString(), nextFilename);
    }

    public void setTag(String tag) {
        put(ModelAttributes.TAG.toString(), tag);
    }

    public void setTaggedPosts(DocumentList taggedPosts) {
        put(ModelAttributes.TAGGED_POSTS.toString(), taggedPosts);
    }

    public void setTaggedDocuments(DocumentList taggedDocuments) {
        put(ModelAttributes.TAGGED_DOCUMENTS.toString(), taggedDocuments);
    }

    public void setVersion(String version) {
        put(ModelAttributes.VERSION.toString(), version);
    }
}
