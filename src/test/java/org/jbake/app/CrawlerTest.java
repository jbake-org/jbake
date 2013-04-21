package org.jbake.app;

import java.io.File;
import java.net.URL;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.junit.Assert;
import org.junit.Test;

public class CrawlerTest {

	
	@Test
	public void crawl() throws ConfigurationException {
//		File test = new File("/content");
//		System.out.println(test.exists());
		
		CompositeConfiguration config = new CompositeConfiguration();
		URL defaultConfigFileUrl = this.getClass().getResource("/default.properties");
		File defaultConfigFile = new File(defaultConfigFileUrl.getFile());
		Assert.assertTrue(defaultConfigFile.exists());
		config.addConfiguration(new PropertiesConfiguration(defaultConfigFile));
		
		URL contentUrl = this.getClass().getResource("/");
		File content = new File(contentUrl.getFile());
		Assert.assertTrue(content.exists());
		Crawler crawler = new Crawler(content, config);
		crawler.crawl(new File(content.getPath() + File.separator + "content"));
		
		Assert.assertEquals(crawler.getPosts().size(), 2);
		Assert.assertEquals(crawler.getPages().size(), 3);
	}
}
