package fixture.groovyMarkupTemplates

layout 'layout/main.tpl',
        bodyContents: contents {
            div(class:"row-fluid marketing"){
                div(class:"span12"){
                    h1('Tags')

                    tags.each {tag ->

                        h2 {
                            a(href:"${tag.uri}","${tag.name}")
                        }
                    }
                }
            }
        }