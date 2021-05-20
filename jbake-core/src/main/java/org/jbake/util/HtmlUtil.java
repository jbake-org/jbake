package org.jbake.util;

import org.jbake.app.configuration.JBakeConfiguration;
import org.jbake.model.DocumentModel;
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
     * If image path starts with "./", i.e. relative to the source file, then it first replace that with output file directory and the add site host.
     *
     * @param fileContents  Map representing file contents
     * @param configuration Configuration object
     */
    public static void fixImageSourceUrls(DocumentModel fileContents, JBakeConfiguration configuration) {
        String htmlContent = fileContents.getBody();
        boolean prependSiteHost = configuration.getImgPathPrependHost();
        String siteHost = configuration.getSiteHost();
        String uri = getDocumentUri(fileContents);

        Document document = Jsoup.parseBodyFragment(htmlContent);
        Elements allImgs = document.getElementsByTag("img");

        for (Element img : allImgs) {
            transformImageSource(img, uri, siteHost, prependSiteHost);
        }

        //Use body().html() to prevent adding <body></body> from parsed fragment.
        fileContents.setBody(document.body().html());
    }

    private static String getDocumentUri(DocumentModel fileContents) {
        String uri = fileContents.getUri();

        if (fileContents.getNoExtensionUri() != null) {
            uri = fileContents.getNoExtensionUri();
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
