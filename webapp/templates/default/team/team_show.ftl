<#include "/templates/default/header.htm" />
<@navHeader "team" />
<#import "/templates/macros/pagination.ftl" as pagination>
<#import "/templates/macros/presentation.ftl" as presentation/>
<link href="${contextPath}/templates/${templateName}/styles/team.css?${startupTime}" media="screen" rel="stylesheet" type="text/css" />
<script type="text/javascript" src="${contextPath}/templates/${templateName}/js/pagination.js?${startupTime}"></script>
<script type="text/javascript" src="${contextPath}/templates/${templateName}/js/jquery.js?${startupTime}"></script>
<#if logged>
	<script type="text/javascript" src="${contextPath}/templates/${templateName}/js/watch.js?${startupTime}"></script>
</#if>

<#if moderator>
	<script type="text/JavaScript" src="${JForumContext.encodeURL("/js/list/moderation")}"></script>
</#if>

<#if logged && isBanUser>
<table cellspacing="1" cellpadding="3" width="100%" border="0">
    <tr>
        <td class="row2" align="center">
           <span class="gens">
            <font color="red">你已被该群组禁言！</font>
           </span>
        </td>
     </tr>
</table>
</#if>

<div id="group_nav" class="main">
  <div class="crumbs">
    <a href="${contextPath}/team/list.action">群组首页</a>
    <span class="arrow">→</span>
    ${team.name?html}
    <span id="membership">
    <#if logged && !isTeamMember&& !isBanUser>
      <a href="<@s.url namespace="/team" action="join"><@s.param name="teamId" value="${team.id}" /></@s.url>">加入此群组</a>
    </#if>
    <#if logged && moderator || session.isAdmin()>
      <a href="<@s.url namespace="/team" action="editAnnounce"><@s.param name="teamId" value="${team.id}" /></@s.url>">修改公告</a> &nbsp;&nbsp;|&nbsp;&nbsp;
      <a href="<@s.url namespace="/team" action="edit"><@s.param name="teamId" value="${team.id}" /></@s.url>">修改群组资料</a>
    </#if>
    </span>
  </div>
  <div class="left" style="margin: 0 10px;">
    <div class="logo"><img src="${contextPath}${team.logo?default("")}" alt="${team.name}" height="48" width="48" /></div>
  </div>
  <div>${team.description?default("")}</div>
</div>


<#if announcement?exists>
<div id="announcements" class="main">
  <strong>公告</strong>
  <div>${announcement?default("")}</div>
</div>
</#if>


  <div class="main">
    <div class="groups_title">
      <span>讨论</span>
      <#if !replyOnly>
      <div class="more">
        <img src="${contextPath}/images/team/icon_plus.gif" alt="发表新文章" />
        <a href="${JForumContext.encodeURL("/jforum${extension}?module=posts&amp;action=insert&amp;forum_id=${team.id}", "")}">发表新帖</a>
      </div>
      </#if>
    </div>
    <#--
    <div class="nav"><a href="">精华区</a> <a href="">全部讨论</a></div>
    -->
    <table class="grid">
      <thead>
        <tr>
          <td>${I18n.getMessage("ForumIndex.topics")}</td>
          <td style="width:110px;">${I18n.getMessage("ForumIndex.author")}</td>
          <td style="width:70px;">${I18n.getMessage("ForumIndex.answers")}/${I18n.getMessage("ForumIndex.views")}</td>
          <td style="width:140px;">${I18n.getMessage("ForumIndex.lastMessage")}</td>
        </tr>
      </thead>
      <tbody>
      <#list topics as topic>
        <tr onmouseover="$(this).addClassName(&#39;mouse_over&#39;);" onmouseout="$(this).removeClassName(&#39;mouse_over&#39;);" class="">
          <td>
            <#if topic.type == TOPIC_STICKY>
                <img alt="置顶" src="${contextPath}/images/team/top.gif" title="置顶">
            <#elseif topic.type == TOPIC_GOOD>
                <img alt="精华" src="${contextPath}/images/team/good.gif" title="精华">
            <#elseif topic.type == (TOPIC_GOOD+TOPIC_STICKY)>
                <img alt="置顶" src="${contextPath}/images/team/top.gif" title="置顶">
                <img alt="精华" src="${contextPath}/images/team/good.gif" title="精华">
            </#if>
            
            <a href="${JForumContext.encodeURL("/posts/list/${topic.id}")}" title="${topic.title?html}">${topic.title?html}</a>
            <#if topic.paginate>
              <div class="topic_page"><@pagination.littlePostPagination topic.id, postsPerPage, topic.totalReplies/>
              </div>
            </#if>
            
          </td>
          <td><a href="${JForumContext.encodeURL("/user/profile/${topic.postedBy.id}")}" target="_blank" title="${topic.postedBy.username}">${topic.postedBy.username}</a></td>
          <td>${topic.totalReplies} / ${topic.totalViews}</td>
          <td>
          <#if (topic.lastPostTime?length > 0)>
            ${topic.lastPostTime}
            <#assign startPage = ""/>
            <#if (topic.totalReplies + 1 > postsPerPage?number)>
                <#assign startPage = ((topic.totalReplies / postsPerPage?number)?int * postsPerPage?number) +"/"/>
            </#if>
            <a href="${JForumContext.encodeURL("/posts/list/${startPage}${topic.id}")}#${topic.lastPostId}"><img alt="浏览最新的文章" src="${contextPath}/templates/${templateName}/images/icon_latest_reply.gif"></a>
          </#if>
          </td>
        </tr>
        </#list>
        <tr>
          <td colspan="4" style="text-align:right;">
          <#if logged && moderator || session.isAdmin()>
          &gt; <a href="${JForumContext.encodeURL("/forums/moderation/${team.id}")}">管理论坛</a>&nbsp;&nbsp;&nbsp;&nbsp; 
          </#if>
          &gt; <a href="${JForumContext.encodeURL("/forums/show/${team.id}")}">更多讨论</a></td>
        </tr>
      </tbody>
    </table>
  </div>

