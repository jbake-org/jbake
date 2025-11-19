package org.jbake.util

import java.net.URI
import java.net.URISyntaxException
import kotlin.math.ceil

class PagingHelper(private val totalDocuments: Long, private val postsPerPage: Int) {
    val numberOfPages: Int
        get() = ceil((totalDocuments * 1.0) / (postsPerPage * 1.0)).toInt()

    @Throws(URISyntaxException::class)
    fun getNextFileName(currentPageNumber: Int): String {
        return if (currentPageNumber < this.numberOfPages) {
            URI((currentPageNumber + 1).toString() + URI_SEPARATOR).toString()
        } else {
            ""
        }
    }

    @Throws(URISyntaxException::class)
    fun getPreviousFileName(currentPageNumber: Int): String {
        return if (isFirstPage(currentPageNumber)) {
            ""
        } else if (currentPageNumber == 2) {
            // Returning to first page, return empty string which when prefixed with content.rootpath should get to root of the site.
            ""
        } else {
            URI((currentPageNumber - 1).toString() + URI_SEPARATOR).toString()
        }
    }

    private fun isFirstPage(page: Int): Boolean {
        return page == 1
    }

    @Throws(URISyntaxException::class)
    fun getCurrentFileName(page: Int, fileName: String): String {
        return if (isFirstPage(page)) {
            fileName
        } else {
            URI(page.toString() + URI_SEPARATOR + fileName).toString()
        }
    }

    companion object {
        private const val URI_SEPARATOR = "/"
    }
}
