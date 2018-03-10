package org.jbake.parser;

import org.jbake.app.Crawler;
import org.jbake.app.configuration.JBakeConfiguration;

import java.io.File;
import java.util.List;
import java.util.Map;

public class ParserContext {
    private final File file;
    private final List<String> fileLines;
    private final JBakeConfiguration config;
    private final boolean hasHeader;
    private final Map<String,Object> contents;

    public ParserContext(
            File file,
            List<String> fileLines,
            JBakeConfiguration config,
            boolean hasHeader,
            Map<String, Object> contents) {
        this.file = file;
        this.fileLines = fileLines;
        this.config = config;
        this.hasHeader = hasHeader;
        this.contents = contents;
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
