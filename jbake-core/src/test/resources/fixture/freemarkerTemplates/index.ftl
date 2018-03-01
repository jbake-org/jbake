<#include "header.ftl">

<#include "menu.ftl">

<div class="page-header">
    <h1>Blog</h1>
</div>
<#list published_posts as post>
    <#if (post.status == "published")>
    <h4><a href="${post.uri}">${post.title}</a></h4>
    <p>${post.date?string("dd MMMM yyyy")}</p>
    <p>${post.body}</p>
    </#if>
</#list>
<span>${db.getPublishedPages().size()}</span>
<hr/>

<#if (config.index_paginate!false) >
<span> <#if (previousFileName??) > <a href="${previousFileName}">Previous</a> </#if> - <#if (nextFileName??) > <a href="${nextFileName}">Next</a> </#if></span>
<span> ${currentPageNumber} of ${numberOfPages} </span>
<hr/>
</#if>

<p>Older posts are available in the <a href="/${config.archive_file}">archive</a>.</p>

<#include "footer.ftl">