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
              <div class="rank_info">${rankTeam_index + 1}.
              <a href="
              <#if rankTeam.uri?exists>
                <@s.url value="/${rankTeam.uri}" />
              <#else>
                <@s.url namespace="/team" action="show"><@s.param name="teamId" value="${rankTeam.id}" /></@s.url>
              </#if>
              " title="${rankTeam.description?default("")}">${rankTeam.name}</a><br>文章数：${rankTeam.totalPosts}</div>
              <div class="rank_logo"><div class="logo">
                    <a href="
                    <#if rankTeam.uri?exists>
                      <@s.url value="/${rankTeam.uri}" />
                    <#else>
                        <@s.url namespace="/team" action="show"><@s.param name="teamId" value="${rankTeam.id}" /></@s.url>
                    </#if>
                    " title="${rankTeam.description?default("")}"><img src="${rankTeam.logo?default("")}" alt="${rankTeam.name}" height="48" width="48"></a></div></div>
            </div>
        </#if>
        </#list>
    </div>

</div>
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
        <div class="groups_title"><span>最近更新</span> <#--<a href="#" class="more">更多文章</a>--></div>
        <#list recentTopics as recentTopic>
        <div class="group_topic clearfix">
            <h4><a href="${JForumContext.encodeURL("/posts/list/${recentTopic.id}")}" target="_blank">${recentTopic.title}</a></h4>
            <div style="float:right;">
                <div class="logo">
                <a href="
                <#if recentTopic.forum.uri?exists>
                   <@s.url value="/${recentTopic.forum.uri}" />
                <#else>
                    <@s.url namespace="/team" action="show"><@s.param name="teamId" value="${recentTopic.forum.id}" /></@s.url>
                </#if>
                    " target="_blank">
                    <img src="<@s.url value="${recentTopic.forum.logo}" />" alt="${recentTopic.forum.name}" title="${recentTopic.forum.description?default("")}" height="48" width="48">
                </a>
                </div>
            </div>
            <div>${recentTopic.firstPost.text?default("")} ...</div>
            <#--
            <div class="page_tags">
            
            </div>
            -->
            <div class="topic_info clearfix">by 
            <a href="${JForumContext.encodeURL("/user/profile/${recentTopic.postedBy.id}")}" target="_blank" title="${recentTopic.postedBy.username}">${recentTopic.postedBy.username}</a> ${recentTopic.time} 浏览 (${recentTopic.totalViews}) 
            <a href="${JForumContext.encodeURL("/posts/reply/${recentTopic.totalReplies}/${recentTopic.id}")}" target="_blank">回复 (${recentTopic.totalReplies})</a> 群组: 
            <a href="<@s.url namespace="/team" action="show"><@s.param name="teamId" value="${recentTopic.forum.id}" /></@s.url>" target="_blank">${recentTopic.forum.name}</a>
            </div>
        </div>
        </#list>
    </div>
    <div class="clearfix">
        <div class="groups_title"><span>推荐群组</span> <a href="<@s.url namespace="/team" action="all" />" class="more">更多群组</a></div>
            <div class="clearfix">
                <#list recommendTeams as team>
                <ul class="group">
                  <li class="logo"><div class="logo">
                  <a href="
                  <#if team.uri?exists>
                   <@s.url value="/${team.uri}" />
                  <#else>
                    <@s.url namespace="/team" action="show"><@s.param name="teamId" value="${team.id}" /></@s.url>
                  </#if>
                  " title="${team.description?default("")}"><img src="${team.logo?default("")}" alt="${team.name}" height="48" width="48"></a></div> </li>
                  <li class="clearfix"><a href="
                  <#if team.uri?exists>
                   <@s.url value="/${team.uri}" />
                  <#else>
                    <@s.url namespace="/team" action="show"><@s.param name="teamId" value="${team.id}" /></@s.url>
                  </#if>
                  " title="${team.description?default("")}"><strong>${team.name}</strong></a> (${team.totalTopics})</li>
                </ul>
                </#list>
            </div>
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
      <a href="<@s.url namespace="/team" action="myteam" />" class="my_group_link">我的群组</a>
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
    <div class="box">
      <h3>近期热门群组</h3>
      <#list hotTeams as hotTeam>
        <#if (hotTeam_index < 10)>
        <div class="clearfix hot_group">
          <div class="logo">
                <a href="
                <#if hotTeam.uri?exists>
                    <@s.url value="/${hotTeam.uri}" />
                <#else>
                    <@s.url namespace="/team" action="show"><@s.param name="teamId" value="${hotTeam.id}" /></@s.url>
                </#if>
                " title="${hotTeam.description}"><img src="${contextPath}${hotTeam.logo?default("")}" alt="${hotTeam.name}" height="48" width="48"></a></div>
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
