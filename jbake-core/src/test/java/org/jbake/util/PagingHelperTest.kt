package org.jbake.util

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

class PagingHelperTest : StringSpec({
    "getNumberOfPages" {
        val expected = 3
        val total = 5
        val perPage = 2

        val helper = PagingHelper(total.toLong(), perPage)
        helper.numberOfPages.toLong() shouldBe expected.toLong()
    }

    "shouldReturnRootIndexPage" {
        val helper = PagingHelper(5, 2)
        val previousFileName = helper.getPreviousFileName(2)
        previousFileName shouldBe ""
    }

    "shouldReturnPreviousFileName" {
        val helper = PagingHelper(5, 2)
        val previousFileName = helper.getPreviousFileName(3)
        previousFileName shouldBe "2/"
    }

    "shouldReturnNullIfNoPreviousPageAvailable" {
        val helper = PagingHelper(5, 2)
        val previousFileName = helper.getPreviousFileName(1)
        previousFileName.shouldBeNull()
    }

    "shouldReturnNullIfNextPageNotAvailable" {
        val helper = PagingHelper(5, 2)
        val nextFileName = helper.getNextFileName(3)
        nextFileName.shouldBeNull()
    }

    "shouldReturnNextFileName" {
        val helper = PagingHelper(5, 2)
        val nextFileName = helper.getNextFileName(2)
        nextFileName shouldBe "3/"
    }
})
