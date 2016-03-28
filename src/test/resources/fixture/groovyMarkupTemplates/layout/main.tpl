package fixture.groovyMarkupTemplates.layout

yieldUnescaped '<!DOCTYPE html>'
html(lang:'en'){

    head {
        include template: "header.tpl"
    }

    body {
        div(class:"container-narrow"){

            include template: 'menu.tpl'

            hr()

            bodyContents()

            hr()

            include template: 'footer.tpl'

        } 

        script(src:"/js/jquery-1.9.1.min.js"){}
        newLine()
        script(src:"/js/bootstrap.min.js"){}
    }
}
newLine()
