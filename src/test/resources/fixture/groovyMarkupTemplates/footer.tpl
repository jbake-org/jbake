package fixture.groovyMarkupTemplates

div(class:"footer"){
    p{
        yieldUnescaped '&copy; Jonathan Bullock 2013 | Mixed with '
        a(href:"http://twitter.github.com/bootstrap/",'Bootstrap v2.3.1')
        yieldUnescaped '| Baked with '
        a(href:"http://jbake.org","JBake ${version}")
    }
}
