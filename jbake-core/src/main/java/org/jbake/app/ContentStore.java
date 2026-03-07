/*
 * The MIT License
 *
 * Copyright 2015 jdlee.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.jbake.app;

import org.jbake.model.DocumentModel;
import org.jbake.model.DocumentTypes;
import org.jbake.model.ModelAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * In-memory content store backed by a simple list of DocumentModel objects.
 *
 * @author jdlee
 */
public class ContentStore {

    private final Logger logger = LoggerFactory.getLogger(ContentStore.class);

    private final List<DocumentModel> documents = new ArrayList<>();
    private String templatesSignature;

    private long start = -1;
    private long limit = -1;

    public ContentStore() {
    }

    /**
     * @deprecated No longer needed. Kept for API compatibility.
     */
    @Deprecated
    public ContentStore(final String type, String name) {
    }

    public void startup() {
        // no-op: in-memory store is always ready
    }

    public long getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public long getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public void resetPagination() {
        this.start = -1;
        this.limit = -1;
    }

    public void updateSchema() {
        // no-op: no schema to manage
    }

    public void close() {
        // no-op
    }

    public void shutdown() {
        // no-op
    }

    public void drop() {
        documents.clear();
        templatesSignature = null;
    }

    public long getDocumentCount(String docType) {
        return documents.stream()
                .filter(d -> docType.equals(d.getType()))
                .count();
    }

    public long getPublishedCount(String docType) {
        return documents.stream()
                .filter(d -> "published".equals(d.getStatus()))
                .filter(d -> docType.equals(d.getType()))
                .count();
    }

    public DocumentList<DocumentModel> getDocumentByUri(String uri) {
        return toDocumentList(
                documents.stream()
                        .filter(d -> uri.equals(d.getSourceuri()))
        );
    }

    public DocumentList<DocumentModel> getDocumentStatus(String uri) {
        DocumentList<DocumentModel> result = new DocumentList<>();
        for (DocumentModel doc : documents) {
            if (uri.equals(doc.getSourceuri())) {
                // Return only sha1 and rendered fields, matching the original SQL:
                // "select sha1,rendered from Documents where sourceuri=?"
                DocumentModel projection = new DocumentModel();
                projection.setSha1(doc.getSha1());
                projection.setRendered(doc.getRendered());
                result.add(projection);
            }
        }
        return result;
    }

    public DocumentList<DocumentModel> getPublishedPosts() {
        return getPublishedContent("post");
    }

    public DocumentList<DocumentModel> getPublishedPosts(boolean applyPaging) {
        return getPublishedContent("post", applyPaging);
    }

    public DocumentList<DocumentModel> getPublishedPostsByTag(String tag) {
        return toDocumentList(
                documents.stream()
                        .filter(d -> "published".equals(d.getStatus()))
                        .filter(d -> "post".equals(d.getType()))
                        .filter(d -> hasTag(d, tag))
                        .sorted(byDateDesc())
        );
    }

    public DocumentList<DocumentModel> getPublishedDocumentsByTag(String tag) {
        DocumentList<DocumentModel> result = new DocumentList<>();
        for (String docType : DocumentTypes.getDocumentTypes()) {
            documents.stream()
                    .filter(d -> "published".equals(d.getStatus()))
                    .filter(d -> docType.equals(d.getType()))
                    .filter(d -> hasTag(d, tag))
                    .sorted(byDateDesc())
                    .forEach(result::add);
        }
        return result;
    }

    public DocumentList<DocumentModel> getPublishedPages() {
        return getPublishedContent("page");
    }

    public DocumentList<DocumentModel> getPublishedContent(String docType) {
        return getPublishedContent(docType, false);
    }

    private DocumentList<DocumentModel> getPublishedContent(String docType, boolean applyPaging) {
        Stream<DocumentModel> stream = documents.stream()
                .filter(d -> "published".equals(d.getStatus()))
                .filter(d -> docType.equals(d.getType()))
                .sorted(byDateDesc());

        if (applyPaging && hasStartAndLimitBoundary()) {
            stream = stream.skip(start).limit(limit);
        }
        return toDocumentList(stream);
    }

    public DocumentList<DocumentModel> getAllContent(String docType) {
        return getAllContent(docType, false);
    }

