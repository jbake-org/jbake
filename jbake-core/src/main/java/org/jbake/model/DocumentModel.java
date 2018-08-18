package org.jbake.model;

import java.util.Date;
import java.util.HashMap;

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
        return (String[]) get(DocumentAttributes.TAGS.toString());
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
}
