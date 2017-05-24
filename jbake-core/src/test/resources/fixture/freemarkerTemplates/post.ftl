<#include "header.ftl">
	
	<#include "menu.ftl">
	
	<div class="page-header">
		<h2><#escape x as x?xml>${content.title}</#escape></h2>
	</div>

	<p class="post-date">${content.date?string("dd MMMM yyyy")}</p>

	<p>${content.body}</p>

	<hr />

	<h5>Published Posts</h5>
	<#list published_posts as post>
	<a href="${config.site_host}/${post.uri}">${post.title}</a>
	</#list>

<#include "footer.ftl">