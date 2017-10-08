<%include 'header.gsp'%>

      <!--<div class="jumbotron">
        <h1>Bake your own site!</h1>
        <p class="lead">Cras justo odio, dapibus ac facilisis in, egestas eget quam. Fusce dapibus, tellus ac cursus commodo, tortor mauris condimentum nibh, ut fermentum massa justo sit amet risus.</p>
        <a class="btn btn-large btn-success" href="#">Sign up today</a>
      </div>

	<hr>-->

	<div class="row-fluid marketing">
		<div class="span12">
			<h1>Tags</h1>
            <%def last_month=null;%>
            <%tags.each {tag ->%>
				
				
				<h2><a href="${tag.uri}">${tag.name}</a></h2>
				
			<%}%>
		</div>
	</div>

	<hr>

<%include "footer.gsp"%>