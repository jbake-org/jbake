<%include "header.gsp"%>
      
      <div class="row-fluid marketing">
		<div class="span12">
				<h2>${content.title}</h2>
				<p class="post-date">${content.date.format("dd MMMM yyyy")}</p>
				<p>${content.body}</p>	
		</div>
	</div>

	<hr>
	
	<h5>Published Posts</h5>
    <%published_posts.each {post -> %>
    	<a href="${config.site_host}${post.uri}">${post.title}</a>
    <%}%>
	
<%include "footer.gsp"%>
