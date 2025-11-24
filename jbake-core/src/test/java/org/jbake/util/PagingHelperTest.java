package org.jbake.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PagingHelperTest {

    @Test
    void getNumberOfPages() throws Exception {
        int expected = 3;
        int total = 5;
        int perPage = 2;

        PagingHelper helper = new PagingHelper(total, perPage);

        assertEquals(expected, helper.getNumberOfPages());
    }

    @Test
    void shouldReturnRootIndexPage() throws Exception {
        PagingHelper helper = new PagingHelper(5, 2);

        String previousFileName = helper.getPreviousFileName(2);

        assertThat(previousFileName).isEmpty();
    }

    @Test
    void shouldReturnPreviousFileName() throws Exception {
        PagingHelper helper = new PagingHelper(5, 2);

        String previousFileName = helper.getPreviousFileName(3);

        assertThat(previousFileName).isEqualTo("2/");
    }

    @Test
    void shouldReturnNullIfNoPreviousPageAvailable() throws Exception {
        PagingHelper helper = new PagingHelper(5, 2);

        String previousFileName = helper.getPreviousFileName(1);

        assertNull(previousFileName);
    }

    @Test
    void shouldReturnNullIfNextPageNotAvailable() throws Exception {
        PagingHelper helper = new PagingHelper(5, 2);

        String nextFileName = helper.getNextFileName(3);

        assertNull(nextFileName);
    }

    @Test
    void shouldReturnNextFileName() throws Exception {
        PagingHelper helper = new PagingHelper(5, 2);

        String nextFileName = helper.getNextFileName(2);

        assertThat(nextFileName).isEqualTo("3/");
    }
}
