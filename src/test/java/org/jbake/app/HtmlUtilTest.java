package org.jbake.app;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.jbake.app.Crawler.Attributes;
import org.junit.Test;



public class HtmlUtilTest {

	@Test
	public void shouldAddRootpath(){
		Map<String, Object> fileContent = new HashMap<String, Object>();
		fileContent.put(Attributes.ROOTPATH, "../../../");
		fileContent.put(Attributes.URI, "blog/2017/05/first_post.html");
		fileContent.put(Attributes.BODY, "<div> Test <img src='blog/2017/05/first.jpg' /></div>");
		
		HtmlUtil.fixImageSourceUrls(fileContent);
		
		assertTrue(fileContent.get(Attributes.BODY).toString().contains("../../../blog/2017/05/first.jpg"));
		
	}
	
	@Test
	public void shouldAddContentpath(){
		Map<String, Object> fileContent = new HashMap<String, Object>();
		fileContent.put(Attributes.ROOTPATH, "../../../");
		fileContent.put(Attributes.URI, "blog/2017/05/first_post.html");
		fileContent.put(Attributes.BODY, "<div> Test <img src='./first.jpg' /></div>");
		
		HtmlUtil.fixImageSourceUrls(fileContent);
		
		assertTrue(fileContent.get(Attributes.BODY).toString().contains("../../../blog/2017/05/first.jpg"));
		
	}
	
	@Test
	public void shouldNotAddRootPath(){
		Map<String, Object> fileContent = new HashMap<String, Object>();
		fileContent.put(Attributes.ROOTPATH, "../../../");
		fileContent.put(Attributes.URI, "blog/2017/05/first_post.html");
		fileContent.put(Attributes.BODY, "<div> Test <img src='/blog/2017/05/first.jpg' /></div>");
		
		HtmlUtil.fixImageSourceUrls(fileContent);
		
		assertFalse(fileContent.get(Attributes.BODY).toString().contains("../../../blog/2017/05/first.jpg"));
		assertTrue(fileContent.get(Attributes.BODY).toString().contains("/blog/2017/05/first.jpg"));
		
	}
	
	@Test
	public void shouldAddRootPathForNoExtension(){
		Map<String, Object> fileContent = new HashMap<String, Object>();
		fileContent.put(Attributes.ROOTPATH, "../../../");
		fileContent.put(Attributes.URI, "blog/2017/05/first_post.html");
		fileContent.put(Attributes.NO_EXTENSION_URI, "blog/2017/05/first_post/");
		fileContent.put(Attributes.BODY, "<div> Test <img src='blog/2017/05/first.jpg' /></div>");
		
		HtmlUtil.fixImageSourceUrls(fileContent);
		
		assertTrue(fileContent.get(Attributes.BODY).toString().contains("../../../blog/2017/05/first.jpg"));

	}
	
	@Test
	public void shouldAddContentPathForNoExtension(){
		Map<String, Object> fileContent = new HashMap<String, Object>();
		fileContent.put(Attributes.ROOTPATH, "../../../");
		fileContent.put(Attributes.URI, "blog/2017/05/first_post.html");
		fileContent.put(Attributes.NO_EXTENSION_URI, "blog/2017/05/first_post/");
		fileContent.put(Attributes.BODY, "<div> Test <img src='./first.jpg' /></div>");
		
		HtmlUtil.fixImageSourceUrls(fileContent);
		
		assertTrue(fileContent.get(Attributes.BODY).toString().contains("../../../blog/2017/05/first.jpg"));

	}
	
	@Test
	public void shouldNotChangeForHTTP(){
		Map<String, Object> fileContent = new HashMap<String, Object>();
		fileContent.put(Attributes.ROOTPATH, "../../../");
		fileContent.put(Attributes.URI, "blog/2017/05/first_post.html");
		fileContent.put(Attributes.NO_EXTENSION_URI, "blog/2017/05/first_post/");
		fileContent.put(Attributes.BODY, "<div> Test <img src='http://example.com/first.jpg' /></div>");
		
		HtmlUtil.fixImageSourceUrls(fileContent);
		assertTrue(fileContent.get(Attributes.BODY).toString().contains("http://example.com/first.jpg"));

	}
	
	@Test
	public void shouldNotChangeForHTTPS(){
		Map<String, Object> fileContent = new HashMap<String, Object>();
		fileContent.put(Attributes.ROOTPATH, "../../../");
		fileContent.put(Attributes.URI, "blog/2017/05/first_post.html");
		fileContent.put(Attributes.NO_EXTENSION_URI, "blog/2017/05/first_post/");
		fileContent.put(Attributes.BODY, "<div> Test <img src='https://example.com/first.jpg' /></div>");
		
		HtmlUtil.fixImageSourceUrls(fileContent);
		assertTrue(fileContent.get(Attributes.BODY).toString().contains("https://example.com/first.jpg"));

	}
}
