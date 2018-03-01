<?xml version="1.0"?>
<rss version="2.0" xmlns:atom="http://www.w3.org/2005/Atom">
  <channel>
    <title>JonathanBullock.com</title>
    <link>http://jonathanbullock.com/</link>
    <atom:link href="http://jonathanbullock.com/feed.xml" rel="self" type="application/rss+xml" />
    <description>My corner of the Internet</description>
    <language>en-gb</language>
    <pubDate>${new java.text.SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.ENGLISH).format(published_date)}</pubDate>
    <lastBuildDate>${new java.text.SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.ENGLISH).format(published_date)}</lastBuildDate>

    <%posts.each {post -> %>
    <item>
      <title>${post.title}</title>
      <link>http://jonathanbullock.com${post.uri}</link>
      <pubDate>${post.date.format("EEE, d MMM yyyy HH:mm:ss Z")}</pubDate>
      <guid isPermaLink="false">${post.uri}</guid>
      	<description>
	${post.body}
	</description>
    </item>
    <%}%>

  </channel> 
</rss>
