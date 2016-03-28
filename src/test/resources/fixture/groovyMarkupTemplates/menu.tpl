package fixture.groovyMarkupTemplates

div(class:"masthead"){
    ul(class:"nav nav-pills pull-right"){
        li { a(href:"/",'Home') }
        li { a(href:"/about.html",'About') }
        li { a(href:"/projects.html",'Projects') }
        li { a(href:"/feed.xml",'Subscribe') }

    }

    ul {
        if ( papers ){
            li(papers.size())
        }
        else {
            li("no papers no fun")
        }
        papers.each { paper ->
            li(paper.title + " " + paper.status)
        }
    }

    h3(class:"muted",'Jonathan Bullock')
}