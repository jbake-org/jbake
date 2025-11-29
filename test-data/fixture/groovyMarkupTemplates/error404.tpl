package fixture.groovyMarkupTemplates

layout 'layout/main.tpl',
        bodyContents: contents {
            h1('404 Not found')
            h2('The requested page is not found.')
        }
