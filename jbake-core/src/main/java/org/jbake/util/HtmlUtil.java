package org.jbake.util;

import org.jbake.app.Crawler.Attributes;
import org.jbake.app.configuration.JBakeConfiguration;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Map;

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
     * If image path starts with "./", i.e. relative to the source file, then it first replace that with output file directory and the add site host.
     *
     * @param fileContents  Map representing file contents
     * @param configuration Configuration object
     */
    public static void fixImageSourceUrls(Map<String, Object> fileContents, JBakeConfiguration configuration) {
        String htmlContent = fileContents.get(Attributes.BODY).toString();
        boolean prependSiteHost = configuration.getImgPathPrependHost();
        String siteHost = configuration.getSiteHost();
        String uri = getDocumentUri(fileContents);

        Document document = Jsoup.parseBodyFragment(htmlContent);
        Elements allImgs = document.getElementsByTag("img");

        for (Element img : allImgs) {
            transformImageSource(img, uri, siteHost, prependSiteHost);
        }

        //Use body().html() to prevent adding <body></body> from parsed fragment.
        fileContents.put(Attributes.BODY, document.body().html());
    }

    private static String getDocumentUri(Map<String, Object> fileContents) {
        String uri = fileContents.get(Attributes.URI).toString();

        if (fileContents.get(Attributes.NO_EXTENSION_URI) != null) {
            uri = fileContents.get(Attributes.NO_EXTENSION_URI).toString();
            uri = removeTrailingSlash(uri);
        }

        if (uri.contains("/")) {
            uri = removeFilename(uri);
        }
        return uri;
    }

    private static void transformImageSource(Element img, String uri, String siteHost, boolean prependSiteHost) {
        String source = img.attr("src");

        // Now add the root path
        if (!source.startsWith("http://") && !source.startsWith("https://")) {

            if (isRelative(source)) {
                source = uri + source.replaceFirst("\\./", "");
            }

            if (prependSiteHost) {
                if (!siteHost.endsWith("/") && isRelative(source)) {
                    siteHost = siteHost.concat("/");
                }
                source = siteHost + source;
            }

            img.attr("src", source);
        }
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
