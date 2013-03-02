<#include "header.ftl">
      
      <div class="row-fluid marketing">
		<div class="span12">
				<h2>${content.title}</h2>
				<p class="post-date">${content.date?string("dd MMMM yyyy")}</p>
				<p>${content.body}</p>	
		</div>
	</div>

	<hr>
	
<#include "footer.ftl">
