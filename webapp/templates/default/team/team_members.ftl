<#include "/templates/default/header.htm" />
<@navHeader "team" />
<#import "/templates/macros/paginationStruts.ftl" as pagination/>

<link href="${contextPath}/templates/${templateName}/styles/team.css?${startupTime}" media="screen" rel="stylesheet" type="text/css" />
<script type="text/javascript" src="${contextPath}/templates/${templateName}/js/paginationStruts.js?${startupTime}"></script>


<div id="group_nav" class="main" style="height:auto">
  <div class="crumbs">
    <a href="${contextPath}/team/list.action">群组首页</a>
    <span class="arrow">→</span>
    ${team.name?html}
    <span class="arrow">→</span>
                成员列表
  </div>
</div>
  <div class="main">
    <table>
      <tr>
        <td>
        <#list pgData.list as user>
        <div class="latest_member" style="width:100px; height:80px;">
          <div class="logo">
              <a href="${JForumContext.encodeURL("/user/profile/${user.id}")}" target="_blank">
                <#if (user.avatar?exists && user.avatar?length > 0)>
                    <#if user.isExternalAvatar() || user.avatar.startsWith("http://")>
                        <#if avatarAllowExternalUrl>
                            <img class="logo" src="${user.avatar?html}" alt="[Avatar]" />
                        </#if>
                    <#else>
                        <img class="logo" src="${contextPath}/images/avatar/${user.avatar}" alt="[Avatar]" />
                    </#if>
                <#else>
                    <img class="logo" src="${contextPath}/images/team/photo_not_available.png" alt="[Avatar]" />
                </#if>
              </a>
          </div>
          <span><a href="${JForumContext.encodeURL("/user/profile/${user.id}")}" target="_blank" title="${user.username}">${user.username}</a></span>
        </div>
      </#list>
        </td>
      </tr>
      <tr>
        <td>
        <@pagination.doPagination "/team", "members", pgData />
        </td>
      </tr>
    </table>
  </div>

</div>
<#include "/templates/default/bottom.htm" />
