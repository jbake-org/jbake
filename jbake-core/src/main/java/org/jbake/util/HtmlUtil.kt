package org.jbake.util

import org.jbake.app.configuration.JBakeConfiguration
import org.jbake.model.DocumentModel
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

object HtmlUtil {

    /**
     * Image paths are specified as w.r.t. assets directory. This function prefix site host to all img src except the ones that starts with http://, https://.
     *
     * If image path starts with "./", i.e. relative to the source file, then it first replace that with output file directory and the add site host.
     */
    @JvmStatic
    fun fixImageSourceUrls(documentModel: DocumentModel, conf: JBakeConfiguration) {

        val document = Jsoup.parseBodyFragment(documentModel.body)
        val allImgElements = document.getElementsByTag("img")
        for (img in allImgElements) {
            transformImageSource(img, getDocumentUri(documentModel), conf.siteHost ?: error("siteHost must not be null"), conf.imgPathPrependHost)
        }

        // Use body().html() to prevent adding <body></body> from parsed fragment.
        documentModel.body = document.body().html()
    }

    private fun getDocumentUri(documentModel: DocumentModel): String {
        var uri = documentModel.noExtensionUri?.let { it.trimEnd('/') } ?: documentModel.uri

        // Remove filename.
        if (uri.contains("/"))
            uri = uri.take(uri.lastIndexOf('/') + 1)

        return uri
    }

    private fun transformImageSource(img: Element, uri: String, siteHost: String, prependSiteHost: Boolean) {
        var source = img.attr("src")

        // Early return for absolute URLs
        if (source.startsWith("http://") || source.startsWith("https://"))
            return

        val isRelative = !source.startsWith("/")
        if (isRelative)
            source = uri + source.replaceFirst("\\./".toRegex(), "")

        if (prependSiteHost) {
            val prefix = if (!siteHost.endsWith("/") && isRelative) "$siteHost/" else siteHost
            source = prefix + source
        }

        img.attr("src", source)
    }

}
