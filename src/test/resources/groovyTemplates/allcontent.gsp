<?xml version="1.0" encoding="UTF-8"?>
<list>
    <% all_content.each { content -> %>
    <content>
        <uri>${config.site_host}${content.uri}</uri>
        <date>${content.date.format("yyyy-MM-dd")}</date>
    </content>
    <%}%>
</list>
