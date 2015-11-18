package org.jbake.app;

import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.jbake.app.ConfigUtil.Keys;
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
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

public class CrawlerTest {
    private CompositeConfiguration config;
    private ContentStore db;
    private File sourceFolder;
	
	@Before
    public void setup() throws Exception, IOException, URISyntaxException {
        URL sourceUrl = this.getClass().getResource("/");

        sourceFolder = new File(sourceUrl.getFile());
        if (!sourceFolder.exists()) {
            throw new Exception("Cannot find sample data structure!");
        }

        config = ConfigUtil.load(new File(this.getClass().getResource("/").getFile()));
        Assert.assertEquals(".html", config.getString(Keys.OUTPUT_EXTENSION));
        db = DBUtil.createDataStore("memory", "documents"+System.currentTimeMillis());
    }

    @After
    public void cleanup() throws InterruptedException {
        db.drop();
        db.close();
    }
	@Test
	public void crawl() throws ConfigurationException {
        Crawler crawler = new Crawler(db, sourceFolder, config);
        crawler.crawl(new File(sourceFolder.getPath() + File.separator + config.getString(Keys.CONTENT_FOLDER)));

        Assert.assertEquals(2, crawler.getDocumentCount("post"));
        Assert.assertEquals(3, crawler.getDocumentCount("page"));
        
        List<ODocument> results = db.getPublishedPosts();
//                query(new OSQLSynchQuery<ODocument>("select * from post where status='published' order by date desc"));
        DocumentList list = DocumentList.wrap(results.iterator());
        for (Map<String,Object> content : list) {
        	assertThat(content)
        		.containsKey(Crawler.Attributes.ROOTPATH)
        		.containsValue("../../");
        }
        
    }
	@Test
	public void renderWithPrettyUrls() throws Exception {
	    Map<String, Object> testProperties = new HashMap<String, Object>();
	    testProperties.put(Keys.URI_NO_EXTENSION, "/blog");

	    CompositeConfiguration config = new CompositeConfiguration();
	    config.addConfiguration(new MapConfiguration(testProperties));
	    config.addConfiguration(ConfigUtil.load(new File(this.getClass().getResource("/").getFile())));

	    URL contentUrl = this.getClass().getResource("/");
	    File content = new File(contentUrl.getFile());
	    Crawler crawler = new Crawler(db, content, config);
	    crawler.crawl(new File(content.getPath() + File.separator + "content"));

	    Assert.assertEquals(2, crawler.getDocumentCount("post"));
	    Assert.assertEquals(3, crawler.getDocumentCount("page"));
	    DocumentIterator documents = new DocumentIterator(db.getPublishedPosts().iterator());
	    while (documents.hasNext()) {
	        Map<String, Object> model = documents.next();
	        String noExtensionUri = "blog/\\d{4}/" + FilenameUtils.getBaseName((String) model.get("file")) + "/";

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
