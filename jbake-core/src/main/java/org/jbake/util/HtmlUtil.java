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
    public static void fixImageSourceUrls(DocumentModel fileContents, JBakeConfiguration configuration) {
        String htmlContent = fileContents.getBody();
        boolean prependSiteHost = configuration.getImgPathPrependHost();
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
        fileContents.setBody(document.body().html());
    }

    private static String getDocumentUri(DocumentModel fileContents) {
        String uri = fileContents.getUri();

        if (fileContents.getNoExtensionUri() != null) {
            uri = fileContents.getNoExtensionUri();
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
