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
package org.jbake.app;

import com.orientechnologies.orient.core.record.impl.ODocument;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.configuration.CompositeConfiguration;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author jdlee
 */
public class PaginationTest {

    private CompositeConfiguration config;
    private ContentStore db;

    @Before
    public void setup() throws Exception, IOException, URISyntaxException {
        config = ConfigUtil.load(new File(this.getClass().getResource("/").getFile()));
        Iterator<String> keys = config.getKeys();
        while (keys.hasNext()) {
            String key = keys.next();
            if (key.startsWith("template") && key.endsWith(".file")) {
                String old = (String) config.getProperty(key);
                config.setProperty(key, old.substring(0, old.length() - 4) + ".ftl");
            }
        }
        config.setProperty(ConfigUtil.Keys.PAGINATE_INDEX, true);
        config.setProperty(ConfigUtil.Keys.POSTS_PER_PAGE, 1);
        db = DBUtil.createDataStore("memory", "documents" + System.currentTimeMillis());
    }

    @After
    public void cleanup() throws InterruptedException {
        db.drop();
        db.close();
    }

    @Test
    public void testPagination() {
        Map<String, Object> fileContents = new HashMap<String, Object>();
        final int TOTAL_POSTS = 5;
        final int PER_PAGE = 2;

        for (int i = 1; i <= TOTAL_POSTS; i++) {
            fileContents.put("name", "dummyfile" + i);

            ODocument doc = new ODocument("post");
            doc.fields(fileContents);
            boolean cached = fileContents.get("cached") != null ? Boolean.valueOf((String) fileContents.get("cached")) : true;
            doc.field("cached", cached);
            doc.save();
        }
        
        int iterationCount = 0;
        int start = 0;
        db.setLimit(PER_PAGE);
        
        while (start < TOTAL_POSTS) {
            db.setStart(start);
            List<ODocument> posts = db.getAllContent("post");
            Assert.assertEquals("dummyfile" + (1 + (PER_PAGE * iterationCount)), posts.get(0).field("name"));
//            Assert.assertEquals("dummyfile" + (PER_PAGE + (PER_PAGE * iterationCount)), posts.get(posts.size()-1).field("name"));
            iterationCount++;
            start += PER_PAGE;
        }
        Assert.assertEquals(Math.round(TOTAL_POSTS / (1.0*PER_PAGE) + 0.4), iterationCount);
    }
}
