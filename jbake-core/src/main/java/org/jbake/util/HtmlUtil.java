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

    private static final char SLASH = '/';
    private static final String SLASH_TEXT = String.valueOf(SLASH);
    private static final String HTTP = "http://";
    private static final String HTTPS = "https://";
    private static final String WORKING_DIR = "\\./";
    private static final String EMPTY = "";

    private HtmlUtil() {
    }

    /**
     * Images or file paths are specified as w.r.t. assets folder. This function prefix site host to all path except
     * the ones that starts with http://, https://.
     * <p>
     * If path starts with "./", i.e. relative to the source file, then it first replace that with output file directory and the add site host.
     *
     * @param fileContents  Map representing file contents
     * @param configuration Configuration object
     */
    public static void fixUrls(Map<String, Object> fileContents, JBakeConfiguration configuration) {
        String htmlContent = fileContents.get(Attributes.BODY).toString();
        boolean prependSiteHost = configuration.getImgPathPrependHost() && configuration.getRelativePathPrependHost();
        String siteHost = configuration.getSiteHost();
        String uri = getDocumentUri(fileContents);

        Document document = Jsoup.parseBodyFragment(htmlContent);
        Map<String, String> pairs = configuration.getTagAttributes();
        for (Map.Entry<String, String> entry : pairs.entrySet()) {
            String tagName = entry.getKey();
            String attKey = entry.getValue();
            Elements allTags = document.getElementsByTag(tagName);
            for (Element tag : allTags) {
                transformPath(tag, attKey, uri, siteHost, prependSiteHost);
            }
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

        if (uri.contains(SLASH_TEXT)) {
            uri = removeFilename(uri);
        }
        return uri;
    }

    private static void transformPath(Element tag, String attrKey, String uri, String siteHost, boolean prependSiteHost) {
        String path = tag.attr(attrKey);

        // Now add the root path
        if (!isUrl(path)) {
            if (isRelative(path)) {
                path = uri + path.replaceFirst(WORKING_DIR, EMPTY);
            }
            if (prependSiteHost) {
                if (!siteHost.endsWith(SLASH_TEXT) && isRelative(path)) {
                    siteHost = siteHost.concat(SLASH_TEXT);
                }
                path = siteHost + path;
            }
            tag.attr(attrKey, path);
        }
    }

    private static String removeFilename(String uri) {
        uri = uri.substring(0, uri.lastIndexOf(SLASH) + 1);
        return uri;
    }

    private static String removeTrailingSlash(String uri) {
        if (uri.endsWith(SLASH_TEXT)) {
            uri = uri.substring(0, uri.length() - 1);
        }
        return uri;
    }

    private static boolean isUrl(String path) {
        return path.startsWith(HTTP) || path.startsWith(HTTPS);
    }

    private static boolean isRelative(String source) {
        return !source.startsWith(SLASH_TEXT);
    }
}
