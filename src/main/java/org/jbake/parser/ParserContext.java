/*
 * Copyright 2003-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jbake.parser;

import org.apache.commons.configuration.CompositeConfiguration;

import java.io.File;
import java.util.List;
import java.util.Map;

public class ParserContext {
    private final File file;
    private final List<String> fileLines;
    private final CompositeConfiguration config;
    private final String contentPath;
    private final boolean hasHeader;
    private final Map<String,Object> contents;

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
        this.contents = contents;
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

    public Map<String, Object> getContents() {
        return contents;
    }

    public boolean hasHeader() {
        return hasHeader;
    }

    // short methods for common use
    public String getBody() {
        return contents.get("body").toString();
    }

    public void setBody(String str) {
        contents.put("body", str);
    }
}
