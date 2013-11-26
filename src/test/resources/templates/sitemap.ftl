<?xml version="1.0" encoding="UTF-8"?>
<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.sitemaps.org/schemas/sitemap/0.9 http://www.sitemaps.org/schemas/sitemap/0.9/sitemap.xsd">
<#list pages as page>
    <url>
        <loc>${config.site_host}${page.uri}</loc>
        <lastmod>${page.date?string("yyyy-MM-dd")}</lastmod>
    </url>
</#list>
</urlset>