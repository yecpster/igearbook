<#include "/templates/default/header.htm" />
<#assign currentChannel="team" />
<link href="${contextPath}/templates/${templateName}/styles/team.css?${startupTime}" media="screen" rel="stylesheet" type="text/css" />


<@s.form namespace="/team" action="saveAnnounce" method="post">
<@s.if test="team!=null">
    <@s.hidden name="team.id" />
</@s.if>


<fieldset class="groups_new">
<h3>修改群组公告：</h3>

<ul>
    <li><label>群组名称</label>
        <@s.textfield cssClass="text" size="30" name="team.name" disabled="true" />
    </li>
    <li><label>公告</label>
        <@s.textarea name="announcement" cols="30" rows="15"  />
    </li>
</ul>
    <@s.submit cssClass="submit" value="更新" />
</fieldset>

<@s.token />
</@s.form>

<#include "/templates/default/bottom.htm" />
