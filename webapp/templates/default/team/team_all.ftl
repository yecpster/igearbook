<#include "../header.htm"/>
<@navHeader "team" />
<link href="${contextPath}/templates/${templateName}/styles/teams.css?${startupTime}" media="screen" rel="stylesheet" type="text/css" />

<#--side-->
<div id="side">
  
    <div class="box">
      <h3>群组排名</h3>
        <#list rankTeams as rankTeam>
        <#if (rankTeam_index < 10)>
            <div class="rank">
              <div class="rank_info">${rankTeam_index + 1}.<a href="<@s.url namespace="/team" action="show"><@s.param name="teamId" value="${rankTeam.id}" /></@s.url>" title="${rankTeam.description?default("")}">${rankTeam.name}</a><br>文章数：${rankTeam.totalPosts}</div>
              <div class="rank_logo"><div class="logo"><a href="<@s.url namespace="/team" action="show"><@s.param name="teamId" value="${rankTeam.id}" /></@s.url>" title="${rankTeam.description?default("")}"><img src="${contextPath}${rankTeam.logo?default("")}" alt="${rankTeam.name}" height="48" width="48"></a></div></div>
            </div>
        </#if>
        </#list>
    </div>

</div>
<#--side end-->

<#--index_main-->
<div id="index_main">
    <div class="clearfix">
        <div class="groups_title"><span>全部群组</span></div>
            <div class="clearfix">
                <#list categories as category>
                <#list category.getForums() as team>
                <ul class="group">
                  <li class="logo"><div class="logo"><a href="<@s.url namespace="/team" action="show"><@s.param name="teamId" value="${team.id}" /></@s.url>" title="${team.description?default("")}"><img src="${contextPath}${team.logo?default("")}" alt="${team.name}" height="48" width="48"></a></div> </li>
                  <li class="clearfix"><a href="<@s.url namespace="/team" action="show"><@s.param name="teamId" value="${team.id}" /></@s.url>" title="${team.description?default("")}"><strong>${team.name}</strong></a> (${team.totalTopics})</li>
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
    <div class="box">
      <h3>近期热门群组</h3>
      <#list hotTeams as hotTeam>
        <#if (hotTeam_index < 10)>
        <div class="clearfix hot_group">
          <div class="logo"><a href="<@s.url namespace="/team" action="show"><@s.param name="teamId" value="${hotTeam.id}" /></@s.url>" title="${hotTeam.description}"><img src="${contextPath}${hotTeam.logo?default("")}" alt="${hotTeam.name}" height="48" width="48"></a></div>
          <div class="info" style="margin-left: 70px;">
            <a href="<@s.url namespace="/team" action="show"><@s.param name="teamId" value="${hotTeam.id}" /></@s.url>" title="${hotTeam.description}">${hotTeam.name}</a><br>
                <#if hotTeam.description?exists && (hotTeam.description?length > 20)>
                    ${hotTeam.description?substring(0, 20)} ...
                <#else>
                    ${hotTeam.description}
                </#if>
          </div>
        </div>
        </#if>
       </#list>
    </div>

<#--boxes end-->

  

<#include "../bottom.htm"/>
