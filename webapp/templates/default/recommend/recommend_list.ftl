<#include "/templates/default/header.htm"/>
<#import "/templates/macros/paginationStruts.ftl" as pagination/>
<@navHeader />

<script type="text/javascript" src="${contextPath}/templates/${templateName}/js/paginationStruts.js?${startupTime}"></script>
<script type="text/javascript" src="${contextPath}/templates/${templateName}/js/jquery.js?${startupTime}"></script>

<table width="100%">
    <tr>
        <td align="right">
        <#assign paginationData><@pagination.doPagination "/recommend", "manage", data /></#assign>${paginationData}
        </td>
    </tr>
</table>
<@s.form namespace="/recommend" action="delete" method="post">
<table class="forumline" cellspacing="1" cellpadding="3" width="100%" border="0">
	<tr align="center">
	  <th class="thcornerl" nowrap="nowrap">#</th>
	  <th class="thtop" nowrap="nowrap">&nbsp;频道&nbsp;</th>
	  <th class="thtop" nowrap="nowrap" width="280">&nbsp;标题&nbsp;</th>
	  <th class="thtop" nowrap="nowrap">&nbsp;封面图片&nbsp;</th>	  
	  <th class="thtop" nowrap="nowrap">&nbsp;创建&nbsp;</th>
	  <th class="thtop" nowrap="nowrap" colspan="2">&nbsp;最后更新&nbsp;</th>
	  <th class="thtop" nowrap="nowrap">&nbsp;删除&nbsp;</th>
	</tr>

	<#list data.list as r>
			<tr align="center">
			    <td class="row2"><span class="gen">${r_index+1}</span></td>
			    <td class="row2"><#if r.type==0>
			                         <span class="gen">编辑推荐</span>
			                     <#elseif r.type==1>
			                         <span class="gen">群组精华</span>
			                     </#if>
			    </td>
				<td class="row2"><a href="${JForumContext.encodeURL("/posts/list/${r.topicId}")}" title="${r.desc?default("")}"><span class="gen">${r.title?default("")}</span></a></td>			
				<td class="row2"><a href="${JForumContext.encodeURL("/posts/list/${r.topicId}")}" title="${r.desc?default("")}"><span class="gen"><img src="${r.imageUrl?default("")}" width="100" /></span></a></td>
				<td class="row2"><span class="gen">${r.createTime?datetime?string}<br/>by&nbsp;</span>
				                 <a href="${JForumContext.encodeURL("/user/profile/${r.createBy.id}")}">${r.createBy.username}</a>
				</td>
				<td class="row2"><span class="gen">${r.lastUpdateTime?datetime?string}<br/>by&nbsp;</span>
				                 <a href="${JForumContext.encodeURL("/user/profile/${r.lastUpdateBy.id}")}">${r.lastUpdateBy.username}</a>
				</td>
				<td class="row2"><span class="gen"><a href="<@s.url namespace="/recommend" action="edit"><@s.param name="topicId" value="${r.topicId}" /></@s.url>">编辑</a></td>
				<td class="row2"><span class="gen"><@s.checkbox name="selectedRtopics" value="" fieldValue="${r.id}" /></span></td>
			</tr>
	</#list>
	<tr align="center">
		<td class="catbottom" colspan="8" height="28"><@s.submit cssClass="mainoption" value="删除选择的项目" />
		</td>
	</tr>
</table>
</@s.form>
<table width="100%">
	<tr>
		<td align="right">${paginationData}</td>
	</tr>
</table>

<#include "/templates/default/bottom.htm"/>