</div>


<div id="local">


<div id="search_box">
  <form accept-charset="${encoding}" action="${JForumContext.encodeURL("/jforum")}" method="get" id="formSearch" name="formSearch">
    <input type="hidden" name="module" value="search"/>
    <input type="hidden" name="action" value="search"/>
    <input type="hidden" name="channel" value="team"/>
    <input type="hidden" name="forum" value="${team.id}" />
    <input type="hidden" name="match_type" value="all" />
    
    <input class="text" name="search_keywords" style="width: 180px;" type="text" onblur="if (this.value == '') this.value = '搜索这个群组...';" onclick="if (this.value == '搜索这个群组...') this.value = '';" value="搜索这个群组..." />
    <input type="submit" value="${I18n.getMessage("ForumBase.search")}" class="submit">
  </form>
</div>


<div id="group_detail">
      <h3>小组成员 ${totalUsers} 人<#-- 浏览6926847次-->
          <#if logged && isTeamOwner>
            <a href="<@s.url namespace="/team" action="manageUser"><@s.param name="teamId" value="${team.id}" /></@s.url>">管理会员</a>
          </#if>
      </h3>
      <div>
        群主: <a href="${JForumContext.encodeURL("/user/profile/${teanOwner.id}")}" target="_blank" title="${teanOwner.username}">${teanOwner.username}</a><br>
   <#if moderators?has_content>
        管理员:  <#list moderators as moderator>
                <a href="${JForumContext.encodeURL("/user/profile/${moderator.id}")}" target="_blank" title="${moderator.username}">${moderator.username}</a> 
            </#list>
   </#if>
      </div>
    </div>
    
    
    <div class="more"><a href="<@s.url namespace="/team" action="members"><@s.param name="teamId" value="${team.id}" /></@s.url>">&gt;&gt;更多成员</a></div>
    <div id="latest_members">
      <h3>群组成员</h3>
      
      <#list users as user>
        <div class="latest_member">
          <div class="logo">
              <a href="${JForumContext.encodeURL("/user/profile/${user.id}")}" target="_blank">
                <#if (user.avatar?exists && user.avatar?length > 0)>
                    <#if user.isExternalAvatar() || user.avatar.startsWith("http://")>
                        <#if avatarAllowExternalUrl>
                            <img class="logo" src="${user.avatar?html}" alt="Avatar of ${user.username}" />
                        </#if>
                    <#else>
                        <img class="logo" src="${contextPath}/images/avatar/${user.avatar}" alt="Avatar of ${user.username}" />
                    </#if>
                <#else>
                    <img class="logo" src="${contextPath}/images/team/photo_not_available.png" alt="Avatar of ${user.username}" />
                </#if>
              </a>
          </div>
          <span><a href="${JForumContext.encodeURL("/user/profile/${user.id}")}" target="_blank" title="${user.username}">${user.username}</a></span>
        </div>
      </#list>
      
    </div>
   <#--
    <div class="calendar">
    <div class="calendar_bar">群组活动表</div>
    <div class="calendar_time">
    
    <div class="clear"></div>
    </div>
    </div>
   -->
   <#--
  <div id="groups_right">
    <h3>群组活动相册</h3>
    <div class="groups_photo">
      
        <dl>
          <dd>
            <a href="http://pcdiy.group.iteye.com/group/events/103/pictures/105442"><img src="./电脑DIY - ITeye技术社区_files/341a42f4-80c2-371f-acc9-2fe5796b22ab-thumb.jpg"></a>
          </dd>
        </dl>
      
        <dl>
          <dd>
            <a href="http://pcdiy.group.iteye.com/group/events/103/pictures/97434"><img src="./电脑DIY - ITeye技术社区_files/ace27793-a13a-364b-93a2-030e9362dc14-thumb.jpg"></a>
          </dd>
        </dl>
      
        <dl>
          <dd>
            <a href="http://pcdiy.group.iteye.com/group/events/103/pictures/91968"><img src="./电脑DIY - ITeye技术社区_files/46264c03-7a10-3541-9c5a-6270ee40c777-thumb.jpg"></a>
          </dd>
        </dl>
      
        <dl>
          <dd>
            <a href="http://pcdiy.group.iteye.com/group/events/103/pictures/84051"><img src="./电脑DIY - ITeye技术社区_files/206c4faa-b24d-3e4c-b7ec-9c3f73dadc0e-thumb.jpg"></a>
          </dd>
        </dl>
      
    </div>
    <div class="clear"></div>
  </div>

-->
    <#--
      <div id="group_links">
        <h3>群组链接</h3>
        <ul>
          
            <li><a href="" title="美女PP&amp;&amp;PLMM" target="_blank" rel="nofollow">美女PP&amp;&amp;PLMM</a></li>
          
        </ul>
      </div>
     -->
    <#if customUrl?exists>
    <div>
      <h3>群组信息</h3>
      <ul>
        <li>个性域名 <a href="<@s.url value="/${customUrl}" />">www.igearbook.com/${customUrl}</a></li>
       <#-- <li>创建于 2008-09-23</li>-->
      </ul>
    </div>
    <#elseif isTeamOwner>
    <div>
      <h3>群组信息</h3>
      <ul>
        <li>个性域名 <a href="<@s.url namespace="/team" action="apply_url"><@s.param name="teamId" value="${team.id}" /></@s.url>">点这里注册一个！</a></li>
      </ul>
    </div>
    </#if>

<#include "/templates/default/bottom.htm" />
