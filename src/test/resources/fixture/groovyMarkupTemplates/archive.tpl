package fixture.groovyMarkupTemplates

layout 'layout/main.tpl',
        bodyContents: contents {
            div(class:"row-fluid marketing"){
                div(class:"span12"){
                    h2('Archive')

                    def last_month
                    posts.each {post ->
                        if (last_month) {
                            if (post.date.format("MMMM yyyy") != last_month) {
                                h3("${post.date.format("MMMM yyyy")}")
                            }
                        }
                        else {
                            h3("${post.date.format("MMMM yyyy")}")
                        }

                        h4 {
                            yield "${post.date.format("dd MMMM")} - "
                            a(href:"${post.uri}","${post.title}")
                        }
                        last_month = post.date.format("MMMM yyyy")
                    }
                }
            }
        }