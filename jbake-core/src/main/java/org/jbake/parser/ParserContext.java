package org.jbake.parser;

import org.jbake.app.configuration.JBakeConfiguration;
import org.jbake.model.DocumentModel;

import java.io.File;
import java.time.Instant;
import java.util.List;

public class ParserContext {
    private final File file;
    private final List<String> fileLines;
    private final JBakeConfiguration config;
    private final boolean hasHeader;
    private final DocumentModel documentModel;

    public ParserContext(
            File file,
            List<String> fileLines,
            JBakeConfiguration config,
            boolean hasHeader) {
        this.file = file;
        this.fileLines = fileLines;
        this.config = config;
        this.hasHeader = hasHeader;
        this.documentModel = DocumentModel.createDefaultDocumentModel();
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

    public DocumentModel getDocumentModel() {
        return documentModel;
    }

    public boolean hasHeader() {
        return hasHeader;
    }

    // short methods for common use
    public String getBody() {
        return documentModel.getBody();
    }

    public void setBody(String str) {
        documentModel.setBody(str);
    }

    public Instant getDate() {
        return getDocumentModel().getDate();
    }

    public void setDate(Instant date) {
        getDocumentModel().setDate(date);
    }

    public String getStatus() {
        if (getDocumentModel().getStatus() != null) {
            return getDocumentModel().getStatus();
        }
        return "";
    }

    public void setDefaultStatus() {
        getDocumentModel().setStatus(getConfig().getDefaultStatus());
    }

    public String getType() {
        if (getDocumentModel().getType() != null) {
            return getDocumentModel().getType();
        }
        return "";
    }

    public void setDefaultType() {
        getDocumentModel().setType(getConfig().getDefaultType());
    }

    public Object getTags() {
        return getDocumentModel().getTags();
    }

    public void setTags(String[] tags) {
        getDocumentModel().setTags(tags);
    }
}
