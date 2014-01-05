package org.jbake.app;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.jbake.utils.TestUtils;
import org.junit.Assert;
import org.junit.Test;

public class CrawlerTest {
	
	@Test
	public void crawl() throws ConfigurationException, IOException {
		CompositeConfiguration config = TestUtils.loadTestConfig();
		Assert.assertEquals(".html", config.getString("output.extension"));
				
		URL contentUrl = this.getClass().getResource("/");
		File content = new File(contentUrl.getFile());
		Assert.assertTrue(content.exists());
		Crawler crawler = new Crawler(content, config);
		crawler.crawl(new File(content.getPath() + File.separator + "content"));
		
		Assert.assertEquals(2, crawler.getPosts().size());
		Assert.assertEquals(3, crawler.getPages().size());
	}
}
