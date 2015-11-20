<#include "header.ftl">
	
	<#include "menu.ftl">

	<div class="page-header">
		<h1>Blog</h1>
	</div>
	<#list posts as post>
  		<#if (post.status == "published")>
        	<h4><a href="${post.uri}">${post.title}</a></h4>
  			<p>${post.date?string("dd MMMM yyyy")}</p>
  			<p>${post.body}</p>
  		</#if>
  	</#list>
	
	<hr />
	
	<p>Older posts are available in the <a href="/${config.archive_file}">archive</a>.</p>

<#include "footer.ftl">