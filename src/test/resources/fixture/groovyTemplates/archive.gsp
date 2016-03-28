<%include 'header.gsp'%>

      <!--<div class="jumbotron">
        <h1>Bake your own site!</h1>
        <p class="lead">Cras justo odio, dapibus ac facilisis in, egestas eget quam. Fusce dapibus, tellus ac cursus commodo, tortor mauris condimentum nibh, ut fermentum massa justo sit amet risus.</p>
        <a class="btn btn-large btn-success" href="#">Sign up today</a>
      </div>

	<hr>-->

	<div class="row-fluid marketing">
		<div class="span12">
			<h2>Archive</h2>
            <%def last_month=null;%>
            <%posts.each {post ->%>
				<%if (last_month) {%>
					<%if (new java.text.SimpleDateFormat("MMMM yyyy", Locale.ENGLISH).format(post.date) != last_month) {%>
						<h3>${new java.text.SimpleDateFormat("MMMM yyyy", Locale.ENGLISH).format(post.date)}</h3>
					<%}%>
				<% } else { %>
					<h3>${new java.text.SimpleDateFormat("MMMM yyyy", Locale.ENGLISH).format(post.date)}</h3>
				<% }%>
				
				<h4>${post.date.format("dd MMMM")} - <a href="${post.uri}">${post.title}</a></h4>
				<%last_month = new java.text.SimpleDateFormat("MMMM yyyy", Locale.ENGLISH).format(post.date)%>
			<%}%>
		</div>
	</div>

	<hr>

<%include "footer.gsp"%>