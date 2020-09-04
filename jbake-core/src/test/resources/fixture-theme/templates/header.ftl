<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="utf-8"/>
    <title><#if (content.title)??>${content.title}<#else>JBake</#if></title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="">
    <meta name="author" content="">
    <meta name="keywords" content="">
    <meta name="generator" content="JBake">

    <!-- Le styles -->
    <link href="${content.rootpath!}css/bootstrap.min.css" rel="stylesheet">
    <link href="${content.rootpath!}css/asciidoctor.css" rel="stylesheet">
    <link href="${content.rootpath!}css/base.css" rel="stylesheet">
    <link href="${content.rootpath!}css/prettify.css" rel="stylesheet">

    <!-- HTML5 shim, for IE6-8 support of HTML5 elements -->
    <!--[if lt IE 9]>
      <script src="${content.rootpath!}js/html5shiv.min.js"></script>
    <![endif]-->

    <!-- Fav and touch icons -->
    <link rel="shortcut icon" href="${content.rootpath!}favicon.ico">
    <#if content?? && content.og??>
        <meta property="og:description" content="${content.og.description}"/>
    </#if>
  </head>
  <body onload="prettyPrint()">
    <div id="wrap">
