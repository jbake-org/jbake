package org.jbake.util

import java.net.URI
import java.net.URISyntaxException
import kotlin.math.ceil

class PagingHelper(totalDocuments: Long, postsPerPage: Int) {

    val numberOfPages: Int = ceil((totalDocuments * 1.0) / (postsPerPage * 1.0)).toInt()

    /** Returns the next page file name or null if there is no next page.  */
    @Throws(URISyntaxException::class)
    fun getNextFileName(currentPage: Int): String? =
        if (currentPage >= this.numberOfPages) null
        else validated("${currentPage + 1}/")


    @Throws(URISyntaxException::class)
    fun getPreviousFileName(currentPage: Int): String?
        = when (currentPage) {
            1 -> null
            // Returning to first page -> return "" -> prefixed with `content.rootpath` should get to site root.
            2 -> ""
            else -> validated("${currentPage - 1}/")
        }

    @Throws(URISyntaxException::class)
    fun getCurrentFileName(page: Int, fileName: String): String =
        if (page == 1) fileName
        else validated("$page/$fileName")

    private fun validated(path: String) = URI(path).toString()
}
