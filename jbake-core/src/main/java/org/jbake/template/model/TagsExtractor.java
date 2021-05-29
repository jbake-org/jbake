package org.jbake.template.model;

import org.jbake.app.ContentStore;
import org.jbake.app.DocumentList;
import org.jbake.app.FileUtil;
import org.jbake.template.ModelExtractor;

import static org.jbake.app.configuration.PropertyList.OUTPUT_EXTENSION;
import static org.jbake.app.configuration.PropertyList.TAG_PATH;


public class TagsExtractor implements ModelExtractor<DocumentList<TemplateModel>> {

    @Override
    public DocumentList<TemplateModel> get(ContentStore db, TemplateModel model, String key) {
        DocumentList<TemplateModel> dl = new DocumentList<>();

        String tagPath = model.getConfig().get(TAG_PATH.getKey().replace(".", "_")).toString();

        for (String tag : db.getAllTags()) {
            TemplateModel newTag = new TemplateModel();
            newTag.setName(tag);

            String uri = tagPath + FileUtil.URI_SEPARATOR_CHAR + tag + model.getConfig().get(OUTPUT_EXTENSION.getKey().replace(".", "_")).toString();

            newTag.setUri(uri);
            newTag.setTaggedPosts(db.getPublishedPostsByTag(tag));
            newTag.setTaggedDocuments(db.getPublishedDocumentsByTag(tag));
            dl.push(newTag);
        }
        return dl;
    }

}
