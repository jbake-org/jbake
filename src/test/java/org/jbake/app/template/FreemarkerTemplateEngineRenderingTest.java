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
                "index.html\">Previous</a>",
                "index3.html\">Next</a>",
                "2 of 3"
        ));

        renderer.renderIndexPaging("index.html");

        File outputFile = new File(destinationFolder, "index2.html");
        String output = FileUtils.readFileToString(outputFile, Charset.defaultCharset());

        for (String string : getOutputStrings("index")) {
            assertThat(output).contains(string);
        }
    }

}
