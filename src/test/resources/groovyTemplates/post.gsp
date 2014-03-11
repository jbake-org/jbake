<%include "header.gsp"%>
      
      <div class="row-fluid marketing">
		<div class="span12">
				<h2>${content.title}</h2>
				<p class="post-date">${content.date.format("dd MMMM yyyy")}</p>
				<p>${content.body}</p>	
		</div>
	</div>

	<hr>
	
<%include "footer.gsp"%>
