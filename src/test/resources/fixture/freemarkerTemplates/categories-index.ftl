<#include "header.ftl">

	<#include "menu.ftl">
	
	<div class="page-header">
		<h1>All Categories</h1>
	</div>
	
		<ul>
		<#list categories as category>
			<li><a href="${category.uri}"/>${category.name}</a></li>
		</#list>
		</ul>
	
<#include "footer.ftl">