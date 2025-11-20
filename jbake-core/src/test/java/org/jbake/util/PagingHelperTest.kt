package org.jbake.util

import org.hamcrest.core.Is
import org.junit.Assert
import org.junit.Test

class PagingHelperTest {
    @Test
    @Throws(Exception::class)
    fun getNumberOfPages() {
        val expected = 3
        val total = 5
        val perPage = 2

        val helper = PagingHelper(total.toLong(), perPage)

        Assert.assertEquals(expected.toLong(), helper.numberOfPages.toLong())
    }

    @Test
    @Throws(Exception::class)
    fun shouldReturnRootIndexPage() {
        val helper = PagingHelper(5, 2)

        val previousFileName = helper.getPreviousFileName(2)

        Assert.assertThat<String>("", Is.`is`<String>(previousFileName))
    }

    @Test
    @Throws(Exception::class)
    fun shouldReturnPreviousFileName() {
        val helper = PagingHelper(5, 2)

        val previousFileName = helper.getPreviousFileName(3)

        Assert.assertThat<String>("2/", Is.`is`<String>(previousFileName))
    }

    @Test
    @Throws(Exception::class)
    fun shouldReturnNullIfNoPreviousPageAvailable() {
        val helper = PagingHelper(5, 2)

        val previousFileName = helper.getPreviousFileName(1)

        Assert.assertNull(previousFileName)
    }

    @Test
    @Throws(Exception::class)
    fun shouldReturnNullIfNextPageNotAvailable() {
        val helper = PagingHelper(5, 2)

        val nextFileName = helper.getNextFileName(3)

        Assert.assertNull(nextFileName)
    }

    @Test
    @Throws(Exception::class)
    fun shouldReturnNextFileName() {
        val helper = PagingHelper(5, 2)

        val nextFileName = helper.getNextFileName(2)

        Assert.assertThat<String>("3/", Is.`is`<String>(nextFileName))
    }
}
