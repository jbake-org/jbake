package org.jbake.util;

import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class PagingHelperTest {
    @Test
    public void getNumberOfPages() throws Exception {
        int expected = 3;
        int total = 5;
        int perPage = 2;

        PagingHelper helper = new PagingHelper(total,perPage);

        Assert.assertEquals( expected, helper.getNumberOfPages() );
    }

    @Test
    public void shouldReturnRootIndexPage() throws Exception {
        PagingHelper helper = new PagingHelper(5,2);

        String previousFileName = helper.getPreviousFileName(2, "index.html");

        Assert.assertThat("", is( previousFileName) );
    }

    @Test
    public void shouldReturnPreviousFileName() throws Exception {
        PagingHelper helper = new PagingHelper(5,2);

        String previousFileName = helper.getPreviousFileName(3, "index.html");

        Assert.assertThat("2/", is( previousFileName) );
    }

    @Test
    public void shouldReturnNullIfNoPreviousPageAvailable() throws Exception {
        PagingHelper helper = new PagingHelper(5,2);

        String previousFileName = helper.getPreviousFileName(1, "index.html");

        Assert.assertNull( previousFileName );
    }

    @Test
    public void shouldReturnNullIfNextPageNotAvailable() throws Exception {
        PagingHelper helper = new PagingHelper(5,2);

        String nextFileName = helper.getNextFileName(3, "index.html");

        Assert.assertNull( nextFileName );
    }

    @Test
    public void shouldReturnNextFileName() throws Exception {
        PagingHelper helper = new PagingHelper(5,2);

        String nextFileName = helper.getNextFileName(2, "index.html");

        Assert.assertThat("3/", is( nextFileName) );
    }
}