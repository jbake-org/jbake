<#include "header.ftl">
      
      <div class="row-fluid marketing">
		<div class="span12">
				<h2>${content.title}</h2>
				<p class="post-date">${content.date?string("dd MMMM yyyy")}</p>
				<p>${content.body}</p>	
		</div>
	</div>

	<hr>
	
	<h5>Published Posts</h5>
  	<#list published_posts as post>
  		<a href="${config.site_host}${post.uri}">${post.title}</a>
  	</#list>
	
<#include "footer.ftl">
