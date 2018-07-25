package org.jbake.util;

import java.util.Map;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.jbake.app.ConfigUtil.Keys;
import org.jbake.app.Crawler.Attributes;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * @author Manik Magar
 */
public class HtmlUtil
{
    /**
     * Image paths are specified as w.r.t. assets folder. This function prefix site host to all img src except
     * the ones that starts with http://, https://.
     * <p>
     * If image path starts with "./", i.e. relative to the source file, then it first replace that with output file directory and the add site host.
     *
     * @param fileContents Map representing file contents
     * @param config       Configuration object
     */
    public static void fixImageSourceUrls(Map<String, Object> fileContents, CompositeConfiguration config)
    {

        String siteHost = config.getString(Keys.SITE_HOST);
        siteHost = StringUtils.appendIfMissing(siteHost, "/");

        String dirUri = fileContents.get(Attributes.URI).toString();

        if (fileContents.get(Attributes.NO_EXTENSION_URI) != null) {
            dirUri = fileContents.get(Attributes.NO_EXTENSION_URI).toString();
            StringUtils.removeEnd(dirUri, "/");
        }

        if (dirUri.contains("/")) {
            dirUri = StringUtils.substringBeforeLast(dirUri, "/") + "/";
        }

        String htmlContent = fileContents.get(Attributes.BODY).toString();
        Document document = Jsoup.parseBodyFragment(htmlContent);

        Elements allImgs = document.getElementsByTag("img");
        for (Element img : allImgs) {
            String source = img.attr("src");

            if (source.startsWith("http://") || source.startsWith("https://"))
                continue;

            if (source.startsWith("./")) {
                // Image relative to current content is explicitly specified, lets add dir URI to it.
                source = source.replaceFirst("./", dirUri);
            }

            source = StringUtils.removeStart(source, "/");

            // Now add the root path
            img.attr("src", siteHost + source);
        }

        //Use body().html() to prevent adding <body></body> from parsed fragment.
        fileContents.put(Attributes.BODY, document.body().html());
    }
}
