<#include "header.ftl">

      <!--<div class="jumbotron">
        <h1>Bake your own site!</h1>
        <p class="lead">Cras justo odio, dapibus ac facilisis in, egestas eget quam. Fusce dapibus, tellus ac cursus commodo, tortor mauris condimentum nibh, ut fermentum massa justo sit amet risus.</p>
        <a class="btn btn-large btn-success" href="#">Sign up today</a>
      </div>

	<hr>-->

	<div class="row-fluid marketing">
		<div class="span12">
			<#list posts as post>
			<h4><a href="${post.uri}">${post.title}</a></h4>
			<p>${post.date?string("dd MMMM yyyy")} - ${post.body?substring(0, 150)}...</p>
			<#if post_index = 2><#break></#if>
			</#list>
			<a href="/archive.html">Archive</a>
		</div>
	</div>

	<hr>

<#include "footer.ftl">