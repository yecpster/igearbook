<#include "/templates/default/header.htm" />
<@navHeader "team" />
<link href="${contextPath}/templates/${templateName}/styles/team.css?${startupTime}" media="screen" rel="stylesheet" type="text/css" />


<div id="group_nav" class="main">
  <div class="crumbs">
    <a href="${contextPath}/team/list.action">群组首页</a>
    <span class="arrow">→</span>
    <a href="${contextPath}/team/show.action?teamId=${teamId}">${team.name?html}</a>
    <span class="arrow">→</span>
             修改群组公告
  </div>
  <div class="left" style="margin: 0 10px;">
    <div class="logo"><img src="${team.logo?default("")}" alt="${team.name}" height="48" width="48"></div>
  </div>
  <div>${team.description?default("")}</div>
</div>

<@s.form namespace="/team" action="saveAnnounce" method="post">
<@s.if test="team!=null">
    <@s.hidden name="team.id" />
</@s.if>


<fieldset class="groups_new">

<ul>
    <li><label>公告</label>
        <@s.textarea name="announcement" cols="30" rows="15"  />
    </li>
</ul>
    <@s.submit cssClass="submit" value="更新" />
</fieldset>

<@s.token />
</@s.form>
<#include "/templates/default/bottom.htm" />
