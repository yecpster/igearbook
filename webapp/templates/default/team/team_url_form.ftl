<#include "/templates/default/header.htm" />
<@navHeader "team" />
<link href="${contextPath}/templates/${templateName}/styles/team.css?${startupTime}" media="screen" rel="stylesheet" type="text/css" />
<link href="${contextPath}/templates/${templateName}/styles/validation_style.css?${startupTime}" media="screen" rel="stylesheet" type="text/css" />
<link href="${contextPath}/templates/${templateName}/styles/tooltips.css?${startupTime}" media="screen" rel="stylesheet" type="text/css" />
<script type="text/javascript" src="${contextPath}/templates/${templateName}/js/validation/prototype_for_validation.js"></script>
<script type="text/javascript" src="${contextPath}/templates/${templateName}/js/validation/tooltips.js"></script>
<script type="text/javascript" src="${contextPath}/templates/${templateName}/js/validation/effects.js"></script>
<script type="text/javascript" src="${contextPath}/templates/${templateName}/js/validation/validation_cn.js"></script>

<@s.form namespace="/team" action="apply_url_save" method="post" enctype="multipart/form-data" cssClass="required-validate">
<@s.hidden name="team.id" />

<fieldset class="groups_new">
<h3申请个性域名：</h3>
<@s.if test="hasActionErrors()">
   <ul>
    <li><span class="gen"><font color="red"><@s.actionerror /></font></span></li>
  <ul>
    </@s.if>
<ul>
    <li><label>群组名称</label>
        <@s.textfield cssClass="text" size="30" name="team.name" disabled="true" />
    </li>
    <li><label>群组图标</label> 
         <img src="${contextPath}${team.logo?default("")}" height="48" width="48">
    </li>
    <li><label>个性域名</label>
        <@s.textfield cssClass="text" size="30" name="customUrl.url" cssClass="required min-length-5 max-length-25 validate-alphanumNMinus" />
    </li>
</ul>
    <@s.submit cssClass="submit" value="更新" />
</fieldset>

<@s.token />
</@s.form>

<#include "/templates/default/bottom.htm" />
