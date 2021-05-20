package org.jbake.util;

import org.jbake.TestUtils;
import org.jbake.app.configuration.ConfigUtil;
import org.jbake.app.configuration.DefaultJBakeConfiguration;
import org.jbake.model.DocumentModel;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class HtmlUtilTest {

    private DefaultJBakeConfiguration config;

    @Before
    public void setUp() throws Exception {
        config = (DefaultJBakeConfiguration) new ConfigUtil().loadConfig(TestUtils.getTestResourcesAsSourceFolder());
    }

    @Test
    public void shouldNotAddBodyHTMLElement() {
        DocumentModel fileContent = new DocumentModel();
        fileContent.setRootPath("../../../");
        fileContent.setUri("blog/2017/05/first_post.html");
        fileContent.setBody("<div> Test <img src='/blog/2017/05/first.jpg' /></div>");

        HtmlUtil.fixImageSourceUrls(fileContent, config);

        String body = fileContent.getBody();

        assertThat(body).doesNotContain("<body>");
        assertThat(body).doesNotContain("</body>");

    }

    @Test
    public void shouldNotAddSiteHost() {
        DocumentModel fileContent = new DocumentModel();
        fileContent.setRootPath("../../../");
        fileContent.setUri("blog/2017/05/first_post.html");
        fileContent.setBody("<div> Test <img src='./first.jpg' /></div>");
        config.setImgPathPrependHost(false);

        HtmlUtil.fixImageSourceUrls(fileContent, config);

        String body = fileContent.getBody();

        assertThat(body).contains("src=\"blog/2017/05/first.jpg\"");

    }

    @Test
    public void shouldAddSiteHostWithRelativeImageToDocument() {
        DocumentModel fileContent = new DocumentModel();
        fileContent.setRootPath("../../../");
        fileContent.setUri("blog/2017/05/first_post.html");
        fileContent.setBody("<div> Test <img src='img/deeper/underground.jpg' /></div>");
        config.setImgPathPrependHost(true);

        HtmlUtil.fixImageSourceUrls(fileContent, config);

        String body = fileContent.getBody();

        assertThat(body).contains("src=\"http://www.jbake.org/blog/2017/05/img/deeper/underground.jpg\"");
    }

    @Test
    public void shouldAddContentPath() {
        DocumentModel fileContent = new DocumentModel();
        fileContent.setRootPath("../../../");
        fileContent.setUri("blog/2017/05/first_post.html");
        fileContent.setBody("<div> Test <img src='./first.jpg' /></div>");
        config.setImgPathPrependHost(true);

        HtmlUtil.fixImageSourceUrls(fileContent, config);

        String body = fileContent.getBody();

        assertThat(body).contains("src=\"http://www.jbake.org/blog/2017/05/first.jpg\"");

    }

    @Test
    public void shouldAddContentPathForCurrentDirectory() {
        DocumentModel fileContent = new DocumentModel();
        fileContent.setRootPath("../../../");
        fileContent.setUri("blog/2017/05/first_post.html");
        fileContent.setBody("<div> Test <img src='first.jpg' /></div>");
        config.setImgPathPrependHost(true);

        HtmlUtil.fixImageSourceUrls(fileContent, config);

        String body = fileContent.getBody();

        assertThat(body).contains("src=\"http://www.jbake.org/blog/2017/05/first.jpg\"");

    }

    @Test
    public void shouldNotAddRootPath() {
        DocumentModel fileContent = new DocumentModel();
        fileContent.setRootPath("../../../");
        fileContent.setUri("blog/2017/05/first_post.html");
        fileContent.setBody("<div> Test <img src='/blog/2017/05/first.jpg' /></div>");

        HtmlUtil.fixImageSourceUrls(fileContent, config);

        String body = fileContent.getBody();

        assertThat(body).contains("src=\"http://www.jbake.org/blog/2017/05/first.jpg\"");

    }

    @Test
    public void shouldNotAddRootPathForNoExtension() {
        DocumentModel fileContent = new DocumentModel();
        fileContent.setRootPath("../../../");
        fileContent.setUri("blog/2017/05/first_post.html");
        fileContent.setNoExtensionUri("blog/2017/05/first_post/");
        fileContent.setBody("<div> Test <img src='/blog/2017/05/first.jpg' /></div>");

        HtmlUtil.fixImageSourceUrls(fileContent, config);

        String body = fileContent.getBody();

        assertThat(body).contains("src=\"http://www.jbake.org/blog/2017/05/first.jpg\"");

    }

    @Test
    public void shouldAddContentPathForNoExtension() {
        DocumentModel fileContent = new DocumentModel();
        fileContent.setRootPath("../../../");
        fileContent.setUri("blog/2017/05/first_post.html");
        fileContent.setNoExtensionUri("blog/2017/05/first_post/");
        fileContent.setBody("<div> Test <img src='./first.jpg' /></div>");

        HtmlUtil.fixImageSourceUrls(fileContent, config);

        String body = fileContent.getBody();

        assertThat(body).contains("src=\"http://www.jbake.org/blog/2017/05/first.jpg\"");
    }

    @Test
    public void shouldNotChangeForHTTP() {
        DocumentModel fileContent = new DocumentModel();
        fileContent.setRootPath("../../../");
        fileContent.setUri("blog/2017/05/first_post.html");
        fileContent.setNoExtensionUri("blog/2017/05/first_post/");
        fileContent.setBody("<div> Test <img src='http://example.com/first.jpg' /></div>");

        HtmlUtil.fixImageSourceUrls(fileContent, config);

        String body = fileContent.getBody();

        assertThat(body).contains("src=\"http://example.com/first.jpg\"");

    }

    @Test
    public void shouldNotChangeForHTTPS() {
        DocumentModel fileContent = new DocumentModel();
        fileContent.setRootPath("../../../");
        fileContent.setUri("blog/2017/05/first_post.html");
        fileContent.setNoExtensionUri("blog/2017/05/first_post/");
        fileContent.setBody("<div> Test <img src='https://example.com/first.jpg' /></div>");

        HtmlUtil.fixImageSourceUrls(fileContent, config);

        String body = fileContent.getBody();

        assertThat(body).contains("src=\"https://example.com/first.jpg\"");
    }
}
