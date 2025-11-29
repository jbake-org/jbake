package fixture.groovyMarkupTemplates

xmlDeclaration()
rss(version:"2.0", 'xmlns:atom':'http://www.w3.org/2005/Atom') {
  channel {
    title('JonathanBullock.com')
    link('http://jonathanbullock.com/')
    atom:link(href:"http://jonathanbullock.com/feed.xml", rel:"self", type:"application/rss+xml")
    description('My corner of the Internet')
    language('en-gb')
    pubDate("${published_date.format("EEE, d MMM yyyy HH:mm:ss Z")}")
    lastBuildDate("${published_date.format("EEE, d MMM yyyy HH:mm:ss Z")}")

    posts.each { post ->
        item{
          title("${post.title}")
          link("http://jonathanbullock.com${post.uri}")
          pubDate("${post.date.format("EEE, d MMM yyyy HH:mm:ss Z")}")
          guid(isPermaLink:"false","${post.uri}")
          description("${post.body}")
        }
    }
  }
}
