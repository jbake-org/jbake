package org.jbake.parser;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.jbake.app.Crawler;

public class ParserContext {
    private final File file;
    private final List<String> fileLines;
    private final Configuration config;
    private final String contentPath;
    private final boolean hasHeader;
    private final Map<String,Object> contents;

    public ParserContext(
            File file,
            List<String> fileLines,
            Configuration config,
            String contentPath,
            boolean hasHeader,
            Map<String, Object> contents) {
        this.file = file;
        this.fileLines = fileLines;
        this.config = config;
        this.contentPath = contentPath;
        this.hasHeader = hasHeader;
        this.contents = contents;
    }

    public File getFile() {
        return file;
    }

    public List<String> getFileLines() {
        return fileLines;
    }

    public Configuration getConfig() {
        return config;
    }

    public String getContentPath() {
        return contentPath;
    }

    public Map<String, Object> getContents() {
        return contents;
    }

    public boolean hasHeader() {
        return hasHeader;
    }

    // short methods for common use
    public String getBody() {
        return contents.get(Crawler.Attributes.BODY).toString();
    }

    public void setBody(String str) {
        contents.put(Crawler.Attributes.BODY, str);
    }
}
