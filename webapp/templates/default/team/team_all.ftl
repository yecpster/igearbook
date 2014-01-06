<#include "../header.htm"/>
<@navHeader "team" />
<link href="${contextPath}/templates/${templateName}/styles/teams.css?${startupTime}" media="screen" rel="stylesheet" type="text/css" />

<#--side-->
<#include "team_rank.ftl"/>
<#--side end-->

<#--index_main-->
<div id="index_main">
    <div class="clearfix">
        <div class="groups_title"><span>全部群组</span></div>
            <div class="clearfix">
                <#list categories as category>
                <#list category.getForums() as team>
                <ul class="group">
                  <li class="logo"><div class="logo">
                  <a href="
                  <#if team.uri?exists>
                     <@s.url value="/${team.uri}" />
                  <#else>
                    <@s.url value="/team/show/${team.id}" />
                  </#if>
                  " title="${team.description?default("")}"><img src="${contextPath}${team.logo?default("")}" alt="${team.name}" height="48" width="48"></a></div> </li>
                  <li class="clearfix">
                  <a href="
                  <#if team.uri?exists>
                     <@s.url value="/${team.uri}" />
                  <#else>
                    <@s.url value="/team/show/${team.id}" />
                  </#if>
                    " title="${team.description?default("")}"><strong>${team.name}</strong></a> (${team.totalTopics})</li>
                </ul>
                </#list>
                </#list>
            </div>
        </div>
    </div>


</div>
<#--index_main end-->
        
        <div id="local">
  <p> 
      <#if canCreateTeam>
        <a href="<@s.url namespace="/team" action="insert" />" class="new_group_link">创建群组</a>  &nbsp;&nbsp;
      </#if>
      <#--
      <a href="" class="my_group_link">我的群组</a>
      -->
  </p>
    <#include "team_hot.ftl"/>

<#--boxes end-->

  

<#include "../bottom.htm"/>
