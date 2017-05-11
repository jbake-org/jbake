package fixture.groovyMarkupTemplates

layout 'layout/main.tpl',
        bodyContents: contents {

                div(class:"row-fluid marketing"){
                    div(class:"span12"){
                        published_posts[0..<2].each { post ->
                            h4 { a(href:"${post.uri}","${post.title}") }
                            p("${post.date.format("dd MMMM yyyy")} - ${post.body.substring(0, 150)}...")
                        }
                        a(href:"/archive.html",'Archive')
                    }
                }
        }