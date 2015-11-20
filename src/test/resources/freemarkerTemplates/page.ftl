<#include "header.ftl">

	<#include "menu.ftl">
	
	<div class="page-header">
		<h4><#escape x as x?xml>${content.title}</#escape></h4>
	</div>

	<p><em>${content.date?string("dd MMMM yyyy")}</em></p>

	<p>${content.body}</p>

	<hr />

	<h5>Published Pages</h5>
	<#list published_pages as page>
	<a href="${config.site_host}/${page.uri}">${page.title}</a>
	</#list>


<#include "footer.ftl">