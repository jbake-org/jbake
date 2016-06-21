<#include "header.ftl">

	<#include "menu.ftl">
	
	<div class="page-header">
		<h1>All Categories</h1>
	</div>
	
		<ul>
		<#list content.categories?keys as category>
			<li><a href="${content.categories[category]}"/>${category}</a></li>
		</#list>
		</ul>
	
<#include "footer.ftl">