package org.jbake.template.model;

import org.jbake.app.ContentStore;
import org.jbake.app.DocumentList;
import org.jbake.app.FileUtil;
import org.jbake.template.ModelExtractor;

import java.util.HashMap;
import java.util.Map;

import static org.jbake.app.configuration.PropertyList.OUTPUT_EXTENSION;
import static org.jbake.app.configuration.PropertyList.TAG_PATH;


public class TagsExtractor implements ModelExtractor<DocumentList> {

    @Override
    public DocumentList get(ContentStore db, Map model, String key) {
        DocumentList dl = new DocumentList();
        Map<?, ?> config = (Map<?, ?>) model.get("config");

        String tagPath = config.get(TAG_PATH.getKey().replace(".", "_")).toString();

        for (String tag : db.getAllTags()) {
            Map<String, Object> newTag = new HashMap<>();
            String tagName = tag;
            newTag.put("name", tagName);

            String uri = tagPath + FileUtil.URI_SEPARATOR_CHAR + tag + config.get(OUTPUT_EXTENSION.getKey().replace(".", "_")).toString();

            newTag.put("uri", uri);
            newTag.put("tagged_posts", db.getPublishedPostsByTag(tagName));
            newTag.put("tagged_documents", db.getPublishedDocumentsByTag(tagName));
            dl.push(newTag);
        }
        return dl;
    }

}
