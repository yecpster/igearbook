<#include "/templates/default/header.htm"/>
<@navHeader "bbs" />

<link href="${contextPath}/templates/${templateName}/styles/validation_style.css?${startupTime}" media="screen" rel="stylesheet" type="text/css" />
<script type="text/javascript" src="${contextPath}/templates/${templateName}/js/validation/prototype_for_validation.js"></script>
<script type="text/javascript" src="${contextPath}/templates/${templateName}/js/validation/validation_cn.js"></script>
<script type="text/javascript" src="${contextPath}/templates/${templateName}/js/jquery.js?${startupTime}"></script>

<@s.form namespace="/url" action="save" method="post" enctype="multipart/form-data" onsubmit="return checkSubmit()" id="postFrom">
<table cellspacing="0" cellpadding="10" width="100%" align="center" border="0">
    <tr>
        <td class="bodyline">
            URL:<@s.textfield cssClass="text" size="30" name="url.url"  />
             Type:<@s.textfield cssClass="text" size="30" name="url.type" />
        <@s.submit cssClass="submit" value="更新" />
        </td>
    </tr>
</table>
<@s.token />
</@s.form>

<#include "/templates/default/bottom.htm" />
