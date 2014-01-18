<#include "/templates/default/header.htm" />
<@navHeader "userList" />
<link href="${contextPath}/templates/${templateName}/styles/validation_style.css?${startupTime}" media="screen" rel="stylesheet" type="text/css" />
<script type="text/javascript" src="${contextPath}/templates/${templateName}/js/validation/prototype_for_validation.js"></script>
<script type="text/javascript" src="${contextPath}/templates/${templateName}/js/validation/validation_cn.js"></script>
<script type="text/javascript" src="${contextPath}/templates/${templateName}/js/jquery.js?${startupTime}"></script>

<@s.form namespace="/qqapi" method="post" onsubmit="return checkSubmit()" id="postFrom">
<table class="forumline" cellspacing="1" cellpadding="4" width="100%" align="center" border="0">
    <tr>
        <th class="thhead" nowrap="nowrap" height="25">登录成功</th>
    </tr>
    <@s.if test="hasActionErrors()">
    <tr>
       <td align="center">
       <span class="gen">
           <font color="red"><@s.actionerror /></font>
       </span>
       </td>
    </tr>
    </@s.if>
    <tr>
        <td class="row1">
            <table cellspacing="1" cellpadding="3" width="100%" border="0">
                <tr>
                    <td align="center" colspan="3">
                    
                    <img src="${user.avatar?default("")}" height="48" width="48">&nbsp;&nbsp;欢迎你，来自&nbsp;&nbsp;<img alt="QQ登录" src="${contextPath}/templates/${templateName}/images/QQ_Connect_logo_1.png" width="16" height="16" />
                    QQ登录&nbsp;&nbsp;的 &nbsp;&nbsp;${user.username}
                    </td>
                </tr>
                <tr align="center">
                    <td colspan="3">&nbsp;</td>
                </tr>
                <tr>
                    <td align="center" colspan="3"><span class="gen">现在你可以绑定网站帐号或者直接<a href="<@s.url value="/" />">进入网站</a> </span>
                    </td>
                </tr>
                <tr>
                    <td align="right"  colspan="2">
                    <input type="radio" name="accountType" value="hasAccount" id="accountTypeYes" <#if !accountType?exists || accountType=="hasAccount"> checked="checked" </#if> />
                    <label for="accountTypeYes">已经有装备网帐号</label>
                    <input type="radio" name="accountType" value="noAccount" id="accountTypeNo" <#if accountType?exists && accountType=="noAccount"> checked="checked" </#if> />
                    <label for="accountTypeNo">一步注册装备网帐号</label>
                    </td>
                    <td align="left">&nbsp;</td>
                </tr>
                
                <tr>
                    <td align="right" width="45%"><span class="gen">${I18n.getMessage("Login.user")}: * </span></td>
                    <td width="205px">
                        <@s.textfield cssClass="post required min-length-3 max-length-15" maxlength="40" size="25" name="user.username" />
                     </td>
                    <td align="left">&nbsp;
                    </td>
                </tr>
                
                <tr>
                    <td align="right"><span class="gen">${I18n.getMessage("Login.password")}: * </span></td>
                    <td width="205px">
                        <@s.password id="password" cssClass="post required" maxlength="25" size="25" name="user.password" />
                    </td>
                    <td align="left">
                        <@s.submit id="bind" action="bind" cssClass="submitBig" value="   绑定   " />
                    </td>
                </tr>
                <tr>
                    <td colspan="3">
                    <div id="confirmTR" style="display:none;">
                        <table width="100%" border="0">
                        <tr>
                            <td align="right" width="45%"><span class="gen">${I18n.getMessage("User.confirmPassword")}: * </span></td>
                            <td width="210px"><input class="post required equals-password" type="password" maxlength="25" size="25" name="password_confirm" /> </td>
                            <td align="left">
                                <@s.submit action="reg" cssClass="submitBig" value="   注册   " />
                            </td>
                        </tr>
                        </table>
                    </div>
                    </td>
                 </tr>
                
                
                    <tr align="center">
                        <td colspan="3">&nbsp;</td>
                    </tr>
                    <tr align="center">
                        <td colspan="3">&nbsp;</td>
                    </tr>

                <tr align="center">
                    <td colspan="3" class="gensmall">
                        <a href="${JForumContext.encodeURL("/user/lostPassword")}">${I18n.getMessage("Login.lostPassword")}</a>
                         | 
                        <a href="${JForumContext.encodeURL("/user/activateManual")}">${I18n.getMessage("ActivateAccount.activate")}</a>
                    </td>
                </tr>
            </table>
        </td>
    </tr>
</table>
<@s.token />
</@s.form>
<script>

$(document).ready(function() {
     var valid = new Validation('postFrom', {immediate : true});
     $("#password").bind('paste',function(e){
     e.preventDefault();
     }
    );
    switchAccountType();
    $("input[type='radio'][name='accountType']").change(
        switchAccountType
    );
});
function switchAccountType(){
        var accountType = $("input[type='radio'][name='accountType']:checked").val();
        if ("hasAccount"==accountType){
            $("#confirmTR").slideUp("slow");
            $("#bind").show("slow");
        } else{
            $("#bind").hide("slow");
            $("#confirmTR").slideDown("slow");
        }
    }

</script>

<#include "/templates/default/bottom.htm" />
