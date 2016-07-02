<%include "header.gsp"%>

      <!--<div class="jumbotron">
        <h1>Bake your own site!</h1>
        <p class="lead">Cras justo odio, dapibus ac facilisis in, egestas eget quam. Fusce dapibus, tellus ac cursus commodo, tortor mauris condimentum nibh, ut fermentum massa justo sit amet risus.</p>
        <a class="btn btn-large btn-success" href="#">Sign up today</a>
      </div>

	<hr>-->

	<div class="row-fluid marketing">
		<div class="span12">
			<%published_posts[0..<2].each { post ->%>
			<h4><a href="${post.uri}">${post.title}</a></h4>
			<p>${new java.text.SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH).format(post.date)} - ${post.body.substring(0, 150)}...</p>
			<%}%>
			<a href="/archive.html">Archive</a>
		</div>
	</div>

	<hr>

<%include "footer.gsp"%>