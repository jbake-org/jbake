package org.jbake.app;

import org.apache.commons.io.FilenameUtils;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.jbake.model.DocumentModel;
import org.jbake.model.ModelAttributes;
import org.jbake.model.DocumentTypes;
import org.jbake.util.DataFileUtil;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;

public class CrawlerTest extends ContentStoreIntegrationTest {

    @Test
    public void crawl() throws InterruptedException {
        Crawler crawler = new Crawler(db, config);
        crawler.crawl();

        Assert.assertEquals(4, db.getDocumentCount("post"));
        Assert.assertEquals(3, db.getDocumentCount("page"));

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
        Assert.assertEquals(3, publishedPostsByTag.size());
    }

    @Test
    public void crawlDataFiles() throws InterruptedException {
        Crawler crawler = new Crawler(db, config);
        // manually register data doctype
        DocumentTypes.addDocumentType(config.getDataFileDocType());
        db.updateSchema();
        crawler.crawlDataFiles();
        crawler.shutdown();
        Assert.assertEquals(1, db.getDocumentCount("data"));

        DataFileUtil util = new DataFileUtil(db, "data");
        Map<String, Object> data = util.get("videos.yaml");
        Assert.assertFalse(data.isEmpty());
        Assert.assertNotNull(data.get("data"));
    }

    @Test
    public void renderWithPrettyUrls() throws InterruptedException {

        config.setUriWithoutExtension(true);
        config.setPrefixForUriWithoutExtension("/blog");

        Crawler crawler = new Crawler(db, config);
        crawler.crawl();

        Assert.assertEquals(4, db.getDocumentCount("post"));
        Assert.assertEquals(3, db.getDocumentCount("page"));

        DocumentList<DocumentModel> documents = db.getPublishedPosts();

        for (DocumentModel model : documents) {
            String noExtensionUri = "blog/\\d{4}/" + FilenameUtils.getBaseName(model.getFile()) + "/";

            Assert.assertThat(model.getNoExtensionUri(), RegexMatcher.matches(noExtensionUri));
            Assert.assertThat(model.getUri(), RegexMatcher.matches(noExtensionUri + "index\\.html"));
            Assert.assertThat(model.getRootPath(), is("../../../"));
        }
    }

    private static class RegexMatcher extends BaseMatcher<Object> {
        private final String regex;

        public RegexMatcher(String regex) {
            this.regex = regex;
        }

        public static RegexMatcher matches(String regex) {
            return new RegexMatcher(regex);
        }

        @Override
        public boolean matches(Object o) {
            return ((String) o).matches(regex);

        }

        @Override
        public void describeTo(Description description) {
            description.appendText("matches regex: " + regex);
        }
    }
}
