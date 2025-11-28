package org.jbake.util

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import org.jbake.TestUtils
import org.jbake.app.configuration.ConfigUtil
import org.jbake.app.configuration.DefaultJBakeConfiguration
import org.jbake.model.DocumentModel

class HtmlUtilTest : StringSpec({
    lateinit var config: DefaultJBakeConfiguration

    beforeTest {
        config = ConfigUtil().loadConfig(TestUtils.testResourcesAsSourceFolder) as DefaultJBakeConfiguration
    }

    "shouldNotAddBodyHTMLElement" {
        val fileContent = DocumentModel()
        fileContent.rootPath = "../../../"
        fileContent.uri = "blog/2017/05/first_post.html"
        fileContent.body = "<div> Test <img src='/blog/2017/05/first.jpg' /></div>"

        HtmlUtil.fixImageSourceUrls(fileContent, config)

        val body = fileContent.body

        body shouldNotContain "<body>"
        body shouldNotContain "</body>"
    }

    "shouldNotAddSiteHost" {
        val fileContent = DocumentModel()
        fileContent.rootPath = "../../../"
        fileContent.uri = "blog/2017/05/first_post.html"
        fileContent.body = "<div> Test <img src='./first.jpg' /></div>"
        config.imgPathPrependHost = false

        HtmlUtil.fixImageSourceUrls(fileContent, config)

        val body = fileContent.body

        body shouldContain "src=\"blog/2017/05/first.jpg\""
    }

    "shouldAddSiteHostWithRelativeImageToDocument" {
        val fileContent = DocumentModel()
        fileContent.rootPath = "../../../"
        fileContent.uri = "blog/2017/05/first_post.html"
        fileContent.body = "<div> Test <img src='img/deeper/underground.jpg' /></div>"
        config.imgPathPrependHost = true

        HtmlUtil.fixImageSourceUrls(fileContent, config)

        val body = fileContent.body

        body shouldContain "src=\"http://www.jbake.org/blog/2017/05/img/deeper/underground.jpg\""
    }

    "shouldAddContentPath" {
        val fileContent = DocumentModel()
        fileContent.rootPath = "../../../"
        fileContent.uri = "blog/2017/05/first_post.html"
        fileContent.body = "<div> Test <img src='./first.jpg' /></div>"
        config.imgPathPrependHost = true

        HtmlUtil.fixImageSourceUrls(fileContent, config)

        val body = fileContent.body

        body shouldContain "src=\"http://www.jbake.org/blog/2017/05/first.jpg\""
    }

    "shouldAddContentPathForCurrentDirectory" {
        val fileContent = DocumentModel()
        fileContent.rootPath = "../../../"
        fileContent.uri = "blog/2017/05/first_post.html"
        fileContent.body = "<div> Test <img src='first.jpg' /></div>"
        config.imgPathPrependHost = true

        HtmlUtil.fixImageSourceUrls(fileContent, config)

        val body = fileContent.body

        body shouldContain "src=\"http://www.jbake.org/blog/2017/05/first.jpg\""
    }

    "shouldNotAddRootPath" {
        val fileContent = DocumentModel()
        fileContent.rootPath = "../../../"
        fileContent.uri = "blog/2017/05/first_post.html"
        fileContent.body = "<div> Test <img src='/blog/2017/05/first.jpg' /></div>"

        HtmlUtil.fixImageSourceUrls(fileContent, config)

        val body = fileContent.body

        body shouldContain "src=\"http://www.jbake.org/blog/2017/05/first.jpg\""
    }

    "shouldNotAddRootPathForNoExtension" {
        val fileContent = DocumentModel()
        fileContent.rootPath = "../../../"
        fileContent.uri = "blog/2017/05/first_post.html"
        fileContent.noExtensionUri = "blog/2017/05/first_post/"
        fileContent.body = "<div> Test <img src='/blog/2017/05/first.jpg' /></div>"

        HtmlUtil.fixImageSourceUrls(fileContent, config)

        val body = fileContent.body

        body shouldContain "src=\"http://www.jbake.org/blog/2017/05/first.jpg\""
    }

    "shouldAddContentPathForNoExtension" {
        val fileContent = DocumentModel()
        fileContent.rootPath = "../../../"
        fileContent.uri = "blog/2017/05/first_post.html"
        fileContent.noExtensionUri = "blog/2017/05/first_post/"
        fileContent.body = "<div> Test <img src='./first.jpg' /></div>"

        HtmlUtil.fixImageSourceUrls(fileContent, config)

        val body = fileContent.body

        body shouldContain "src=\"http://www.jbake.org/blog/2017/05/first.jpg\""
    }

    "shouldNotChangeForHTTP" {
        val fileContent = DocumentModel()
        fileContent.rootPath = "../../../"
        fileContent.uri = "blog/2017/05/first_post.html"
        fileContent.noExtensionUri = "blog/2017/05/first_post/"
        fileContent.body = "<div> Test <img src='http://example.com/first.jpg' /></div>"

        HtmlUtil.fixImageSourceUrls(fileContent, config)

        val body = fileContent.body

        body shouldContain "src=\"http://example.com/first.jpg\""
    }

    "shouldNotChangeForHTTPS" {
        val fileContent = DocumentModel()
        fileContent.rootPath = "../../../"
        fileContent.uri = "blog/2017/05/first_post.html"
        fileContent.noExtensionUri = "blog/2017/05/first_post/"
        fileContent.body = "<div> Test <img src='https://example.com/first.jpg' /></div>"

        HtmlUtil.fixImageSourceUrls(fileContent, config)

        val body = fileContent.body

        body shouldContain "src=\"https://example.com/first.jpg\""
    }
})
