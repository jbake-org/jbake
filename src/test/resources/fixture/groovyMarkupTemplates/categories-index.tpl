package fixture.groovyMarkupTemplates

layout 'layout/main.tpl',
        bodyContents: contents {

            div(class:"row-fluid marketing"){

                div(class:"span12"){
                    h1("Category List")
                    div{
                    	categories.each { cat ->
			                    a(href:"${cat.uri}","${cat.name}")
			            }
                        
                    }
                }
            }

            hr()
        }