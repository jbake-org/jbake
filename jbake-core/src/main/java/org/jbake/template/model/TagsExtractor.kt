package org.jbake.template.model;

import org.jbake.app.ContentStore;
import org.jbake.app.DocumentList;
import org.jbake.app.FileUtil;
import org.jbake.template.ModelExtractor;

import java.util.Map;

import static org.jbake.app.configuration.PropertyList.OUTPUT_EXTENSION;
import static org.jbake.app.configuration.PropertyList.TAG_PATH;


public class TagsExtractor implements ModelExtractor<DocumentList> {

    @Override
    public DocumentList get(ContentStore db, Map model, String key) {
        DocumentList<TemplateModel> dl = new DocumentList<>();
        TemplateModel templateModel = new TemplateModel();
        templateModel.putAll(model);
        Map<?, ?> config = templateModel.getConfig();

        String tagPath = config.get(TAG_PATH.getKey().replace(".", "_")).toString();

        for (String tag : db.getAllTags()) {
            TemplateModel newTag = new TemplateModel();
            String tagName = tag;
            newTag.setName(tagName);

            String uri = tagPath + FileUtil.URI_SEPARATOR_CHAR + tag + config.get(OUTPUT_EXTENSION.getKey().replace(".", "_")).toString();

            newTag.setUri(uri);
            newTag.setTaggedPosts(db.getPublishedPostsByTag(tagName));
            newTag.setTaggedDocuments(db.getPublishedDocumentsByTag(tagName));
            dl.push(newTag);
        }
        return dl;
    }

}
