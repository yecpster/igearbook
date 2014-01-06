<#include "/templates/default/header.htm" />
<@navHeader />
<form action="${JForumContext.encodeURL("/jforum")}" method="post" name="loginform" id="loginform" accept-charset="${encoding}">
  <input type="hidden" name="module" value="user" />
  <input type="hidden" name="action" value="validateLogin" />
  
  <#if returnPath?exists>
  	<input type="hidden" name="returnPath" value="${returnPath?html}" />
  </#if>
  
<table cellspacing="2" cellpadding="2" width="100%" align="center" border="0">
	<tr>
		<td class="nav" align="left"><a class="nav" href="${JForumContext.encodeURL("/forums/list")}">${I18n.getMessage("ForumListing.forumIndex")}</a></td>
	</tr>
</table>

<table class="forumline" cellspacing="1" cellpadding="4" width="100%" align="center" border="0">
	<tr>
		<th class="thhead" nowrap="nowrap" height="25">${I18n.getMessage("Login.enterUsername")}</th>
	</tr>

	<tr>
		<td class="row1">
			<table cellspacing="1" cellpadding="3" width="100%" border="0">
				<tr>
					<td align="center" colspan="3">&nbsp;</td>
				</tr>
				
				<#if invalidLogin?exists>
				<tr>
					<td align="center" width="100%" colspan="3">
						<span class="gen" id="invalidlogin">
  							<font color="red">${I18n.getMessage("Login.invalidLogin")}</font>
  						</span>
  					</td>
  				</tr>
				</#if>
				
				<tr>
					<td align="right" width="45%"><span class="gen">${I18n.getMessage("Login.user")}:</span></td>
					<td width="205px"><input class="post" maxlength="40" size="25" name="username" type="text"/> </td>
					<td align="left">
					<#if autoLoginEnabled>
                        <span style="font-size: 13px;"><input type="checkbox" id="autologin" name="autologin" /><label for="autologin">${I18n.getMessage("Login.autoLogon")}</a></span>
                    </#if>
                    </td>
				</tr>
				
				<tr>
					<td align="right"><span class="gen">${I18n.getMessage("Login.password")}:</span></td>
					<td><input class="post" type="password" maxlength="25" size="25" name="password" /> </td>
					<td align="left">
					   <input type="hidden" name="redirect" />
                        <input class="submitBig" type="submit" value="&nbsp;&nbsp;&nbsp;${I18n.getMessage("Login.enter")}&nbsp;&nbsp;&nbsp;" name="login" />
                    </td>
				</tr>
                    <tr>
                        <td align="right" width="45%">
                        <span style="font-size: 13px;">其他帐号登录</span>
                        </td>
                        <td align="left" colspan="2">
                        <a href="${contextPath}/qqapi/login.action" class="qqlogin" title="QQ登录">
                        <img alt="QQ登录" src="${contextPath}/templates/${templateName}/images/QQ_Connect_logo_3.png" width="120" height="24" />
                        </a>
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
</form>
<#include "/templates/default/bottom.htm"/>