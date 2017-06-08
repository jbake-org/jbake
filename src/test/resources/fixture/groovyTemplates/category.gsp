<%include 'header.gsp'%>

	<div class="page-header">
		<h1>Category: ${category}</h1>
	</div>
	<div class="category-posts">
	<!--<ul>-->
		<%def last_month=null;%>
		<%category_posts.each {post ->%>
		<%if (last_month) {%>
			<%if (post.date.format("MMMM yyyy") != last_month) {%>
				</ul>
				<h4>${post.date.format("MMMM yyyy")}</h4>
				<ul>
			<%}%>
		<%} else {%>
			<h4>${post.date.format("MMMM yyyy")}</h4>
			<ul>
		<%}%>

		<li>${post.date.format("dd")} - <a href="${content.rootpath}${post.uri}">${post.title}</a></li>
		<% last_month = post.date.format("MMMM yyyy")%>
		<%}%>
	</ul>
		</div>

<%include "footer.gsp"%>
