<#include "header.ftl">
			<div id="body">
				<div id="post">
				<h2>${content.title}</h3>
				<p class="post-date">${content.date?string("dd MMMM yyyy")}</p>
				<p>${content.body}</p>
				<div id="share"><#include "share_links.ftl"></div>
				</div>
			</div>
<#include "footer.ftl">
