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

import org.apache.commons.io.FileUtils;
import org.jbake.app.ConfigUtil.Keys;
import org.junit.Test;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author jdlee
 */
public class FreemarkerTemplateEngineRenderingTest extends AbstractTemplateEngineRenderingTest {

    public FreemarkerTemplateEngineRenderingTest() {
        super("freemarkerTemplates", "ftl");
    }

    @Test
    public void renderPaginatedIndex() throws Exception {
        config.setProperty(Keys.PAGINATE_INDEX, true);
        config.setProperty(Keys.POSTS_PER_PAGE, 1);

        outputStrings.put("index", Arrays.asList(
                "\">Previous</a>",
                "3/\">Next</a>",
                "2 of 3"
        ));

        renderer.renderIndexPaging("index.html");

        File outputFile = new File(destinationFolder, 2 + File.separator + "index.html");
        String output = FileUtils.readFileToString(outputFile, Charset.defaultCharset());

        for (String string : getOutputStrings("index")) {
            assertThat(output).contains(string);
        }
    }

    @Test
    public void shouldFallbackToRenderSingleIndexIfNoPostArePresent() throws Exception {
        config.setProperty(Keys.PAGINATE_INDEX, true);
        config.setProperty(Keys.POSTS_PER_PAGE, 1);

        db.deleteAllByDocType("post");

        renderer.renderIndexPaging("index.html");

        File paginatedFile = new File(destinationFolder, "index2.html");
        assertFalse("paginated file is not rendered",paginatedFile.exists());

        File indexFile = new File(destinationFolder, "index.html");
        assertTrue("index file exists",indexFile.exists());

    }
    
    @Test
    public void checkDbTemplateModelIsPopulated() throws Exception {
    	
        config.setProperty(Keys.PAGINATE_INDEX, true);
        config.setProperty(Keys.POSTS_PER_PAGE, 1);
        
        outputStrings.put("dbSpan", Arrays.asList("<span>3</span>"));
        
        db.deleteAllByDocType("post");

        renderer.renderIndexPaging("index.html");

        File outputFile = new File(destinationFolder, "index.html");
        String output = FileUtils.readFileToString(outputFile, Charset.defaultCharset());

        for (String string : getOutputStrings("dbSpan")) {
            assertThat(output).contains(string);
        }

    }

}
