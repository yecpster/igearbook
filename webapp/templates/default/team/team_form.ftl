<#include "/templates/default/header.htm" />
<@navHeader "team" />
<link href="${contextPath}/templates/${templateName}/styles/team.css?${startupTime}" media="screen" rel="stylesheet" type="text/css" />
<link href="${contextPath}/templates/${templateName}/styles/validation_style.css?${startupTime}" media="screen" rel="stylesheet" type="text/css" />
<link href="${contextPath}/templates/${templateName}/styles/tooltips.css?${startupTime}" media="screen" rel="stylesheet" type="text/css" />
<script type="text/javascript" src="${contextPath}/templates/${templateName}/js/validation/prototype_for_validation.js"></script>
<script type="text/javascript" src="${contextPath}/templates/${templateName}/js/validation/tooltips.js"></script>
<script type="text/javascript" src="${contextPath}/templates/${templateName}/js/validation/effects.js"></script>
<script type="text/javascript" src="${contextPath}/templates/${templateName}/js/validation/validation_cn.js"></script>

<@s.form namespace="/team" action="save" method="post" enctype="multipart/form-data" cssClass="required-validate">
<@s.if test="team!=null">
    <@s.hidden name="team.id" />
</@s.if>

<fieldset class="groups_new">
<@s.if test="team==null">
<h3>基本要求：</h3>
  <ul>
    <li>1.申请群组主题明确,简介翔实清晰。</li>
    <li>2.新申请的群组不能和已有群组内容重复,类似。</li>
  </ul>
</@s.if>
<@s.if test="team==null">
<h3>创建群组：</h3>
</@s.if>
<@s.else>
<h3>修改群组：</h3>
</@s.else>
<@s.if test="hasActionErrors()">
   <ul>
    <li><span class="gen"><font color="red"><@s.actionerror /></font></span></li>
  </ul>
</@s.if>
<ul>
    <li><label>群组名称</label>
        <#if !team?exists || session.isAdmin()>
            <@s.textfield cssClass="text required max-length-20" size="30" name="team.name" />
        <#else>
            <@s.textfield cssClass="text" size="30" name="team.name" disabled="true" />
        </#if>
    </li>
    <li><label>群组图标</label> 
        <@s.if test="team!=null">
            <img src="${contextPath}${team.logo?default("")}" height="48" width="48">
        </@s.if>
        <@s.file name="upload"  cssClass="validate-file-png-jpg-gif" />
    </li>
    <li><label>群组简介</label>
        <@s.textarea name="team.description" cols="30" rows="15" cssClass="required max-length-80" />
    </li>
</ul>
    <@s.submit cssClass="submit" value="更新" />
</fieldset>

<@s.token />
</@s.form>

<#include "/templates/default/bottom.htm" />
