package org.jbake.app;

import org.apache.commons.io.FilenameUtils;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class CrawlerTest extends ContentStoreIntegrationTest {

    @Test
    public void crawl() {
        Crawler crawler = new Crawler(db, config);
        crawler.crawl();

        Assert.assertEquals(4, db.getDocumentCount("post"));
        Assert.assertEquals(3, db.getDocumentCount("page"));

        DocumentList results = db.getPublishedPosts();

        assertThat(results.size()).isEqualTo(3);

        for (Map<String, Object> content : results) {
            assertThat(content)
                    .containsKey(Crawler.Attributes.ROOTPATH)
                    .containsValue("../../../");
        }

        DocumentList allPosts = db.getAllContent("post");

        assertThat(allPosts.size()).isEqualTo(4);

        for (Map<String, Object> content : allPosts) {
            if (content.get(Crawler.Attributes.TITLE).equals("Draft Post")) {
                assertThat(content).containsKey(Crawler.Attributes.DATE);
            }
        }

        // covers bug #213
        DocumentList publishedPostsByTag = db.getPublishedPostsByTag("blog");
        Assert.assertEquals(3, publishedPostsByTag.size());
    }

    @Test
    public void renderWithPrettyUrls() throws Exception {

        config.setUriWithoutExtension(true);
        config.setPrefixForUriWithoutExtension("/blog");

        Crawler crawler = new Crawler(db, config);
        crawler.crawl();

        Assert.assertEquals(4, db.getDocumentCount("post"));
        Assert.assertEquals(3, db.getDocumentCount("page"));

        DocumentList documents = db.getPublishedPosts();

        for (Map<String, Object> model : documents) {
            String noExtensionUri = "blog/\\d{4}/" + FilenameUtils.getBaseName((String) model.get("file")) + "/";

            Assert.assertThat(model.get("noExtensionUri"), RegexMatcher.matches(noExtensionUri));
            Assert.assertThat(model.get("uri"), RegexMatcher.matches(noExtensionUri + "index\\.html"));

            assertThat(model).containsEntry("rootpath", "../../../");
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
