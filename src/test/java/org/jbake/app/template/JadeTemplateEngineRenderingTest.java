package org.jbake.app.template;

import java.util.Arrays;

public class JadeTemplateEngineRenderingTest extends AbstractTemplateEngineRenderingTest {

	public JadeTemplateEngineRenderingTest() {
		super("jadeTemplates", "jade");

        outputStrings.put("post", Arrays.asList("<h2>Second Post</h2>",
        	"<p class=\"post-date\">28",
        	"2013</p>",
        	"Lorem ipsum dolor sit amet",
        	"<h5>Published Posts</h5>",
        	"blog/2012/first-post.html"));
        outputStrings.put("page", Arrays.asList("<h4>About</h4>",
	    	"All about stuff!",
	    	"<h5>Published Pages</h5>",
	    	"/projects.html"));
        outputStrings.put("index", Arrays.asList("<a href=\"blog/2012/first-post.html\">First Post</a>",
        	"<a href=\"blog/2013/second-post.html\">Second Post</a>"));
        outputStrings.put("feed", Arrays.asList("<description>My corner of the Internet</description>",
        	"<title>Second Post</title>",
        	"<title>First Post</title>"));
        outputStrings.put("archive", Arrays.asList("<a href=\"blog/2013/second-post.html\">Second Post</a>",
        	"<a href=\"blog/2012/first-post.html\">First Post</a>"));
        outputStrings.put("tags", Arrays.asList("<a href=\"blog/2013/second-post.html\">Second Post</a>",
        	"<a href=\"blog/2012/first-post.html\">First Post</a>"));
        outputStrings.put("sitemap", Arrays.asList("blog/2013/second-post.html",
        	"blog/2012/first-post.html",
        	"papers/published-paper.html"));
	}
}
