<#include "/templates/default/header.htm" />
<@navHeader />
<a name="top" id="top"></a>
<table width="100%" cellspacing="0" cellpadding="10" border="0" align="center">
	<tr>
		<td class="bodyline">
			<br />
			<table width="100%" cellspacing="2" cellpadding="2" border="0" align="center">
				<tr>
					<td align="left" class="nav"><a class="nav" href="${JForumContext.encodeURL("/forums/list")}">${I18n.getMessage("ForumListing.forumIndex")}</a></td>
				</tr>
			</table>
  
			<table class="forumline" width="100%" cellspacing="1" cellpadding="4" border="0">
				<tr>
					<th class="thhead" height="25"><b>${I18n.getMessage("Information")}</b></th>
				</tr>
				
				<tr>
					<td class="row1">
						<table width="100%" cellspacing="0" cellpadding="1" border="0">
							<tr>
								<td>&nbsp;</td>
							</tr>
	  
							<tr>
								<td align="center"><div class="gen">${I18n.getMessage("Moderation.Denied")}</div></td>
							</tr>
				  
							<tr>
								<td>&nbsp;</td>
							</tr>

						</table>
					</td>
				</tr>
			</table>
		</td>
	</tr>
</table>

<#include "/templates/default/bottom.htm" />