package fixture.groovyMarkupTemplates

xmlDeclaration()
list{
    all_content.each { cntnt ->
        content {
            uri("${config.site_host}${cntnt.uri}")
            date(${cntnt.date.format("yyyy-MM-dd")})
        }
    }
}
