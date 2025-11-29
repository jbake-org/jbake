package fixture.groovyMarkupTemplates

layout 'layout/main.tpl',
        bodyContents: contents {

            div(class:"row-fluid marketing"){
                div(class:"span12"){
                    h2("${content.title}")
                    p(class:"post-date", "${content.date.format("dd MMMM yyyy")}")
                    p("${content.body}")
                }
            }

            hr()

            h5('Published Posts')

            published_posts.each { post ->
                    a(href:"${config.site_host}${post.uri}","${post.title}")
            }

        }