package fixture.groovyMarkupTemplates

xmlDeclaration()
urlset( xmlns:"http://www.sitemaps.org/schemas/sitemap/0.9",
        'xmlns:xsi':"http://www.w3.org/2001/XMLSchema-instance",
        'xsi:schemaLocation':"http://www.sitemaps.org/schemas/sitemap/0.9 http://www.sitemaps.org/schemas/sitemap/0.9/sitemap.xsd"){

    published_content.each { content ->
        url {
            loc("${config.site_host}${content.uri}")
            lastmod("${content.date.format("yyyy-MM-dd")}")
        }
    }
}