package org.jbake.util;

import static org.assertj.core.api.Assertions.*;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration.CompositeConfiguration;
import org.jbake.app.ConfigUtil;
import org.jbake.app.Crawler.Attributes;
import org.jbake.util.HtmlUtil;
import org.junit.Before;
import org.junit.Test;



public class HtmlUtilTest {
	
	private CompositeConfiguration config;
	
	@Before
	public void setUp() throws Exception{
		config = ConfigUtil.load(new File(this.getClass().getResource("/fixture").getFile()));
	}
	
	@Test
	public void shouldNotAddBodyHTMLElement(){
		Map<String, Object> fileContent = new HashMap<String, Object>();
		fileContent.put(Attributes.ROOTPATH, "../../../");
		fileContent.put(Attributes.URI, "blog/2017/05/first_post.html");
		fileContent.put(Attributes.BODY, "<div> Test <img src='blog/2017/05/first.jpg' /></div>");
		
		HtmlUtil.fixImageSourceUrls(fileContent, config);
		
		String body = fileContent.get(Attributes.BODY).toString();
		
		assertThat(body).doesNotContain("<body>");
		assertThat(body).doesNotContain("</body>");
		
	}

	@Test
	public void shouldAddRootpath(){
		Map<String, Object> fileContent = new HashMap<String, Object>();
		fileContent.put(Attributes.ROOTPATH, "../../../");
		fileContent.put(Attributes.URI, "blog/2017/05/first_post.html");
		fileContent.put(Attributes.BODY, "<div> Test <img src='blog/2017/05/first.jpg' /></div>");
		
		HtmlUtil.fixImageSourceUrls(fileContent, config);
		
		String body = fileContent.get(Attributes.BODY).toString();
		
		assertThat(body).contains("src=\"http://www.jbake.org/blog/2017/05/first.jpg\"");
		
	}
	
	@Test
	public void shouldAddContentpath(){
		Map<String, Object> fileContent = new HashMap<String, Object>();
		fileContent.put(Attributes.ROOTPATH, "../../../");
		fileContent.put(Attributes.URI, "blog/2017/05/first_post.html");
		fileContent.put(Attributes.BODY, "<div> Test <img src='./first.jpg' /></div>");
		
		HtmlUtil.fixImageSourceUrls(fileContent, config);
		
		String body = fileContent.get(Attributes.BODY).toString();
		
		assertThat(body).contains("src=\"http://www.jbake.org/blog/2017/05/first.jpg\"");
		
	}
	
	@Test
	public void shouldNotAddRootPath(){
		Map<String, Object> fileContent = new HashMap<String, Object>();
		fileContent.put(Attributes.ROOTPATH, "../../../");
		fileContent.put(Attributes.URI, "blog/2017/05/first_post.html");
		fileContent.put(Attributes.BODY, "<div> Test <img src='/blog/2017/05/first.jpg' /></div>");
		
		HtmlUtil.fixImageSourceUrls(fileContent,config);
		
		String body = fileContent.get(Attributes.BODY).toString();
		
		assertThat(body).contains("src=\"http://www.jbake.org/blog/2017/05/first.jpg\"");
		
	}
	
	@Test
	public void shouldAddRootPathForNoExtension(){
		Map<String, Object> fileContent = new HashMap<String, Object>();
		fileContent.put(Attributes.ROOTPATH, "../../../");
		fileContent.put(Attributes.URI, "blog/2017/05/first_post.html");
		fileContent.put(Attributes.NO_EXTENSION_URI, "blog/2017/05/first_post/");
		fileContent.put(Attributes.BODY, "<div> Test <img src='blog/2017/05/first.jpg' /></div>");
		
		HtmlUtil.fixImageSourceUrls(fileContent, config);
		
		String body = fileContent.get(Attributes.BODY).toString();
		
		assertThat(body).contains("src=\"http://www.jbake.org/blog/2017/05/first.jpg\"");

	}
	
	@Test
	public void shouldAddContentPathForNoExtension(){
		Map<String, Object> fileContent = new HashMap<String, Object>();
		fileContent.put(Attributes.ROOTPATH, "../../../");
		fileContent.put(Attributes.URI, "blog/2017/05/first_post.html");
		fileContent.put(Attributes.NO_EXTENSION_URI, "blog/2017/05/first_post/");
		fileContent.put(Attributes.BODY, "<div> Test <img src='./first.jpg' /></div>");
		
		HtmlUtil.fixImageSourceUrls(fileContent, config);
		
		String body = fileContent.get(Attributes.BODY).toString();
		
		assertThat(body).contains("src=\"http://www.jbake.org/blog/2017/05/first.jpg\"");
	}
	
	@Test
	public void shouldNotChangeForHTTP(){
		Map<String, Object> fileContent = new HashMap<String, Object>();
		fileContent.put(Attributes.ROOTPATH, "../../../");
		fileContent.put(Attributes.URI, "blog/2017/05/first_post.html");
		fileContent.put(Attributes.NO_EXTENSION_URI, "blog/2017/05/first_post/");
		fileContent.put(Attributes.BODY, "<div> Test <img src='http://example.com/first.jpg' /></div>");
		
		HtmlUtil.fixImageSourceUrls(fileContent, config);
		
		String body = fileContent.get(Attributes.BODY).toString();
		
		assertThat(body).contains("src=\"http://example.com/first.jpg\"");

	}
	
	@Test
	public void shouldNotChangeForHTTPS(){
		Map<String, Object> fileContent = new HashMap<String, Object>();
		fileContent.put(Attributes.ROOTPATH, "../../../");
		fileContent.put(Attributes.URI, "blog/2017/05/first_post.html");
		fileContent.put(Attributes.NO_EXTENSION_URI, "blog/2017/05/first_post/");
		fileContent.put(Attributes.BODY, "<div> Test <img src='https://example.com/first.jpg' /></div>");
		
		HtmlUtil.fixImageSourceUrls(fileContent, config);
		
		String body = fileContent.get(Attributes.BODY).toString();
		
		assertThat(body).contains("src=\"https://example.com/first.jpg\"");
	}
}
