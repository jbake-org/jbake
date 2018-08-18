package org.jbake.model;

import org.jbake.app.Crawler;
import org.jbake.app.DBUtil;
import org.jbake.app.DocumentList;
import org.jbake.template.DelegatingTemplateEngine;

import javax.swing.text.Document;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DocumentModel extends HashMap<String, Object> {


    public DocumentModel() {
        super();
    }

    public String getBody() {
        return get(DocumentAttributes.BODY.toString()).toString();
    }

    public void setBody(String body) {
        put(DocumentAttributes.BODY.toString(), body);
    }

    public Date getDate() {
        return (Date) get(DocumentAttributes.DATE.toString());
    }

    public void setDate(Date date) {
        put(DocumentAttributes.DATE.toString(), date);
    }

    public String getStatus() {
        if (containsKey(DocumentAttributes.STATUS.toString())) {
            return get(DocumentAttributes.STATUS.toString()).toString();
        }
        return "";

    }

    public void setStatus(String status) {
        put(DocumentAttributes.STATUS.toString(), status);
    }

    public String getType() {
        if (containsKey(DocumentAttributes.TYPE.toString())) {
            return get(DocumentAttributes.TYPE.toString()).toString();
        }
        return "";
    }

    public void setType(String type) {
        put(DocumentAttributes.TYPE.toString(), type);
    }

    public String[] getTags() {
        return DBUtil.toStringArray(get(DocumentAttributes.TAGS.toString()));
    }

    public void setTags(String[] tags) {
        put(DocumentAttributes.TAGS.toString(), tags);
    }

    public String getSha1() {
        return (String) get(DocumentAttributes.SHA1.toString());
    }

    public void setSha1(String sha1) {
        put(DocumentAttributes.SHA1.toString(), sha1);
    }

    public String getUri() {
        return (String) get(DocumentAttributes.URI.toString());
    }

    public void setUri(String uri) {
        put(DocumentAttributes.URI.toString(), uri);
    }

    public void setSourceUri(String uri) {
        put(DocumentAttributes.SOURCE_URI.toString(), uri);
    }

    public void setRootPath(String pathToRoot) {
        put(DocumentAttributes.ROOTPATH.toString(), pathToRoot);
    }

    public Boolean getRendered() {
        return (Boolean) get(DocumentAttributes.RENDERED.toString());
    }

    public void setRendered(boolean rendered) {
        put(DocumentAttributes.RENDERED.toString(), rendered);
    }

    public void setFile(String path) {
        put(DocumentAttributes.FILE.toString(), path);
    }

    public String getNoExtensionUri() {
        return (String) get(DocumentAttributes.NO_EXTENSION_URI.toString());
    }

    public void setNoExtensionUri(String noExtensionUri) {
        put(DocumentAttributes.NO_EXTENSION_URI.toString(), noExtensionUri);
    }

    public String getTitle() {
        return (String) get(DocumentAttributes.TITLE.toString());
    }

    public void setTitle(String title) {
        put(DocumentAttributes.TITLE.toString(), title);
    }

    public void setName(String name) {
        put(DocumentAttributes.NAME.toString(), name);
    }

    public void setCached(boolean cached) {
        put(DocumentAttributes.CACHED.toString(), cached);
    }

    public boolean getCached() {
        return (boolean) get(DocumentAttributes.CACHED.toString());
    }

    public void setTaggedPosts(DocumentList taggedPosts) {
        put(DocumentAttributes.TAGGED_POSTS.toString(), taggedPosts);
    }

    public void setTaggedDocuments(DocumentList taggedDocuments) {
        put(DocumentAttributes.TAGGED_DOCUMENTS.toString(), taggedDocuments);
    }

    public void setNextContent(DocumentModel nextDocumentModel) {
        put(DocumentAttributes.NEXT_CONTENT.toString(), nextDocumentModel);
    }

    public void setPreviousContent(DocumentModel previousDocumentModel) {
        put(DocumentAttributes.PREVIOUS_CONTENT.toString(), previousDocumentModel);
    }

    public Map<String, Object> getConfig() {
        return (Map<String, Object>) get(DocumentAttributes.CONFIG.toString());
    }

    public void setConfig(Map<String, Object> configModel) {
        put(DocumentAttributes.CONFIG.toString(), configModel);
    }

    public void setContent(DocumentModel content) {
        put(DocumentAttributes.CONTENT.toString(), content);
    }

    public DocumentModel getContent() {
        return (DocumentModel) get(DocumentAttributes.CONTENT.toString());
    }

    public void setRenderer(DelegatingTemplateEngine renderingEngine) {
        put(DocumentAttributes.RENDERER.toString(), renderingEngine);
    }

    public void setNumberOfPages(int numberOfPages) {
        put(DocumentAttributes.NUMBER_OF_PAGES.toString(), numberOfPages);
    }

    public void setCurrentPageNuber(int currentPageNumber) {
        put(DocumentAttributes.CURRENT_PAGE_NUMBERS.toString(), currentPageNumber);
    }

    public void setPreviousFilename(String previousFilename) {
        put(DocumentAttributes.PREVIOUS_FILENAME.toString(), previousFilename);
    }

    public void setNextFileName(String nextFilename) {
        put(DocumentAttributes.NEXT_FILENAME.toString(), nextFilename);
    }

    public void setTag(String tag) {
        put(DocumentAttributes.TAG.toString(), tag);
    }
}
