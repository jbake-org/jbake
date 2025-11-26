package org.jbake.util

import org.hamcrest.core.Is
import org.junit.Assert.*
import org.junit.Test

class PagingHelperTest {
    @Test fun getNumberOfPages() {
        val expected = 3
        val total = 5
        val perPage = 2

        val helper = PagingHelper(total.toLong(), perPage)
        assertEquals(expected.toLong(), helper.numberOfPages.toLong())
    }

    @Test fun shouldReturnRootIndexPage() {
        val helper = PagingHelper(5, 2)
        val previousFileName = helper.getPreviousFileName(2)
        assertThat("", Is.`is`(previousFileName))
    }

    @Test fun shouldReturnPreviousFileName() {
        val helper = PagingHelper(5, 2)
        val previousFileName = helper.getPreviousFileName(3)
        assertThat("2/", Is.`is`(previousFileName))
    }

    @Test fun shouldReturnNullIfNoPreviousPageAvailable() {
        val helper = PagingHelper(5, 2)
        val previousFileName = helper.getPreviousFileName(1)
        assertNull(previousFileName)
    }

    @Test fun shouldReturnNullIfNextPageNotAvailable() {
        val helper = PagingHelper(5, 2)
        val nextFileName = helper.getNextFileName(3)
        assertNull(nextFileName)
    }

    @Test fun shouldReturnNextFileName() {
        val helper = PagingHelper(5, 2)
        val nextFileName = helper.getNextFileName(2)
        assertThat("3/", Is.`is`(nextFileName))
    }
}