    public DocumentList<DocumentModel> getAllContent(String docType, boolean applyPaging) {
        Stream<DocumentModel> stream = documents.stream()
                .filter(d -> docType.equals(d.getType()))
                .sorted(byDateDesc());

        if (applyPaging && hasStartAndLimitBoundary()) {
            stream = stream.skip(start).limit(limit);
        }
        return toDocumentList(stream);
    }

    private boolean hasStartAndLimitBoundary() {
        return (start >= 0) && (limit > -1);
    }

    private DocumentList<DocumentModel> getAllTagsFromPublishedPosts() {
        return toDocumentList(
                documents.stream()
                        .filter(d -> "published".equals(d.getStatus()))
                        .filter(d -> "post".equals(d.getType()))
        );
    }

    public DocumentList<DocumentModel> getUnrenderedContent() {
        return toDocumentList(
                documents.stream()
                        .filter(d -> !d.getRendered())
                        .sorted(byDateDesc())
        );
    }

    public void deleteContent(String uri) {
        documents.removeIf(d -> uri.equals(d.getSourceuri()));
    }

    public void markContentAsRendered(DocumentModel document) {
        documents.stream()
                .filter(d -> !d.getRendered())
                .filter(d -> document.getType().equals(d.getType()))
                .filter(d -> document.getSourceuri().equals(d.getSourceuri()))
                .filter(d -> Boolean.TRUE.equals(d.getCached()))
                .forEach(d -> d.setRendered(true));
    }

    public void deleteAllByDocType(String docType) {
        documents.removeIf(d -> docType.equals(d.getType()));
    }

    public Set<String> getTags() {
        DocumentList<DocumentModel> docs = this.getAllTagsFromPublishedPosts();
        Set<String> result = new HashSet<>();
        for (DocumentModel document : docs) {
            String[] tags = document.getTags();
            Collections.addAll(result, tags);
        }
        return result;
    }

    public Set<String> getAllTags() {
        Set<String> result = new HashSet<>();
        for (String docType : DocumentTypes.getDocumentTypes()) {
            documents.stream()
                    .filter(d -> "published".equals(d.getStatus()))
                    .filter(d -> docType.equals(d.getType()))
                    .forEach(d -> {
                        String[] tags = d.getTags();
                        Collections.addAll(result, tags);
                    });
        }
        return result;
    }

    public void updateAndClearCacheIfNeeded(boolean needed, File templateFolder) {
        boolean clearCache = needed;

        if (!needed) {
            clearCache = updateTemplateSignatureIfChanged(templateFolder);
        }

        if (clearCache) {
            deleteAllDocumentTypes();
        }
    }

    private boolean updateTemplateSignatureIfChanged(File templateFolder) {
        String currentTemplatesSignature;
        try {
            currentTemplatesSignature = FileUtil.sha1(templateFolder);
        } catch (Exception e) {
            currentTemplatesSignature = "";
        }

        if (templatesSignature != null) {
            if (!templatesSignature.equals(currentTemplatesSignature)) {
                templatesSignature = currentTemplatesSignature;
                return true;
            }
        } else {
            // first computation of templates signature
            templatesSignature = currentTemplatesSignature;
            return true;
        }
        return false;
    }

    private void deleteAllDocumentTypes() {
        for (String docType : DocumentTypes.getDocumentTypes()) {
            try {
                this.deleteAllByDocType(docType);
            } catch (Exception e) {
                // maybe a non existing document type
            }
        }
    }

    public boolean isActive() {
        return true;
    }

    public void addDocument(DocumentModel document) {
        documents.add(document);
    }

    // ---- helpers ----

    private static boolean hasTag(DocumentModel doc, String tag) {
        String[] tags = doc.getTags();
        if (tags == null) {
            return false;
        }
        return Arrays.asList(tags).contains(tag);
    }

    private static Comparator<DocumentModel> byDateDesc() {
        return (a, b) -> {
            if (a.getDate() == null && b.getDate() == null) return 0;
            if (a.getDate() == null) return 1;
            if (b.getDate() == null) return -1;
            return b.getDate().compareTo(a.getDate());
        };
    }

    private static DocumentList<DocumentModel> toDocumentList(Stream<DocumentModel> stream) {
        DocumentList<DocumentModel> list = new DocumentList<>();
        stream.forEach(list::add);
        return list;
    }
}
