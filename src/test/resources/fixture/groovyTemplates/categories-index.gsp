<%include 'header.gsp'%>

	<div class="page-header">
		<h1>All Categories</h1>
	</div>
	<div class="category-posts">
		<ul>
		<%categories.each {category ->%>
			<li><a href="${category.uri}"/>${category.name}</a></li>
		<%}%>
		</ul>
		</div>

<%include "footer.gsp"%>
