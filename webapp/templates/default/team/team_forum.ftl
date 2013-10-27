<#include "/templates/default/header.htm" />
<#assign currentChannel="team" />
<#import "/templates/macros/pagination.ftl" as pagination>
<#import "/templates/macros/presentation.ftl" as presentation/>

<script type="text/javascript" src="${contextPath}/templates/${templateName}/js/pagination.js?${startupTime}"></script>

<#if logged>
	<script type="text/javascript" src="${contextPath}/templates/${templateName}/js/watch.js?${startupTime}"></script>
</#if>

<#if moderator>
	<script type="text/JavaScript" src="${JForumContext.encodeURL("/js/list/moderation")}"></script>
</#if>


<table cellspacing="0" cellpadding="10" width="100%" align="center" border="0">
	<tr>
		<td class="bodyline" valign="top">
			<table cellspacing="2" cellpadding="2" width="100%" align="center">
				<tr>
					<td valign="bottom" align="left" colspan="2">
					   <span class="nav">
                            <a class="nav" href="${contextPath}/team/list.action">群组首页</a> 
                            &raquo; 
                            <a class="nav" href="<@s.url namespace="/team" action="show"><@s.param name="teamId" value="${teamId}" /></@s.url>">${team.name?html}</a>
                            &raquo; 群组论坛
                        </span>

						<#if rssEnabled>
							<a href="${JForumContext.encodeURL("/rss/forumTopics/${team.id}")}"><img src="${contextPath}/templates/${templateName}/images/xml_button.gif" border="0" alt="[XML]" /></a>
							<br />
						</#if>
					</td>

					<td valign="middle"  nowrap="nowrap" align="right" class="gensmall">
						<#if moderator>
							<br />

							<#if openModeration?default(false)>
								<a href="<@s.url namespace="/team" action="show"><@s.param name="teamId" value="${team.id}" /><#if (start > 0)><@s.param name="start" value="${start}" /></#if></@s.url>">${I18n.getMessage("Moderation.CloseModeration")}</a>
							<#else>
								<a href="<@s.url namespace="/team" action="moderation"><@s.param name="teamId" value="${team.id}" /><#if (start > 0)><@s.param name="start" value="${start}" /></#if></@s.url>">${I18n.getMessage("Moderation.OpenModeration")}</a>
							</#if>
						</#if>
					</td>
				</tr>
			</table>

			<table cellspacing="2" cellpadding="2" width="100%" align="center">
				<tr>
				    <form accept-charset="${encoding}" action="${JForumContext.encodeURL("/jforum")}" method="get" id="formSearch" name="formSearch">
                    <input type="hidden" name="module" value="search"/>
                    <input type="hidden" name="action" value="search"/>
                    <input type="hidden" name="channel" value="team"/>
                    <input type="hidden" name="forum" value="${team.id}">
                    <input type="hidden" name="match_type" value="all">

                    <td class="nav" valign="middle" align="left">
                        <input type="text" onblur="if (this.value == '') this.value = '搜索这个群组...';" onclick="if (this.value == '搜索这个群组...') this.value = '';" value="搜索这个群组..." size="20" name="search_keywords" class="inputSearchForum"/>
                        <input type="submit" value="${I18n.getMessage("ForumBase.search")}" class="liteoption">
                    </td>

                    </form>
                    
                    <td class="nav" nowrap="nowrap" align="right">
                        <#assign paginationData><@pagination.doPagination action, team.id/></#assign>
                        ${paginationData}
                    </td>
                    
                    <td valign="middle" align="left" width="50">
                    <#if !replyOnly>
                        <a href="${JForumContext.encodeURL("/jforum${extension}?module=posts&amp;action=insert&amp;forum_id=${team.id}", "")}" rel="nofollow" class="icon_new_topic"><img src="${contextPath}/images/transp.gif" alt="" /></a>
                    </#if>
                    </td>
                    
				</tr>
			</table>

			<#if moderator>
				<form action="${JForumContext.encodeURL("/jforum")}" method="post" name="formModeration" id="formModeration" accept-charset="${encoding}">
				<input type="hidden" name="action" value="doModeration" />
				<input type="hidden" name="module" value="moderation" />
				<input type="hidden" name="returnUrl" value="<@s.url namespace="/team" action="show"><@s.param name="teamId" value="${team.id}" /><#if (start > 0)><@s.param name="start" value="${start}" /></#if></@s.url>" />
				<input type="hidden" name="forum_id" value="${team.id}" />
				<input type="hidden" name="log_type" value="0"/>
				<input type="hidden" name="log_description">
			</#if>

			<table class="forumline" cellspacing="1" cellpadding="4" width="100%" border="0">
				<tr>
					<th class="thcornerl" nowrap="nowrap" align="center" colspan="2" height="25">&nbsp;${I18n.getMessage("ForumIndex.topics")}&nbsp;</th>
					<th class="thtop" nowrap="nowrap" align="center" width="50">&nbsp;${I18n.getMessage("ForumIndex.answers")}&nbsp;</th>
					<th class="thtop" nowrap="nowrap" align="center" width="100">&nbsp;${I18n.getMessage("ForumIndex.author")}&nbsp;</th>
					<th class="thtop" nowrap="nowrap" align="center" width="50">&nbsp;${I18n.getMessage("ForumIndex.views")}&nbsp;</th>
					<th class="thcornerr" nowrap="nowrap" align="center">&nbsp;${I18n.getMessage("ForumIndex.lastMessage")}&nbsp;</th>

					<#if moderator && openModeration?default(false)>
						<th class="thcornerr" nowrap="nowrap" align="center">&nbsp;${I18n.getMessage("ForumIndex.moderation")}&nbsp;</th>
					</#if>
				</tr>

				<!-- TOPICS LISTING -->
				<#assign sepTrDisplayed = false />
				<#list topics as topic>
				    <#assign hasAnnounce = false />
				    <#if topic.type == TOPIC_ANNOUNCE>
				        <#assign hasAnnounce = true />
				    </#if>
					<#assign class1>class="row1"</#assign>
					<#assign class2>class="row2"</#assign>
					<#assign class3>class="row3"</#assign>

                    <#if (topic.type != TOPIC_ANNOUNCE) && (topic_index<topics.size()) && !sepTrDisplayed >
                        <tr class="sep1"><td colspan="6"></td></tr>
                        <#assign sepTrDisplayed = true />
                    </#if>
					<tr class="bg_small_yellow">
						<td ${class1} valign="middle"  align="center" width="20"><@presentation.folderImage topic/></td>
						<td ${class1} width="100%">
							<#if topic.hasAttach() && attachmentsEnabled><img src="${contextPath}/templates/${templateName}/images/icon_clip.gif" align="middle" alt="[Clip]" /></#if>
							<span class="topictitle">
							<a href="${JForumContext.encodeURL("/posts/list/${topic.id}")}">
							<#if topic.vote>${I18n.getMessage("ForumListing.pollLabel")}</#if>
							<#if (topic.title?length == 0)>
								No Subject
							<#else>
								${topic.title?html}
							</#if>
							</a>
							</span>

							<#if topic.paginate>
								<span class="gensmall">
								<br />
								<@pagination.littlePostPagination topic.id, postsPerPage, topic.totalReplies/>
								</span>
							</#if>
						</td>

						<td ${class2} valign="middle"  align="center"><span class="postdetails">${topic.totalReplies}</span></td>
						<td ${class3} valign="middle"  align="center">
							<span class="name"><a href="${JForumContext.encodeURL("/user/profile/${topic.postedBy.id}")}">${topic.postedBy.username}</a></span>
						</td>

						<td ${class2} valign="middle"  align="center"><span class="postdetails">${topic.totalViews}</span></td>
						<td ${class3} valign="middle"  nowrap="nowrap" align="center">
							<#if (topic.lastPostTime?length > 0)>
								<span class="postdetails">${topic.lastPostTime}<br />
								<a href="${JForumContext.encodeURL("/user/profile/${topic.lastPostBy.id}")}">${topic.lastPostBy.username}</a>

								<#assign startPage = ""/>
								<#if (topic.totalReplies + 1 > postsPerPage?number)>
									<#assign startPage = ((topic.totalReplies / postsPerPage?number)?int * postsPerPage?number) +"/"/>
								</#if>

								<a href="${JForumContext.encodeURL("/posts/list/${startPage}${topic.id}")}#${topic.lastPostId}"><img src="${contextPath}/templates/${templateName}/images/icon_latest_reply.gif" border="0" alt="[Latest Reply]" /></a></span>
							</#if>
						</td>

						<#if moderator && openModeration?default(false)>
							<td ${class2} valign="middle" align="center">
								<input type="checkbox" <#if topic.movedId != 0 && topic.forumId != forum.id>disabled="disabled"</#if> name="topic_id" value="${topic.id}" onclick="changeTrClass(this, ${topic_index});"/>
							</td>
						</#if>
					</tr>
				</#list>
				<!-- END OF TOPICS LISTING -->
				<#if moderator  && openModeration?default(false)>
				<tr align="center">
					<td class="catbottom" valign="middle"  align="right" colspan="<#if moderator && openModeration?default(false)>7<#else>6</#if>" height="28">
						<table cellspacing="0" cellpadding="0" border="0">
							<tr>
								<td align="center"><span class="gensmall">&nbsp;<@presentation.moderationButtons/></span></td>
							</tr>
						</table>
					</td>
				</tr>
				</#if>
			</table>
			
			<#if moderator></form></#if>	

			<table cellspacing="2" cellpadding="2" width="100%" align="center" border="0">
				<tr>
					

					<td valign="middle"  align="left" colspan="${colspan?default("0")}">
						<span class="nav">
						<a class="nav" href="${contextPath}/team/list.action">群组首页</a> &raquo;  ${team.name?html}
						</span>
					</td>

					<td nowrap="nowrap" align="right" class="nav">${paginationData}</td>
					<#if !readonly && !replyOnly>
                        <td valign="middle"  align="left" width="50">
                            <a href="${JForumContext.encodeURL("/jforum${extension}?module=posts&amp;action=insert&amp;forum_id=${team.id}","")}" rel="nofollow" class="icon_new_topic"><img src="${contextPath}/images/transp.gif" alt="" /></a>
                        </td>
                    <#else>
                        <#assign colspan = "2"/>
                    </#if>
				</tr>

				<tr>
					<td align="left" colspan="3"><span class="nav"></span></td>
				</tr>
			</table>
