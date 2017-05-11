package fixture.groovyMarkupTemplates

layout 'layout/main.tpl',
        bodyContents: contents {

            div(class:"row-fluid marketing"){
                div(class:"span12"){
                    h4("${content.title}")
                    p("${content.body}")
                }
            }

            hr()

            h5('Published Pages')
            published_pages.each {page ->
                a(href:"${config.site_host}${page.uri}","${page.title}")
            }

        }




