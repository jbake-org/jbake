<?xml version="1.0" encoding="UTF-8"?>
<#list all_content as content>
    <content>
        <uri>${config.site_host}${content.uri}</uri>
        <date>${content.date?string("yyyy-MM-dd")}</date>
    </content>
</#list>