package fixture.groovyMarkupTemplates

layout 'layout/main.tpl',
        bodyContents: contents {
            div(class:"row-fluid marketing"){
                div(class:"span12"){
                    h1('Tags')

                    tags.each {tag ->

                        h2 {
                            a(href:"${content.rootpath}${tag.uri}","${tag.name}")
                            yield "${tag.tagged_posts.size()}"
                        }
                    }
                }
            }
        }