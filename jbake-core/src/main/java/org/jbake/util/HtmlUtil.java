package org.jbake.util;

import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.jbake.app.Crawler.Attributes;
import org.jbake.app.configuration.JBakeConfiguration;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * @author Manik Magar
 */
public class HtmlUtil {

    private HtmlUtil() {
    }

    /**
     * Image paths are specified as w.r.t. assets folder. This function prefix site host to all img src except
     * the ones that starts with http://, https://.
     * <p>
     * If the image path is relative to the source file (doesn't start with "/"),
     * then it first replace that with output file directory and the add site host.
     *
     * @param fileContents  Map representing file contents
     * @param configuration Configuration object
     *
     * TODO: This is too complicated, will need a refactor again.
     */
    public static void fixImageSourceUrls(Map<String, Object> fileContents, JBakeConfiguration configuration)
    {
        String siteHost = configuration.getSiteHost();
        siteHost = StringUtils.appendIfMissing(siteHost, "/");

        boolean prependSiteHost = configuration.getImgPathPrependHost();
        boolean relativePointsToAssets = configuration.getRelativeImagePathsPointToAssets();

        String dirUri = getDocumentUri(fileContents);

        String htmlContent = fileContents.get(Attributes.BODY).toString();
        Document document = Jsoup.parseBodyFragment(htmlContent);

        Elements allImgs = document.getElementsByTag("img");
        for (Element img : allImgs) {
            transformImageSource(img, dirUri, siteHost, prependSiteHost, relativePointsToAssets);
        }

        // Use body().html() to prevent adding <body></body> from parsed fragment.
        fileContents.put(Attributes.BODY, document.body().html());
    }

    private static String getDocumentUri(Map<String, Object> fileContents) {
        String dirUri = fileContents.get(Attributes.URI).toString();

        if (fileContents.get(Attributes.NO_EXTENSION_URI) != null) {
            dirUri = fileContents.get(Attributes.NO_EXTENSION_URI).toString();
            dirUri = removeTrailingSlash(dirUri);
        }

        if (dirUri.contains("/")) {
            dirUri = removeFilename(dirUri);
        }
        return dirUri;
    }

    private static void transformImageSource(Element img, String dirUri, String siteHost,
                                             boolean prependSiteHost,
                                             boolean relativePointsToAssets) {
        String srcUrl = img.attr("src");
        if (srcUrl.startsWith("http://") || srcUrl.startsWith("https://"))
            return;

        if (isRelativeToSourceMarkup(srcUrl, relativePointsToAssets))
        {
            // Image relative to current content is explicitly specified, lets add dir URI to it.
            srcUrl = dirUri + srcUrl;
        }

        //source = StringUtils.removeStart(source, "/");

        if (prependSiteHost) {
            if (!siteHost.endsWith("/") && isRelative(srcUrl)) {
                siteHost = siteHost.concat("/");
            }
            // Now add the base URL.
            srcUrl = siteHost + srcUrl;
        }

        img.attr("src", srcUrl);
    }

    private static boolean isRelativeToSourceMarkup(String srcUrl, boolean relativePointsToAssets)
    {
        return (relativePointsToAssets ? srcUrl.startsWith("./") : !srcUrl.startsWith("/"));
    }

    private static String removeFilename(String uri) {
        uri = uri.substring(0, uri.lastIndexOf('/') + 1);
        return uri;
    }

    private static String removeTrailingSlash(String uri) {
        if (uri.endsWith("/")) {
            uri = uri.substring(0, uri.length() - 1);
        }
        return uri;
    }

    private static boolean isRelative(String source) {
        return !source.startsWith("/");
    }

}
