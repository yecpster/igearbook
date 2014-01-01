<#include "/templates/default/header.htm" />
<@navHeader "team" />


<table cellspacing="2" cellpadding="2" width="100%" border="0">
   <tr>
       <td valign="middle" align="left" colspan="${colspan?default("0")}">
           <span class="nav">
               <a class="nav" href="${contextPath}/team/list.action">群组首页</a> 
               &raquo; 
               <a class="nav" href="
               <#if team.uri?exists>
                    <@s.url value="/${team.uri}" />
                <#else>
                    ${contextPath}/team/show.action?teamId=${teamId}
                </#if>    
                ">${team.name?html}</a>
               &raquo;   管理会员
            </span>
       </td>
   </tr>
</table>


<table cellspacing="1" cellpadding="3" width="100%" border="0">
    <#if warns?exists>
        <#assign message = "" />
        <#list warns as w>
           <#assign message = message + w + "<br />"/>
        </#list>
    </#if>
    <#if message?exists && (message?length > 0)>
    <tr>
        <td class="row2" align="center" colspan="3">
           <span class="gens">
           <font color="<#if warns?exists>red<#else>green</#if>">${message}</font>
           </span>
        </td>
     </tr>
     </#if>
</table>


<table class="forumline" cellspacing="1" cellpadding="3" width="100%" border="0">
    <tr>
        <th class="thhead" valign="middle" colspan="6" height="25">管理员：</th>
    </tr>
    <tr>
        <td class="row2" width="10%" align="center"><span class="gen"><b>${I18n.getMessage("User.id")}</b></span></td>
        <td class="row2"><span class="gen"><b>${I18n.getMessage("User.username")}</b></span></td>
        <td class="row2" align="center"><span class="gen"><b>操作</b></span></td>
    </tr>

    <#list moderators as moderator>
        <tr>
            <td class="row1" align="center"><span class="gen">${moderator.id}</span></td>
            <td class="row1"><span class="gen">${moderator.username}</span></td>
            <td class="row1" align="center" width="150px">
                 <a href="<@s.url namespace="/team" action="cancelAdmin"><@s.param name="teamId" value="${teamId}" /><@s.param name="userId" value="${moderator.id}" /></@s.url>">
                                                             取消管理员
                 </a></td>
        </tr>
    </#list>

</table>

<br />
<br />

<table class="forumline" cellspacing="1" cellpadding="3" width="100%" border="0">
    <tr>
        <th class="thhead" valign="middle" colspan="4" height="25">会员：</th>
    </tr>
    <@s.form namespace="/team" action="banPostUsers" method="get">
    <input type="hidden" name="start" value="${start}" />
    <input type="hidden" name="teamId" value="${teamId}" />
    <tr>
        <td class="row2" width="10%" align="center"><span class="gen"><b>${I18n.getMessage("User.id")}</b></span></td>
        <td class="row2" ><span class="gen"><b>${I18n.getMessage("User.username")}</b></span></td>
        <td class="row2" align="center" colspan="2"><span class="gen"><b>操作</b></span></td>
    </tr>

    <#list users as user>
        <tr>
            <td class="row1" align="center"><span class="gen">${user.id}</span></td>
            <td class="row1"><span class="gen">${user.username}</span></td>
            <td class="row1" align="center" width="150px">
                <a href="<@s.url namespace="/team" action="setAdmin"><@s.param name="teamId" value="${teamId}" /><@s.param name="userId" value="${user.id}" /></@s.url>">
                                                             设置为管理员
                 </a>
            </td>
            <td class="row1" align="center" width="70px">禁言<input type="checkbox" name="banPostUserIds" value="${user.id}" /></td>
        </tr>
    </#list>

    <tr align="center">
        <td class="catbottom">
            &nbsp;
        </td>
        <td class="catbottom" height="28">
        <#--
            <span class="gensmall">
            ${I18n.getMessage("Insert")}
            <select name="addByType">
                <option value="username" selected="selected">${I18n.getMessage("User.username")}</option>
                <option value="id">${I18n.getMessage("User.id")}</option>
            </select>
            <input type="text" name="userValue" value="" />
        -->
        </td>
       <td class="catbottom" height="28" colspan="2">
            <input class="mainoption" type="submit" value="${I18n.getMessage("Forums.Form.ClickToUpdate")}" name="submit" />
        </td>
    </tr>
</table>
</@s.form>

<table class="forumline" cellspacing="1" cellpadding="3" width="100%" border="0">
    <tr>
        <th class="thhead" valign="middle" colspan="4" height="25">禁言会员：</th>
    </tr>
    <tr>
        <td class="row2" width="10%" align="center"><span class="gen"><b>${I18n.getMessage("User.id")}</b></span></td>
        <td class="row2" ><span class="gen"><b>${I18n.getMessage("User.username")}</b></span></td>
        <td class="row2" align="center" width="150px"><span class="gen"><b>操作</b></span></td>
    </tr>

    <#list banUsers as user>
        <tr>
            <td class="row1" align="center"><span class="gen">${user.id}</span></td>
            <td class="row1"><span class="gen">${user.username}</span></td>
            <td class="row1" align="center" width="150px">
                <a href="<@s.url namespace="/team" action="unbanUser"><@s.param name="teamId" value="${teamId}" /><@s.param name="userId" value="${user.id}" /></@s.url>">
                                                            取消禁言
                 </a>
            </td>
        </tr>
    </#list>

</table>

<#include "../bottom.htm"/>