package org.jbake.util

import org.assertj.core.api.Assertions.assertThat
import org.jbake.TestUtils
import org.jbake.app.configuration.ConfigUtil
import org.jbake.app.configuration.DefaultJBakeConfiguration
import org.jbake.model.DocumentModel
import org.junit.Before
import org.junit.Test

class HtmlUtilTest {
    private lateinit var config: DefaultJBakeConfiguration

    @Before
    fun setUp() {
        config = ConfigUtil().loadConfig(TestUtils.testResourcesAsSourceFolder) as DefaultJBakeConfiguration
    }

    @Test fun shouldNotAddBodyHTMLElement() {
        val fileContent = DocumentModel()
        fileContent.rootPath = "../../../"
        fileContent.uri = "blog/2017/05/first_post.html"
        fileContent.body = "<div> Test <img src='/blog/2017/05/first.jpg' /></div>"

        HtmlUtil.fixImageSourceUrls(fileContent, config)

        val body = fileContent.body

        assertThat(body).doesNotContain("<body>")
        assertThat(body).doesNotContain("</body>")
    }

    @Test fun shouldNotAddSiteHost() {
        val fileContent = DocumentModel()
        fileContent.rootPath = "../../../"
        fileContent.uri = "blog/2017/05/first_post.html"
        fileContent.body = "<div> Test <img src='./first.jpg' /></div>"
        config.imgPathPrependHost = false

        HtmlUtil.fixImageSourceUrls(fileContent, config)

        val body = fileContent.body

        assertThat(body).contains("src=\"blog/2017/05/first.jpg\"")
    }

    @Test fun shouldAddSiteHostWithRelativeImageToDocument() {
        val fileContent = DocumentModel()
        fileContent.rootPath = "../../../"
        fileContent.uri = "blog/2017/05/first_post.html"
        fileContent.body = "<div> Test <img src='img/deeper/underground.jpg' /></div>"
        config.imgPathPrependHost = true

        HtmlUtil.fixImageSourceUrls(fileContent, config)

        val body = fileContent.body

        assertThat(body).contains("src=\"http://www.jbake.org/blog/2017/05/img/deeper/underground.jpg\"")
    }

    @Test fun shouldAddContentPath() {
        val fileContent = DocumentModel()
        fileContent.rootPath = "../../../"
        fileContent.uri = "blog/2017/05/first_post.html"
        fileContent.body = "<div> Test <img src='./first.jpg' /></div>"
        config.imgPathPrependHost = true

        HtmlUtil.fixImageSourceUrls(fileContent, config)

        val body = fileContent.body

        assertThat(body).contains("src=\"http://www.jbake.org/blog/2017/05/first.jpg\"")
    }

    @Test fun shouldAddContentPathForCurrentDirectory() {
        val fileContent = DocumentModel()
        fileContent.rootPath = "../../../"
        fileContent.uri = "blog/2017/05/first_post.html"
        fileContent.body = "<div> Test <img src='first.jpg' /></div>"
        config.imgPathPrependHost = true

        HtmlUtil.fixImageSourceUrls(fileContent, config)

        val body = fileContent.body

        assertThat(body).contains("src=\"http://www.jbake.org/blog/2017/05/first.jpg\"")
    }

    @Test fun shouldNotAddRootPath() {
        val fileContent = DocumentModel()
        fileContent.rootPath = "../../../"
        fileContent.uri = "blog/2017/05/first_post.html"
        fileContent.body = "<div> Test <img src='/blog/2017/05/first.jpg' /></div>"

        HtmlUtil.fixImageSourceUrls(fileContent, config)

        val body = fileContent.body

        assertThat(body).contains("src=\"http://www.jbake.org/blog/2017/05/first.jpg\"")
    }

    @Test fun shouldNotAddRootPathForNoExtension() {
        val fileContent = DocumentModel()
        fileContent.rootPath = "../../../"
        fileContent.uri = "blog/2017/05/first_post.html"
        fileContent.noExtensionUri = "blog/2017/05/first_post/"
        fileContent.body = "<div> Test <img src='/blog/2017/05/first.jpg' /></div>"

        HtmlUtil.fixImageSourceUrls(fileContent, config)

        val body = fileContent.body

        assertThat(body).contains("src=\"http://www.jbake.org/blog/2017/05/first.jpg\"")
    }

    @Test fun shouldAddContentPathForNoExtension() {
        val fileContent = DocumentModel()
        fileContent.rootPath = "../../../"
        fileContent.uri = "blog/2017/05/first_post.html"
        fileContent.noExtensionUri = "blog/2017/05/first_post/"
        fileContent.body = "<div> Test <img src='./first.jpg' /></div>"

        HtmlUtil.fixImageSourceUrls(fileContent, config)

        val body = fileContent.body

        assertThat(body).contains("src=\"http://www.jbake.org/blog/2017/05/first.jpg\"")
    }

    @Test fun shouldNotChangeForHTTP() {
        val fileContent = DocumentModel()
        fileContent.rootPath = "../../../"
        fileContent.uri = "blog/2017/05/first_post.html"
        fileContent.noExtensionUri = "blog/2017/05/first_post/"
        fileContent.body = "<div> Test <img src='http://example.com/first.jpg' /></div>"

        HtmlUtil.fixImageSourceUrls(fileContent, config)

        val body = fileContent.body

        assertThat(body).contains("src=\"http://example.com/first.jpg\"")
    }

    @Test fun shouldNotChangeForHTTPS() {
        val fileContent = DocumentModel()
        fileContent.rootPath = "../../../"
        fileContent.uri = "blog/2017/05/first_post.html"
        fileContent.noExtensionUri = "blog/2017/05/first_post/"
        fileContent.body = "<div> Test <img src='https://example.com/first.jpg' /></div>"

        HtmlUtil.fixImageSourceUrls(fileContent, config)

        val body = fileContent.body

        assertThat(body).contains("src=\"https://example.com/first.jpg\"")
    }
}
