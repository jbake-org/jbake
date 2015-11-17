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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.jbake.app.Crawler;
import org.jbake.app.Renderer;
import org.jbake.app.ConfigUtil.Keys;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author jdlee
 */
public class FreemarkerTemplateEngineRenderingTest extends AbstractTemplateEngineRenderingTest{

    public FreemarkerTemplateEngineRenderingTest() {
        super("freemarkerTemplates", "ftl");

        outputStrings.put("post", Arrays.asList("<h1>Second Post</h1>",
                "<p><em>28 February 2013</em></p>",
                "Lorem ipsum dolor sit amet", "<meta property=\"og:description\" content=\"Something\"/>"));
        outputStrings.put("page", Arrays.asList("<title>About</title>",
	    	"All about stuff!"));
        outputStrings.put("index", Arrays.asList("<a href=\"blog/2012/first-post.html\">",
                "<a href=\"blog/2013/second-post.html\">"));
        outputStrings.put("feed", Arrays.asList("<description>My corner of the Internet</description>",
                "<title>Second Post</title>",
                "<title>First Post</title>"));
        outputStrings.put("archive", Arrays.asList("<a href=\"blog/2013/second-post.html\">",
        	"<a href=\"blog/2012/first-post.html\">"));
        outputStrings.put("tags", Arrays.asList("<a href=\"blog/2013/second-post.html\">",
        	"<a href=\"blog/2012/first-post.html\">"));
        outputStrings.put("sitemap", Arrays.asList("blog/2013/second-post.html",
        	"blog/2012/first-post.html",
        	"papers/published-paper.html"));
    }

    @Test
	@Override
	public void renderIndex() throws Exception {
		config.setProperty(Keys.PAGINATE_INDEX, true);
        config.setProperty(Keys.POSTS_PER_PAGE, 1);
        outputStrings.put("index", Arrays.asList("<a href=\"blog/2013/second-post.html\">"));
        super.renderIndex();
        
        File outputFile2 = new File(destinationFolder, "index2.html");
        Assert.assertTrue(outputFile2.exists());
        
        String output2 = FileUtils.readFileToString(outputFile2);
        assertThat(output2).contains("<a href=\"blog/2012/first-post.html\">");
	}

}