<#--
			<table cellspacing="0" cellpadding="5" width="100%" border="0">
				<tr>
					<td align="left" class="gensmall">
						<#if logged>
							<#if !watching>
								<#assign watchMessage = I18n.getMessage("ForumShow.watch")/>
								<a href="#watch" onClick="watchForum('${JForumContext.encodeURL("/forums/watchForum/${team.id}")}', '${I18n.getMessage("ForumShow.confirmWatch")}');">
							<#else>
								<#assign watchMessage = I18n.getMessage("ForumShow.unwatch")/>
								<a href="${JForumContext.encodeURL("/forums/unwatchForum/${team.id}")}">
							</#if>
							<img src="${contextPath}/templates/${templateName}/images/watch.gif" align="middle" alt="Watch" />&nbsp;${watchMessage}</a>
						</#if>
					</td>
					<td align="right"><@presentation.forumsComboTable/></td>
				</tr>
			</table>
-->
			<table cellspacing="0" cellpadding="0" width="100%" align="center" border="0">
				<tr>
					<td valign="top" align="left">
					</td>

					<#assign moderators = team.getModeratorList()/>
					<#if (moderators.size() > 0)>
						<td align="right" class="gensmall">
							${I18n.getMessage("ForumIndex.forumAdmins")}:
							<b><#list moderators as m>
							<a href="${JForumContext.encodeURL("/user/listGroup/${m.id}")}">${m.name?html}</a>
							</#list></b>
						</td>
					</#if>
				</tr>
			</table>
		</td>
	</tr>
</table>

<#include "/templates/default/bottom.htm" />
