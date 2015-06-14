/*
 * The MIT License
 *
 * Copyright 2015 jdlee.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.jbake.app.template;

import java.util.Arrays;

/**
 *
 * @author jdlee
 */
public class ThymeleafTemplateEngineRenderingTest extends AbstractTemplateEngineRenderingTest{

    public ThymeleafTemplateEngineRenderingTest() {
        super("thymeleafTemplates", "thyme");
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
        outputStrings.put("index", Arrays.asList("<h4><a href=\"blog/2012/first-post.html\" shape=\"rect\">First Post</a></h4>",
        	"<h4><a href=\"blog/2013/second-post.html\" shape=\"rect\">Second Post</a></h4>"));
        outputStrings.put("feed", Arrays.asList("<description>My corner of the Internet</description>",
        	"<title>Second Post</title>",
        	"<title>First Post</title>"));
        outputStrings.put("archive", Arrays.asList("<a href=\"blog/2013/second-post.html\" shape=\"rect\">Second Post</a></h4>",
        	"<a href=\"blog/2012/first-post.html\" shape=\"rect\">First Post</a></h4>"));
        outputStrings.put("tags", Arrays.asList("<a href=\"blog/2013/second-post.html\" shape=\"rect\">Second Post</a></h4>",
        	"<a href=\"blog/2012/first-post.html\" shape=\"rect\">First Post</a></h4>"));
        outputStrings.put("sitemap", Arrays.asList("blog/2013/second-post.html",
        	"blog/2012/first-post.html",
        	"papers/published-paper.html"));
    }

}
