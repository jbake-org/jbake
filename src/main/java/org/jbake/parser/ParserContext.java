package org.jbake.parser;

import static org.jbake.app.ContentTag.*;

import org.apache.commons.configuration.CompositeConfiguration;
import org.jbake.app.Content;

import java.io.File;
import java.util.List;
import java.util.Map;

public class ParserContext {
    private final File file;
    private final List<String> fileLines;
    private final CompositeConfiguration config;
    private final String contentPath;
    private final boolean hasHeader;
    private final Content contents;

    public ParserContext(
            File file,
            List<String> fileLines,
            CompositeConfiguration config,
            String contentPath,
            boolean hasHeader,
            Content contents) {
        this.file = file;
        this.fileLines = fileLines;
        this.config = config;
        this.contentPath = contentPath;
        this.hasHeader = hasHeader;
        this.contents = contents;
    }

    /**
     * Kept to prevent API break.
     */
    @Deprecated
    public ParserContext(
            File file,
            List<String> fileLines,
            CompositeConfiguration config,
            String contentPath,
            boolean hasHeader,
            Map<String, Object> contents) {
        this.file = file;
        this.fileLines = fileLines;
        this.config = config;
        this.contentPath = contentPath;
        this.hasHeader = hasHeader;
        this.contents = new Content(contents);
    }

    public File getFile() {
        return file;
    }

    public List<String> getFileLines() {
        return fileLines;
    }

    public CompositeConfiguration getConfig() {
        return config;
    }

    public String getContentPath() {
        return contentPath;
    }

    @Deprecated
    public Map<String, Object> getContents() {
        return contents.getContentAsMap();
    }

    public Content getContent() {
        return contents;
    }

    public boolean hasHeader() {
        return hasHeader;
    }

    // short methods for common use
    public String getBody() {
        return contents.getString(body, null);
    }

    public void setBody(String str) {
        contents.put(body, str);
    }
}
