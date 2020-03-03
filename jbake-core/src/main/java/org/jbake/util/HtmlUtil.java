package org.jbake.util;

import org.jbake.app.Crawler.Attributes;
import org.jbake.app.configuration.JBakeConfiguration;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Manik Magar
 */
public class HtmlUtil {

    private static final Map<String, String> TAG_ATTR;

    static {
        Map<String, String> map = new HashMap<>();
        map.put("a", "href");
        map.put("img", "src");
        TAG_ATTR = Collections.unmodifiableMap(map);
    }

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
     * @see #fixRelativeSourceUrls(Map, JBakeConfiguration)
     */
    public static void fixImageSourceUrls(Map<String, Object> fileContents, JBakeConfiguration configuration) {
        fixRelativeSourceUrls(fileContents, configuration);
    }

    public static void fixRelativeSourceUrls(Map<String, Object> fileContents, JBakeConfiguration configuration) {
        String htmlContent = fileContents.get(Attributes.BODY).toString();
        boolean prependSiteHost = configuration.getImgPathPrependHost() || configuration.getRelativePathPrependHost();
        String siteHost = configuration.getSiteHost();
        String uri = getDocumentUri(fileContents);

        Document document = Jsoup.parseBodyFragment(htmlContent);
        for (Map.Entry<String, String> entry : TAG_ATTR.entrySet()) {
            String tagName = entry.getKey();
            String attrKey = entry.getValue();
            Elements tags = document.getElementsByTag(tagName);
            for (Element tag : tags) {
                transformRelativeSource(tag, attrKey, uri, siteHost, prependSiteHost);
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

        if (uri.contains("/")) {
            uri = removeFilename(uri);
        }
        return uri;
    }

    private static void transformRelativeSource(Element element, String attributeKey, String uri, String siteHost, boolean prependSiteHost) {
        String source = element.attr(attributeKey);

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
            element.attr(attributeKey, source);
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
