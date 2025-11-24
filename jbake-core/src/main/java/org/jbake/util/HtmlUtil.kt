package org.jbake.util

import org.jbake.app.configuration.JBakeConfiguration
import org.jbake.model.DocumentModel
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

/**
 * @author Manik Magar
 */
object HtmlUtil {
    /**
     * Image paths are specified as w.r.t. assets folder. This function prefix site host to all img src except
     * the ones that starts with http://, https://.
     *
     *
     * If image path starts with "./", i.e. relative to the source file, then it first replace that with output file directory and the add site host.
     *
     * @param fileContents  Map representing file contents
     * @param configuration Configuration object
     */
    @JvmStatic
    fun fixImageSourceUrls(fileContents: DocumentModel, configuration: JBakeConfiguration) {
        val htmlContent = fileContents.body
        val prependSiteHost = configuration.imgPathPrependHost
        val siteHost: String = configuration.siteHost ?: error("siteHost must not be null")
        val uri = getDocumentUri(fileContents)

        val document = Jsoup.parseBodyFragment(htmlContent)
        val allImgs = document.getElementsByTag("img")

        for (img in allImgs) {
            transformImageSource(img, uri, siteHost, prependSiteHost)
        }

        //Use body().html() to prevent adding <body></body> from parsed fragment.
        fileContents.body = (document.body().html())
    }

    private fun getDocumentUri(fileContents: DocumentModel): String {
        var uri = fileContents.noExtensionUri?.let { removeTrailingSlash(it) } ?: fileContents.uri

        if (uri.contains("/"))
            uri = removeFilename(uri)
        return uri
    }

    private fun transformImageSource(img: Element, uri: String, siteHost: String, prependSiteHost: Boolean) {
        var source = img.attr("src")

        // Early return for absolute URLs
        if (source.startsWith("http://") || source.startsWith("https://")) return

        if (isRelative(source)) {
            source = uri + source.replaceFirst("\\./".toRegex(), "")
        }

        if (prependSiteHost) {
            val prefix = if (!siteHost.endsWith("/") && isRelative(source)) "$siteHost/" else siteHost
            source = prefix + source
        }

        img.attr("src", source)
    }

    private fun removeFilename(uri: String) = uri.take(uri.lastIndexOf('/') + 1)

    private fun removeTrailingSlash(uri: String) = uri.trimEnd('/')

    private fun isRelative(source: String) = !source.startsWith("/")
}
