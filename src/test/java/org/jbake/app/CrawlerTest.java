package org.jbake.app;


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
        db = DBUtil.createDB("memory", "documents"+System.currentTimeMillis());
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
        		.containsKey("rootpath")
        		.containsValue("../../");
        }
        
    }
	
}
