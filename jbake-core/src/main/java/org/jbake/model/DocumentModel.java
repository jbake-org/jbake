package org.jbake.model;

import org.jbake.app.DBUtil;
import org.jbake.app.DocumentList;

import java.util.Date;

public class DocumentModel extends BaseModel {

    public String getBody() {
        return get(ModelAttributes.BODY.toString()).toString();
    }

    public void setBody(String body) {
        put(ModelAttributes.BODY.toString(), body);
    }

    public Date getDate() {
        return (Date) get(ModelAttributes.DATE.toString());
    }

    public void setDate(Date date) {
        put(ModelAttributes.DATE.toString(), date);
    }

    public String getStatus() {
        if (containsKey(ModelAttributes.STATUS.toString())) {
            return get(ModelAttributes.STATUS.toString()).toString();
        }
        return "";

    }

    public void setStatus(String status) {
        put(ModelAttributes.STATUS.toString(), status);
    }

    public String getType() {
        if (containsKey(ModelAttributes.TYPE.toString())) {
            return get(ModelAttributes.TYPE.toString()).toString();
        }
        return "";
    }

    public void setType(String type) {
        put(ModelAttributes.TYPE.toString(), type);
    }

    public String[] getTags() {
        return DBUtil.toStringArray(get(ModelAttributes.TAGS.toString()));
    }

    public void setTags(String[] tags) {
        put(ModelAttributes.TAGS.toString(), tags);
    }

    public String getSha1() {
        return (String) get(ModelAttributes.SHA1.toString());
    }

    public void setSha1(String sha1) {
        put(ModelAttributes.SHA1.toString(), sha1);
    }

    public void setSourceUri(String uri) {
        put(ModelAttributes.SOURCE_URI.toString(), uri);
    }

    public void setRootPath(String pathToRoot) {
        put(ModelAttributes.ROOTPATH.toString(), pathToRoot);
    }

    public Boolean getRendered() {
        return (Boolean) get(ModelAttributes.RENDERED.toString());
    }

    public void setRendered(boolean rendered) {
        put(ModelAttributes.RENDERED.toString(), rendered);
    }

    public void setFile(String path) {
        put(ModelAttributes.FILE.toString(), path);
    }

    public String getNoExtensionUri() {
        return (String) get(ModelAttributes.NO_EXTENSION_URI.toString());
    }

    public void setNoExtensionUri(String noExtensionUri) {
        put(ModelAttributes.NO_EXTENSION_URI.toString(), noExtensionUri);
    }

    public String getTitle() {
        return (String) get(ModelAttributes.TITLE.toString());
    }

    public void setTitle(String title) {
        put(ModelAttributes.TITLE.toString(), title);
    }

    public void setCached(boolean cached) {
        put(ModelAttributes.CACHED.toString(), cached);
    }

    public void setNextContent(DocumentModel nextDocumentModel) {
        put(ModelAttributes.NEXT_CONTENT.toString(), nextDocumentModel);
    }

    public void setPreviousContent(DocumentModel previousDocumentModel) {
        put(ModelAttributes.PREVIOUS_CONTENT.toString(), previousDocumentModel);
    }
}
