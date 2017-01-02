package org.jbake.util;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by frank on 28.10.16.
 */
public class PagingHelperTest {
    @Test
    public void getNumberOfPages() throws Exception {
        int expected = 3;
        int total = 5;
        int perPage = 2;

        PagingHelper helper = new PagingHelper(total,perPage);

        Assert.assertEquals( expected, helper.getNumberOfPages() );
    }

}