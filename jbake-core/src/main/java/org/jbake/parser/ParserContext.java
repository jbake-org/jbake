package org.jbake.parser;

import org.jbake.app.Crawler;
import org.jbake.app.configuration.JBakeConfiguration;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParserContext {
    private final File file;
    private final List<String> fileLines;
    private final JBakeConfiguration config;
    private final boolean hasHeader;
    private final Map<String,Object> documentModel;

    public ParserContext(
            File file,
            List<String> fileLines,
            JBakeConfiguration config,
            boolean hasHeader) {
        this.file = file;
        this.fileLines = fileLines;
        this.config = config;
        this.hasHeader = hasHeader;
        this.documentModel = new HashMap<>();
    }

    public File getFile() {
        return file;
    }

    public List<String> getFileLines() {
        return fileLines;
    }

    public JBakeConfiguration getConfig() {
        return config;
    }

    public Map<String, Object> getDocumentModel() {
        return documentModel;
    }

    public boolean hasHeader() {
        return hasHeader;
    }

    // short methods for common use
    public String getBody() {
        return documentModel.get(Crawler.Attributes.BODY).toString();
    }

    public void setBody(String str) {
        documentModel.put(Crawler.Attributes.BODY, str);
    }

    public Object getDate() {
        return getDocumentModel().get(Crawler.Attributes.DATE);
    }

    public void setDate(Date date) {
        getDocumentModel().put(Crawler.Attributes.DATE, date);
    }

    public String getStatus() {
        if (getDocumentModel().containsKey(Crawler.Attributes.STATUS)) {
            return getDocumentModel().get(Crawler.Attributes.STATUS).toString();
        }
        return "";
    }

    public void setDefaultStatus() {
        getDocumentModel().put(Crawler.Attributes.STATUS, getConfig().getDefaultStatus());
    }

    public String getType() {
        if (getDocumentModel().containsKey(Crawler.Attributes.TYPE)) {
            return getDocumentModel().get(Crawler.Attributes.TYPE).toString();
        }
        return "";
    }

    public void setDefaultType() {
        getDocumentModel().put(Crawler.Attributes.TYPE, getConfig().getDefaultType());
    }

    public Object getTags() {
        return getDocumentModel().get(Crawler.Attributes.TAGS);
    }

    public void setTags(String[] tags) {
        getDocumentModel().put(Crawler.Attributes.TAGS, tags);
    }
}
