package org.jbake.app;

import static org.junit.Assert.assertThat;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.MapConfiguration;
import org.apache.commons.io.FilenameUtils;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;

public class CrawlerTest {

    private final ODatabaseDocumentTx db = DBUtil.createDB("memory", "documents");

    @After
    public void after() {
        db.drop();
        db.close();
    }

	@Test
	public void crawl() throws ConfigurationException {
		CompositeConfiguration config = ConfigUtil.load(new File(this.getClass().getResource("/").getFile()));
		Assert.assertEquals(".html", config.getString("output.extension"));

		URL contentUrl = this.getClass().getResource("/");
		File content = new File(contentUrl.getFile());
		Assert.assertTrue(content.exists());
		Crawler crawler = new Crawler(db, content, config);
		crawler.crawl(new File(content.getPath() + File.separator + "content"));

		Assert.assertEquals(2, crawler.getPostCount());
		Assert.assertEquals(3, crawler.getPageCount());
    }

	@Test
	public void renderWithPrettyUrls() throws Exception {
	    Map<String, Object> testProperties = new HashMap<String, Object>();
	    testProperties.put(ConfigUtil.URI_NO_EXTENSION, "/blog");

	    CompositeConfiguration config = new CompositeConfiguration();
	    config.addConfiguration(new MapConfiguration(testProperties));
	    config.addConfiguration(ConfigUtil.load(new File(this.getClass().getResource("/").getFile())));

	    URL contentUrl = this.getClass().getResource("/");
	    File content = new File(contentUrl.getFile());
	    Crawler crawler = new Crawler(db, content, config);
	    crawler.crawl(new File(content.getPath() + File.separator + "content"));

	    Assert.assertEquals(2, crawler.getPostCount());
	    Assert.assertEquals(3, crawler.getPageCount());
	    DocumentIterator documents = DBUtil.fetchDocuments(db, "select * from post");
	    while (documents.hasNext()) {
	        Map<String, Object> model = documents.next();
	        String noExtensionUri = "/blog/\\d{4}/" + FilenameUtils.getBaseName((String) model.get("file")) + "/";

	        assertThat(model.get("noExtensionUri"), RegexMatcher.matches(noExtensionUri));
            assertThat(model.get("uri"), RegexMatcher.matches(noExtensionUri + "index\\.html"));
	    }
	}

	private static class RegexMatcher extends BaseMatcher<Object> {
	    private final String regex;

	    public RegexMatcher(String regex){
	        this.regex = regex;
	    }

	    @Override
        public boolean matches(Object o){
	        return ((String)o).matches(regex);

	    }

	    @Override
        public void describeTo(Description description){
	        description.appendText("matches regex: " + regex);
	    }

	    public static RegexMatcher matches(String regex){
	        return new RegexMatcher(regex);
	    }
	}
}
