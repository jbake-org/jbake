package org.jbake.model;

import org.jbake.app.DBUtil;

import java.util.Date;

public class DocumentModel extends BaseModel {

    public static DocumentModel createDefaultDocumentModel() {
        DocumentModel documentModel = new DocumentModel();
        documentModel.setCached(true);
        documentModel.setRendered(false);
        return documentModel;
    }

    public String getBody() {
        return (String) get(ModelAttributes.BODY);
    }

    public void setBody(String body) {
        put(ModelAttributes.BODY, body);
    }

    public Date getDate() {
        return (Date) get(ModelAttributes.DATE);
    }

    public void setDate(Date date) {
        put(ModelAttributes.DATE, date);
    }

    public String getStatus() {
        if (containsKey(ModelAttributes.STATUS)) {
            return (String) get(ModelAttributes.STATUS);
        }
        return "";

    }

    public void setStatus(String status) {
        put(ModelAttributes.STATUS, status);
    }

    public String getType() {
        if (containsKey(ModelAttributes.TYPE)) {
            return (String) get(ModelAttributes.TYPE);
        }
        return "";
    }

    public void setType(String type) {
        put(ModelAttributes.TYPE, type);
    }

    public String[] getTags() {
        return DBUtil.toStringArray(get(ModelAttributes.TAGS));
    }

    public void setTags(String[] tags) {
        put(ModelAttributes.TAGS, tags);
    }

    public String getSha1() {
        return (String) get(ModelAttributes.SHA1);
    }

    public void setSha1(String sha1) {
        put(ModelAttributes.SHA1, sha1);
    }

    public String getSourceuri() {
        return (String) get(ModelAttributes.SOURCE_URI);
    }

    public void setSourceUri(String uri) {
        put(ModelAttributes.SOURCE_URI, uri);
    }

    public String getRootPath() {
        return (String) get(ModelAttributes.ROOTPATH);
    }

    public void setRootPath(String pathToRoot) {
        put(ModelAttributes.ROOTPATH, pathToRoot);
    }

    public boolean getRendered() {
        return (boolean) getOrDefault(ModelAttributes.RENDERED, false);
    }

    public void setRendered(boolean rendered) {
        put(ModelAttributes.RENDERED, rendered);
    }

    public String getFile() {
        return (String) get(ModelAttributes.FILE);
    }

    public void setFile(String path) {
        put(ModelAttributes.FILE, path);
    }

    public String getNoExtensionUri() {
        return (String) get(ModelAttributes.NO_EXTENSION_URI);
    }

    public void setNoExtensionUri(String noExtensionUri) {
        put(ModelAttributes.NO_EXTENSION_URI, noExtensionUri);
    }

    public String getTitle() {
        return (String) get(ModelAttributes.TITLE);
    }

    public void setTitle(String title) {
        put(ModelAttributes.TITLE, title);
    }

    public Boolean getCached() {

        Object value = get(ModelAttributes.CACHED);
        if ( value instanceof String ) {
            return Boolean.valueOf((String) value);
        } else {
            return (Boolean) value;
        }
    }

    public void setCached(boolean cached) {
        put(ModelAttributes.CACHED, cached);
    }

    public void setNextContent(DocumentModel nextDocumentModel) {
        put(ModelAttributes.NEXT_CONTENT, nextDocumentModel);
    }

    public void setPreviousContent(DocumentModel previousDocumentModel) {
        put(ModelAttributes.PREVIOUS_CONTENT, previousDocumentModel);
    }
}
