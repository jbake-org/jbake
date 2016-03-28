package fixture.groovyMarkupTemplates

meta(charset:'utf-8')
title('Jonathan Bullock')
meta(name:"viewport", content:"width=device-width, initial-scale=1.0")
meta(name:"description", content:"")
meta(name:"author", content:"Jonathan Bullock")

yieldUnescaped '<!-- Le styles -->'
link(href:"/css/bootstrap.min.css", rel:"stylesheet")
style(type:"text/css") {
    yieldUnescaped """body {
        padding-top: 20px;
        padding-bottom: 40px;
    }

    /* Custom container */
    .container-narrow {
        margin: 0 auto;
        max-width: 700px;
    }
    .container-narrow > hr {
        margin: 30px 0;
    }

    /* Main marketing message and sign up button */
    .jumbotron {
        margin: 60px 0;
        text-align: center;
    }
    .jumbotron h1 {
        font-size: 72px;
        line-height: 1;
    }
    .jumbotron .btn {
        font-size: 21px;
        padding: 14px 24px;
    }

    /* Supporting marketing content */
    .marketing {
        margin: 60px 0;
    }
    .marketing p + h4 {
        margin-top: 28px;
    }"""
}
link(href:"/css/bootstrap-responsive.min.css", rel:"stylesheet")

yieldUnescaped '<!-- HTML5 shim, for IE6-8 support of HTML5 elements -->'
yieldUnescaped '<!--[if lt IE 9]>'
  script( src:"/js/html5shiv.js")
yieldUnescaped '<![endif]-->'

yieldUnescaped '<!-- Fav and touch icons -->'
yieldUnescaped '<!--<link rel="apple-touch-icon-precomposed" sizes="144x144" href="../assets/ico/apple-touch-icon-144-precomposed.png">'
link(rel:"apple-touch-icon-precomposed", sizes:"114x114", href:"../assets/ico/apple-touch-icon-114-precomposed.png")
link(rel:"apple-touch-icon-precomposed", sizes:"72x72", href:"../assets/ico/apple-touch-icon-72-precomposed.png")
link(rel:"apple-touch-icon-precomposed", href:"../assets/ico/apple-touch-icon-57-precomposed.png")
link(rel:"shortcut icon", href="../assets/ico/favicon.png")
yieldUnescaped '-->'
