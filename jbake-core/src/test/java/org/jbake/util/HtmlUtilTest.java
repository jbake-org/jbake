package org.jbake.util;

import org.jbake.app.Crawler.Attributes;
import org.jbake.app.configuration.ConfigUtil;
import org.jbake.app.configuration.DefaultJBakeConfiguration;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;


public class HtmlUtilTest {

    private DefaultJBakeConfiguration config;

    @Before
    public void setUp() throws Exception {
        config = (DefaultJBakeConfiguration) new ConfigUtil().loadConfig(new File(this.getClass().getResource("/fixture").getFile()));
    }

    @Test
    public void shouldNotAddBodyHTMLElement() {
        Map<String, Object> fileContent = new HashMap<String, Object>();
        fileContent.put(Attributes.ROOTPATH, "../../../");
        fileContent.put(Attributes.URI, "blog/2017/05/first_post.html");
        fileContent.put(Attributes.BODY, "<div> Test <img src='/blog/2017/05/first.jpg' /></div>");

        HtmlUtil.fixImageSourceUrls(fileContent, config);

        String body = fileContent.get(Attributes.BODY).toString();

        assertThat(body).doesNotContain("<body>");
        assertThat(body).doesNotContain("</body>");

    }

    @Test
    public void shouldNotAddSiteHost() {
        Map<String, Object> fileContent = new HashMap<String, Object>();
        fileContent.put(Attributes.ROOTPATH, "../../../");
        fileContent.put(Attributes.URI, "blog/2017/05/first_post.html");
        fileContent.put(Attributes.BODY, "<div> Test <img src='./first.jpg' /></div>");
        config.setImgPathPrependHost(false);

        HtmlUtil.fixImageSourceUrls(fileContent, config);

        String body = fileContent.get(Attributes.BODY).toString();

        assertThat(body).contains("src=\"blog/2017/05/first.jpg\"");

    }

    @Test
    public void shouldAddSiteHostWithRelativeImageToDocument() {
        Map<String, Object> fileContent = new HashMap<>();
        fileContent.put(Attributes.ROOTPATH, "../../../");
        fileContent.put(Attributes.URI, "blog/2017/05/first_post.html");
        fileContent.put(Attributes.BODY, "<div> Test <img src='img/deeper/underground.jpg' /></div>");
        config.setImgPathPrependHost(true);

        HtmlUtil.fixImageSourceUrls(fileContent, config);

        String body = fileContent.get(Attributes.BODY).toString();

        assertThat(body).contains("src=\"http://www.jbake.org/blog/2017/05/img/deeper/underground.jpg\"");
    }

    @Test
    public void shouldAddContentPath() {
        Map<String, Object> fileContent = new HashMap<String, Object>();
        fileContent.put(Attributes.ROOTPATH, "../../../");
        fileContent.put(Attributes.URI, "blog/2017/05/first_post.html");
        fileContent.put(Attributes.BODY, "<div> Test <img src='./first.jpg' /></div>");

        HtmlUtil.fixImageSourceUrls(fileContent, config);

        String body = fileContent.get(Attributes.BODY).toString();

        assertThat(body).contains("src=\"http://www.jbake.org/blog/2017/05/first.jpg\"");

    }

    @Test
    public void shouldAddContentPathForCurrentDirectory() {
        Map<String, Object> fileContent = new HashMap<String, Object>();
        fileContent.put(Attributes.ROOTPATH, "../../../");
        fileContent.put(Attributes.URI, "blog/2017/05/first_post.html");
        fileContent.put(Attributes.BODY, "<div> Test <img src='first.jpg' /></div>");

        HtmlUtil.fixImageSourceUrls(fileContent, config);

        String body = fileContent.get(Attributes.BODY).toString();

        assertThat(body).contains("src=\"http://www.jbake.org/blog/2017/05/first.jpg\"");

    }

    @Test
    public void shouldNotAddRootPath() {
        Map<String, Object> fileContent = new HashMap<String, Object>();
        fileContent.put(Attributes.ROOTPATH, "../../../");
        fileContent.put(Attributes.URI, "blog/2017/05/first_post.html");
        fileContent.put(Attributes.BODY, "<div> Test <img src='/blog/2017/05/first.jpg' /></div>");

        HtmlUtil.fixImageSourceUrls(fileContent, config);

        String body = fileContent.get(Attributes.BODY).toString();

        assertThat(body).contains("src=\"http://www.jbake.org/blog/2017/05/first.jpg\"");

    }

    @Test
    public void shouldNotAddRootPathForNoExtension() {
        Map<String, Object> fileContent = new HashMap<String, Object>();
        fileContent.put(Attributes.ROOTPATH, "../../../");
        fileContent.put(Attributes.URI, "blog/2017/05/first_post.html");
        fileContent.put(Attributes.NO_EXTENSION_URI, "blog/2017/05/first_post/");
        fileContent.put(Attributes.BODY, "<div> Test <img src='/blog/2017/05/first.jpg' /></div>");

        HtmlUtil.fixImageSourceUrls(fileContent, config);

        String body = fileContent.get(Attributes.BODY).toString();

        assertThat(body).contains("src=\"http://www.jbake.org/blog/2017/05/first.jpg\"");

    }

    @Test
    public void shouldAddContentPathForNoExtension() {
        Map<String, Object> fileContent = new HashMap<String, Object>();
        fileContent.put(Attributes.ROOTPATH, "../../../");
        fileContent.put(Attributes.URI, "blog/2017/05/first_post.html");
        fileContent.put(Attributes.NO_EXTENSION_URI, "blog/2017/05/first_post/");
        fileContent.put(Attributes.BODY, "<div> Test <img src='./first.jpg' /></div>");

        HtmlUtil.fixImageSourceUrls(fileContent, config);

        String body = fileContent.get(Attributes.BODY).toString();

        assertThat(body).contains("src=\"http://www.jbake.org/blog/2017/05/first.jpg\"");
    }

    @Test
    public void shouldNotChangeForHTTP() {
        Map<String, Object> fileContent = new HashMap<String, Object>();
        fileContent.put(Attributes.ROOTPATH, "../../../");
        fileContent.put(Attributes.URI, "blog/2017/05/first_post.html");
        fileContent.put(Attributes.NO_EXTENSION_URI, "blog/2017/05/first_post/");
        fileContent.put(Attributes.BODY, "<div> Test <img src='http://example.com/first.jpg' /></div>");

        HtmlUtil.fixImageSourceUrls(fileContent, config);

        String body = fileContent.get(Attributes.BODY).toString();

        assertThat(body).contains("src=\"http://example.com/first.jpg\"");

    }

    @Test
    public void shouldNotChangeForHTTPS() {
        Map<String, Object> fileContent = new HashMap<String, Object>();
        fileContent.put(Attributes.ROOTPATH, "../../../");
        fileContent.put(Attributes.URI, "blog/2017/05/first_post.html");
        fileContent.put(Attributes.NO_EXTENSION_URI, "blog/2017/05/first_post/");
        fileContent.put(Attributes.BODY, "<div> Test <img src='https://example.com/first.jpg' /></div>");

        HtmlUtil.fixImageSourceUrls(fileContent, config);

        String body = fileContent.get(Attributes.BODY).toString();

        assertThat(body).contains("src=\"https://example.com/first.jpg\"");
    }
}
