package org.jbake.app;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.jbake.model.DocumentModel;
import org.jbake.model.DocumentTypes;
import org.jbake.model.ModelAttributes;
import org.jbake.util.DataFileUtil;
import org.junit.jupiter.api.Test;

import com.orientechnologies.orient.core.db.record.OTrackedMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CrawlerTest extends ContentStoreIntegrationTest {

    @Test
    void crawl() {
        Crawler crawler = new Crawler(db, config);
        crawler.crawl();

        assertEquals(4, db.getDocumentCount("post"));
        assertEquals(3, db.getDocumentCount("page"));

        DocumentList<DocumentModel> results = db.getPublishedPosts();

        assertThat(results.size()).isEqualTo(3);

        for (Map<String, Object> content : results) {
            assertThat(content)
                    .containsKey(ModelAttributes.ROOTPATH)
                    .containsValue("../../../");
        }

        DocumentList<DocumentModel> allPosts = db.getAllContent("post");

        assertThat(allPosts.size()).isEqualTo(4);

        for (DocumentModel content : allPosts) {
            if (content.getTitle().equals("Draft Post")) {
                assertThat(content).containsKey(ModelAttributes.DATE);
            }
        }

        // covers bug #213
        DocumentList<DocumentModel> publishedPostsByTag = db.getPublishedPostsByTag("blog");
        assertEquals(3, publishedPostsByTag.size());
    }

    @Test
    void crawlDataFiles() {
        Crawler crawler = new Crawler(db, config);
        // manually register data doctype
        DocumentTypes.addDocumentType(config.getDataFileDocType());
        db.updateSchema();
        crawler.crawlDataFiles();
        assertEquals(2, db.getDocumentCount("data"));

        DataFileUtil dataFileUtil = new DataFileUtil(db, "data");
        Map<String, Object> videos = dataFileUtil.get("videos.yaml");
        assertFalse(videos.isEmpty());
        assertNotNull(videos.get("data"));

        // regression test for issue 747
        Map<String, Object> authorsFileContents = dataFileUtil.get("authors.yaml");
        assertFalse(authorsFileContents.isEmpty());
        Object authorsList = authorsFileContents.get("authors");
        assertThat(authorsList).isNotInstanceOf(OTrackedMap.class);
        assertThat(authorsList).isInstanceOf(HashMap.class);
        HashMap<String, Map<String, Object>> authors = (HashMap<String, Map<String, Object>>) authorsList;
        assertThat(authors.get("Joe Bloggs").get("last_name")).isEqualTo("Bloggs");
    }

    @Test
    void renderWithPrettyUrls() {

        config.setUriWithoutExtension(true);
        config.setPrefixForUriWithoutExtension("/blog");

        Crawler crawler = new Crawler(db, config);
        crawler.crawl();

        assertEquals(4, db.getDocumentCount("post"));
        assertEquals(3, db.getDocumentCount("page"));

        DocumentList<DocumentModel> documents = db.getPublishedPosts();

        for (DocumentModel model : documents) {
            String noExtensionUri = "blog/\\d{4}/" + FilenameUtils.getBaseName(model.getFile()) + "/";

            assertThat(model.getNoExtensionUri()).matches(noExtensionUri);
            assertThat(model.getUri()).matches(noExtensionUri + "index\\.html");
            assertThat(model.getRootPath()).isEqualTo("../../../");
        }
    }
}
