<%include "header.gsp"%>

      <div class="row-fluid marketing">
        <div class="span12">
          <h4>${content.title}</h4>
          <p>${content.body}</p>
        </div>

      </div>

      <hr>
      
      <h5>Published Pages</h5>
      <%published_pages.each {page -> %>
      	<a href="${config.site_host}${page.uri}">${page.title}</a>
      <%}%>

<%include "footer.gsp"%>