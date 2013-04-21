<?xml version="1.0"?>
<rss version="2.0" xmlns:atom="http://www.w3.org/2005/Atom">
  <channel>
    <title>JonathanBullock.com</title>
    <link>http://jonathanbullock.com/</link>
    <atom:link href="http://jonathanbullock.com/feed.xml" rel="self" type="application/rss+xml" />
    <description>My corner of the Internet</description>
    <language>en-gb</language>
    <pubDate>${pubdate?string("EEE, d MMM yyyy HH:mm:ss Z")}</pubDate>
    <lastBuildDate>${pubdate?string("EEE, d MMM yyyy HH:mm:ss Z")}</lastBuildDate>

    <#list posts as post>
    <item>
      <title>${post.title}</title>
      <link>http://jonathanbullock.com${post.uri}</link>
      <pubDate>${post.date?string("EEE, d MMM yyyy HH:mm:ss Z")}</pubDate>
      <guid isPermaLink="false">${post.uri}</guid>
      	<description>
	<#escape x as x?xml>	
	${post.body}
	</#escape>
	</description>
    </item>
    </#list>

  </channel> 
</rss>
