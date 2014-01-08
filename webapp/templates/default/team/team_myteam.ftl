<#include "../header.htm"/>
<@navHeader "team" />
<link href="${contextPath}/templates/${templateName}/styles/teams.css?${startupTime}" media="screen" rel="stylesheet" type="text/css" />

<#--side-->
<#include "team_rank.ftl"/>
<#--side end-->

<#--index_main-->
<div id="index_main">
    <#--
      <div class="clearfix">
      <div class="groups_title"><span>群组最新讨论</span> <a href="http://www.iteye.com/groups/all_topics" class="more">更多讨论</a></div>
      <table class="grid">
        <thead>
          <tr>
            <td style="width:300px;">话题</td>
            <td>群组</td>
            <td>最后回复</td>
          </tr>
        </thead>
        <tbody>
            <tr onmouseover="$(this).addClassName(&#39;mouse_over&#39;);" onmouseout="$(this).removeClassName(&#39;mouse_over&#39;);" class="">
              <td>
                <a href="http://jquery.group.iteye.com/group/topic/38655" title="推荐几款不错的jQuery特效" target="_blank">推荐几款不错的jQuery特效</a>
                
              </td>
              <td class="group"><a href="http://jquery.group.iteye.com/">Jquery</a></td>
              <td class="date">2013-09-23 <a href="http://jquery.group.iteye.com/group/topic/38655/post/252691" target="_blank"><img alt="浏览最新的文章" src="${contextPath}/templates/${templateName}/images/icon_last_post.gif?${startupTime}"></a></td>
            </tr>
          
            <tr onmouseover="$(this).addClassName(&#39;mouse_over&#39;);" onmouseout="$(this).removeClassName(&#39;mouse_over&#39;);" class="">
              <td>
                <a href="http://suanfa.group.iteye.com/group/topic/38648" title="TAOCP第一章 - 集合论表述算法的一些困惑" target="_blank">TAOCP第一章 - 集合论表述算法的一些困惑</a>
                
              </td>
              <td class="group"><a href="http://suanfa.group.iteye.com/">算法~</a></td>
              <td class="date">2013-09-13 <a href="http://suanfa.group.iteye.com/group/topic/38648/post/252235" target="_blank"><img alt="浏览最新的文章" src="${contextPath}/templates/${templateName}/images/icon_last_post.gif?${startupTime}"></a></td>
            </tr>
          
        </tbody>
      </table>
    </div>
-->
    
    <div class="clearfix">
        <#if ownerTeams?has_content>
        <div class="groups_title"><span>我创建的群组</span></div>
            <div class="clearfix">
                <#list ownerTeams as team>
                <ul class="group">
                  <li class="logo"><div class="logo">
                        <a href="
                         <#if team.uri?exists>
                             <@s.url value="/${team.uri}" />
                         <#else>
                            <@s.url value="/team/show/${team.id}" />
                        </#if>
                        " title="${team.description?default("")}"><img src="${team.logo?default("")}" alt="${team.name}" height="48" width="48"></a></div> </li>
                  <li class="clearfix"><a href="
                        <#if team.uri?exists>
                             <@s.url value="/${team.uri}" />
                         <#else>
                            <@s.url value="/team/show/${team.id}" />
                         </#if>
                        " title="${team.description?default("")}"><strong>${team.name}</strong></a> (${team.totalTopics})</li>
                </ul>
                </#list>
            </div>
        
        </#if>
        <#if moderatorTeams?has_content>
        <div class="groups_title"><span>我管理的群组</span></div>
            <div class="clearfix">
                <#list moderatorTeams as team>
                <ul class="group">
                  <li class="logo"><div class="logo"><a href="
                  <#if team.uri?exists>
                      <@s.url value="/${team.uri}" />
                  <#else>
                    <@s.url value="/team/show/${team.id}" />
                  </#if>
                    " title="${team.description?default("")}"><img src="${team.logo?default("")}" alt="${team.name}" height="48" width="48"></a></div> </li>
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
            </div>
        </#if>
        <#if userTeams?has_content>
        <div class="groups_title"><span>我加入的群组</span></div>
            <div class="clearfix">
                <#list userTeams as team>
                <ul class="group">
                  <li class="logo"><div class="logo"><a href="
                  <#if team.uri?exists>
                      <@s.url value="/${team.uri}" />
                  <#else>
                      <@s.url value="/team/show/${team.id}" />
                  </#if>
                  " title="${team.description?default("")}"><img src="${team.logo?default("")}" alt="${team.name}" height="48" width="48"></a></div> </li>
                  <li class="clearfix"><a href="
                  <#if team.uri?exists>
                      <@s.url value="/${team.uri}" />
                  <#else>
                      <@s.url value="/team/show/${team.id}" />
                  </#if>
                    " title="${team.description?default("")}"><strong>${team.name}</strong></a> (${team.totalTopics})</li>
                </ul>
                </#list>
            </div>
        </#if>
        </div>
    </div>
    
    


</div>
<#--index_main end-->
        
        <div id="local">
    <#--
    <div id="search_box">
      <form action="#" method="get">
        <input class="text" id="query" name="query" size="15" style="width: 140px;" type="text">
        <input type="submit" value="搜索群组" class="submit" style="width:70px;">
      </form>
    </div>
    -->
    <p> 
      <#if canCreateTeam>
        <a href="<@s.url namespace="/team" action="insert" />" class="new_group_link">创建群组</a>  &nbsp;&nbsp;
      </#if>
  </p>
<#--boxes-->
    <#--
    <div class="box">
      <h3>群组热门讨论帖</h3>
      <ul>
        <li><span title="浏览次数">14967</span>
            <a href="http://heart.group.iteye.com/group/topic/35589" title="IT女找男朋友" target="_blank">IT女找男朋友</a>
        </li>
      </ul>
    </div>

    <div class="box">
      <h3>近期活动栏目</h3>
      <ul>
        <li>12月22日 <a href="http://openstack.group.iteye.com/group/events/313">OpenStack中国行（湖北武汉）  </a></li>
      </ul>
    </div>
    -->
    <#include "team_hot.ftl"/>
<#--boxes end-->

  

<#include "../bottom.htm"/>
