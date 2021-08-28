package org.jbake.util;

import org.apache.commons.collections.CollectionUtils;
import org.jbake.app.configuration.JBakeConfiguration;
import org.jbake.model.DocumentModel;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;

/**
 * @author Manik Magar
 */
public class HtmlUtil {

    private static final char SLASH = '/';
    private static final String DOUBLE_SLASH = "//";
    private static final String SLASH_TEXT = String.valueOf(SLASH);
    private static final Path ROOT = Paths.get(SLASH_TEXT);
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
    public static void fixUrls(DocumentModel fileContents, JBakeConfiguration configuration) {
        String htmlContent = fileContents.getBody();
        boolean prependSiteHost = configuration.getImgPathPrependHost() || configuration.getRelativePathPrependHost();
        String siteHost = configuration.getSiteHost();
        String uri = getDocumentUri(fileContents);
        Set<String> domains = configuration.replaceDomains();
        Document document = Jsoup.parseBodyFragment(htmlContent);
        Map<String, String> pairs = configuration.getTagAttributes();
        for (Map.Entry<String, String> entry : pairs.entrySet()) {
            String tagName = entry.getKey();
            String attKey = entry.getValue();
            Elements allTags = document.getElementsByTag(tagName);
            for (Element tag : allTags) {
                transformPath(tag, attKey, uri, siteHost, prependSiteHost, domains);
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

    /**
     * Site host, document uri, and links in this document. Normally the full path of links should be site host+document uri+ link
     * <p>
     * But, link itself may also be an url. We resolve link uri here.
     *
     * @param docUri doc uri, a path
     */
    private static void transformPath(Element tag, String attrKey,
                                      String docUri, String siteHost, boolean prependSiteHost, Set<String> domains) {
        String linkUri = tag.attr(attrKey);
        if (!isUrl(linkUri)) {
            Path linkPath = Paths.get(linkUri);
            if (!linkPath.isAbsolute()) {
                linkPath = Paths.get(docUri).resolve(linkPath);
            }
            if (prependSiteHost) {
                if (siteHost.endsWith(SLASH_TEXT)) {
                    siteHost = siteHost.substring(0, siteHost.length() - SLASH_TEXT.length());
                }
                linkUri = siteHost + ROOT.resolve(linkPath).normalize();
            } else {
                linkUri = linkPath.normalize().toString();
            }
            tag.attr(attrKey, linkUri);
        } else if (CollectionUtils.isNotEmpty(domains)) {
            int start = linkUri.indexOf(DOUBLE_SLASH);
            if (start > 0) {
                int fromIndex = start + DOUBLE_SLASH.length();
                int end = linkUri.indexOf(SLASH, fromIndex);
                if (end < fromIndex) {
                    end = linkUri.length();
                }
                String host = linkUri.substring(fromIndex, end);
                if (domains.contains(host)) {
                    String newPath = siteHost;
                    if (end < linkUri.length()) {
                        newPath += linkUri.substring(end);
                    }
                    tag.attr(attrKey, newPath);
                }
            }
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
