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

import org.apache.commons.configuration.CompositeConfiguration;
import org.jbake.FakeDocumentBuilder;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author jdlee
 */
public class PaginationTest {

    private CompositeConfiguration config;
    private ContentStore db;

    @Before
    public void setup() throws Exception {
        config = ConfigUtil.load(new File(this.getClass().getResource("/fixture").getFile()));
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
        db.shutdown();
    }

    @Test
    public void testPagination() {
        final int TOTAL_POSTS = 5;
        final int PER_PAGE = 2;
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        for (int i = 1; i <= TOTAL_POSTS; i++) {
        	cal.add(Calendar.SECOND, 5);
            FakeDocumentBuilder builder = new FakeDocumentBuilder("post");
            builder.withName("dummyfile" + i)
                    .withCached(true)
                    .withStatus("published")
                    .withDate(cal.getTime())
                    .build();
        }

        int pageCount = 1;
        int start = 0;
        db.setLimit(PER_PAGE);

        while (start < TOTAL_POSTS) {
            db.setStart(start);
            DocumentList posts = db.getPublishedPosts(true);

            assertThat( posts.size() ).isLessThanOrEqualTo( 2 );

            if( posts.size() > 1 ) {
                assertThat((Date) posts.get(0).get("date")).isAfter((Date) posts.get(1).get("date"));
            }

            pageCount++;
            start += PER_PAGE;
        }
        Assert.assertEquals(4, pageCount);
    }
}
