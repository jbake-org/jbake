package org.jbake.app;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.orientechnologies.orient.core.record.impl.ODocument;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationException;

import org.jbake.app.ConfigUtil.Keys;

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

        Assert.assertEquals(3, crawler.getDocumentCount("post"));
        Assert.assertEquals(3, crawler.getDocumentCount("page"));
        
        List<ODocument> results = db.getPublishedPosts();
//                query(new OSQLSynchQuery<ODocument>("select * from post where status='published' order by date desc"));
        DocumentList list = DocumentList.wrap(results.iterator());
        for (Map<String,Object> content : list) {
        	assertThat(content)
        		.containsKey(Crawler.Attributes.ROOTPATH)
        		.containsValue("../../");
        }
        
        List<ODocument> draftPosts = db.getAllContent("post");
        DocumentList draftList = DocumentList.wrap(draftPosts.iterator());
        for (Map<String,Object> content : list) {
        	if (content.get(Crawler.Attributes.TITLE).equals("Draft Post")) {
        		assertThat(content).containsKey(Crawler.Attributes.DATE);
        	}
        }
        
        // covers bug #213
        List<ODocument> publishedPostsByTag = db.getPublishedPostsByTag("blog");
        Assert.assertEquals(2, publishedPostsByTag.size());
    }
	@Test
	public void renderWithPrettyUrls() throws Exception {
	    Map<String, Object> testProperties = new HashMap<String, Object>();
	    testProperties.put(Keys.URI_NO_EXTENSION, true);
	    testProperties.put(Keys.URI_NO_EXTENSION_PREFIX, "/blog");

	    CompositeConfiguration config = new CompositeConfiguration();
	    config.addConfiguration(new MapConfiguration(testProperties));
	    config.addConfiguration(ConfigUtil.load(new File(this.getClass().getResource("/").getFile())));

	    URL contentUrl = this.getClass().getResource("/");
	    File content = new File(contentUrl.getFile());
	    Crawler crawler = new Crawler(db, content, config);
	    crawler.crawl(new File(content.getPath() + File.separator + "content"));

	    Assert.assertEquals(3, crawler.getDocumentCount("post"));
	    Assert.assertEquals(3, crawler.getDocumentCount("page"));
	    DocumentIterator documents = new DocumentIterator(db.getPublishedPosts().iterator());
	    while (documents.hasNext()) {
	        Map<String, Object> model = documents.next();
	        String noExtensionUri = "blog/\\d{4}/" + FilenameUtils.getBaseName((String) model.get("file")) + "/";

	        Assert.assertThat(model.get("noExtensionUri"), RegexMatcher.matches(noExtensionUri));
	        Assert.assertThat(model.get("uri"), RegexMatcher.matches(noExtensionUri + "index\\.html"));
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
	
	
	private Map<String,Object> getPermalinkPost(String permalinkPattern){
		config.setProperty("permalink", permalinkPattern);
		Crawler crawler = new Crawler(db, sourceFolder, config);
        crawler.crawl(new File(sourceFolder.getPath() + File.separator + config.getString(Keys.CONTENT_FOLDER)));
        
        List<ODocument> results = db.getPublishedPostsByTag("PermalinkTest");
        
        assertThat(results.size()).isEqualTo(1);
        
		DocumentList list = DocumentList.wrap(results.iterator());
		Map<String,Object> content = list.getFirst();
		return content;
	}
	
	@Test
	public void testPermalinkFilePath(){

		Map<String,Object> content = getPermalinkPost("/:filepath");
        
		assertThat(content.get("uri")).isEqualTo("blog/2013/second-post.html");
		assertThat(content.get("uri")).isEqualTo(content.get("permalink"));
		assertThat(content)
		.containsKey(Crawler.Attributes.ROOTPATH)
		.containsValue("../../");
		
	}
	
	@Test
	public void testPermalinkFilePathWithStatic(){
		
		Map<String,Object> content = getPermalinkPost("/:data:/:filepath");
        
		assertThat(content.get("uri")).isEqualTo("data/blog/2013/second-post.html");
		assertThat(content.get("uri")).isEqualTo(content.get("permalink"));
		assertThat(content)
		.containsKey(Crawler.Attributes.ROOTPATH)
		.containsValue("../../../");
	}
	
	@Test
	public void testPermalinkFilename(){
		
		Map<String,Object> content = getPermalinkPost("/:blog:/:filename");
        
		assertThat(content.get("uri")).isEqualTo("blog/second-post.html");
		assertThat(content.get("uri")).isEqualTo(content.get("permalink"));
		assertThat(content)
		.containsKey(Crawler.Attributes.ROOTPATH)
		.containsValue("../");
	}
	
	@Test
	public void testPermalinkWithDate(){
		
		Map<String,Object> content = getPermalinkPost("/:blog:/:YEAR/:MONTH/:DAY/:filename");
        
		assertThat(content.get("uri")).isEqualTo("blog/2013/02/28/second-post.html");
		assertThat(content.get("uri")).isEqualTo(content.get("permalink"));
		assertThat(content)
		.containsKey(Crawler.Attributes.ROOTPATH)
		.containsValue("../../../../");
	}
	
	@Test
	public void testPermalinkWithTagAndTitle(){
		
		Map<String,Object> content = getPermalinkPost("/:data:/:tags/:title");
        
		assertThat(content.get("uri")).isEqualTo("data/blog/PermalinkTest/Second-Post.html");
		assertThat(content.get("uri")).isEqualTo(content.get("permalink"));
		assertThat(content)
		.containsKey(Crawler.Attributes.ROOTPATH)
		.containsValue("../../../");
	}

	
	@Test
	public void testPermalinkWithTagAndTitleWithNoExtension(){
		config.setProperty("uri.noExtension", true);
		Map<String,Object> content = getPermalinkPost("/:data:/:tags/:title");
        
		assertThat(content.get("uri")).isEqualTo("data/blog/PermalinkTest/Second-Post/index.html");
		assertThat(content.get("permalink")).isEqualTo("data/blog/PermalinkTest/Second-Post/");
		assertThat(content)
		.containsKey(Crawler.Attributes.ROOTPATH)
		.containsValue("../../../../");
	}
	
	@Test
	public void testPermalinkWithTypePermalink(){
		config.setProperty("permalink", "/:filepath");
		config.setProperty("permalink.post", "/:blog:/:filename");
		Map<String,Object> content = getPermalinkPost("/:filepath");
        
		assertThat(content.get("uri")).isEqualTo("blog/second-post.html");
		assertThat(content.get("uri")).isEqualTo(content.get("permalink"));
		assertThat(content)
		.containsKey(Crawler.Attributes.ROOTPATH)
		.containsValue("../");
	}
	
	@Test
	public void testPermalinkWithMultipleTypePermalink(){
		config.setProperty("permalink", "/:filepath");
		config.setProperty("permalink.post", "/:blog:/:filename");
		config.setProperty("permalink.page", "/:pages:/:filename");
		Crawler crawler = new Crawler(db, sourceFolder, config);
        crawler.crawl(new File(sourceFolder.getPath() + File.separator + config.getString(Keys.CONTENT_FOLDER)));
        
        List<ODocument> results = db.getPublishedPostsByTag("PermalinkTest");
        
        assertThat(results.size()).isEqualTo(1);
        
		DocumentList list = DocumentList.wrap(results.iterator());
		Map<String,Object> content = list.getFirst();
        
		//Verify that post has used permalink.post pattern.
		assertThat(content.get("uri")).isEqualTo("blog/second-post.html");
		assertThat(content.get("uri")).isEqualTo(content.get("permalink"));
		
		List<ODocument> pageResults = db.getPublishedPages();
          
		DocumentList pages = DocumentList.wrap(pageResults.iterator());
		//Verify that page has used permalink.page pattern.
		Map<String,Object> page = pages.getFirst();
        String url = "pages/"+FilenameUtils.getBaseName((String) page.get("file")) + ".html";
		assertThat(page.get("uri")).isEqualTo(url);
		assertThat(page.get("uri")).isEqualTo(page.get("permalink"));
		
	}
}
