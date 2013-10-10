<#include "/templates/default/header.htm" />
<#assign currentChannel="team" />
<link href="${contextPath}/templates/${templateName}/styles/team.css?${startupTime}" media="screen" rel="stylesheet" type="text/css" />


<@s.form namespace="/team" action="save" method="post" enctype="multipart/form-data">
<@s.if test="team!=null">
    <@s.hidden name="team.id" />
</@s.if>


<fieldset class="groups_new">
<h3>基本要求：</h3>
  <ul>
    <li>1.申请群组主题明确,简介翔实清晰。</li>
    <li>2.新申请的群组不能和已有群组内容重复,类似。</li>
  <ul>
<h3>创建群组：</h3>
<ul>
    <li><label>群组名称</label>
        <@s.if test="team!=null">
            <@s.textfield cssClass="text" size="30" name="team.name" disabled="true" />
        </@s.if>
        <@s.else>
            <@s.textfield cssClass="text" size="30" name="team.name"  />
        </@s.else>
    </li>
    <li><label>群组图标</label> 
        <@s.if test="team!=null">
            <img src="${team.logo?default("")}" height="48" width="48">
        </@s.if>
        <@s.file name="upload" />
    </li>
    <li><label>群组简介</label>
        <@s.textarea name="team.description" cols="30" rows="15"  />
    </li>
</ul>
    <@s.submit cssClass="submit" value="更新" />
</fieldset>

<@s.token />
</@s.form>

<#include "/templates/default/bottom.htm" />
