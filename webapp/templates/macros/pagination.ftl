<#macro littlePostPagination topicId postsPerPage totalReplies>
	[ <img class="icon_latest_reply" src="${contextPath}/images/transp.gif" alt="Go to Page" /> ${I18n.getMessage("goToPage")}: 

	<#assign totalPostPages = ((totalReplies + 1) / postsPerPage)?int/>

	<#if (((totalReplies + 1) % postsPerPage) > 0)>
		<#assign totalPostPages = (totalPostPages + 1)/>
	</#if>

	<#if (totalPostPages > 6)>
		<#assign minTotal = 3/>
	<#else>
		<#assign minTotal = totalPostPages/>
	</#if>

	<#-- ----------- -->
	<#-- First pages -->
	<#-- ----------- -->
	<#assign link = ""/>

	<#list 1 .. minTotal as page>
		<#assign start = postsPerPage * (page - 1)/>

		<#assign link>${link}<a href="${contextPath}/posts/list<#if (start>0)>/${start}</#if>/${topicId}${extension}">${page}</a></#assign>
		<#if (page < minTotal)><#assign link>${link}, </#assign></#if>		
	</#list>

	${link}

	<#assign link = ""/>
	<#if (totalPostPages > 6)>
		&nbsp;...&nbsp;
		<#list totalPostPages - 2 .. totalPostPages as page>
			<#assign start = postsPerPage * (page - 1)/>

			<#assign link>${link}<a href="${contextPath}/posts/list<#if (start>0)>/${start}</#if>/${topicId}${extension}">${page}</a></#assign>
			<#if (page_index + 1 < 3)><#assign link>${link}, </#assign></#if>
		</#list>

		${link}
	</#if>

	]
</#macro>

<#-- ------------------------------------------------------------------------------- -->
<#-- Pagination macro base code inspired from PHPBB's generate_pagination() function -->
<#-- ------------------------------------------------------------------------------- -->
<#macro doPagination action id=-1>
	<#if (totalRecords > recordsPerPage)>
		<div class="pagination">
		<#assign link = ""/>

		<#-- ------------- -->
		<#-- Previous page -->
		<#-- ------------- -->
		<#if (thisPage > 1)>
			<#assign start = (thisPage - 2) * recordsPerPage/>
			<a href="${contextPath}/${moduleName}/${action}<#if (start > 0)>/${start}</#if><#if (id > -1)>/${id}</#if>${extension}">&#9668;</a>
		</#if>

		<#if (totalPages > 10)>
			<#-- ------------------------------ -->
			<#-- Always write the first 3 links -->
			<#-- ------------------------------ -->
			<#list 1 .. 3 as page>
				<@pageLink page, id/>
			</#list>

			<#-- ------------------ -->
			<#-- Intermediate links -->
			<#-- ------------------ -->
			<#if (thisPage > 1 && thisPage < totalPages)>
				<#if (thisPage > 5)><span class="gensmall">...</span></#if>

				<#if (thisPage > 4)>
					<#assign min = thisPage - 1/>
				<#else>
					<#assign min = 4/>
				</#if>

				<#if (thisPage < totalPages - 4)>
					<#assign max = thisPage + 2/>
				<#else>
					<#assign max = totalPages - 2/>
				</#if>

				<#if (max >= min + 1)>
					<#list min .. max - 1 as page>
						<@pageLink page, id/>
					</#list>
				</#if>

				<#if (thisPage < totalPages - 4)><span class="gensmall">...</span></#if>
			<#else>
				<span class="gensmall">...</span>
			</#if>

			<#-- ---------------------- -->
			<#-- Write the last 3 links -->
			<#-- ---------------------- -->
			<#list totalPages - 2 .. totalPages as page>
				<@pageLink page, id/>
			</#list>
		<#else>
			<#list 1 .. totalPages as page>
				<@pageLink page, id/>
			</#list>
		</#if>

		<#-- ------------- -->
		<#-- Next page -->
		<#-- ------------- -->
		<#if (thisPage < totalPages)>
			<#assign start = thisPage * recordsPerPage/>
			<a href="${contextPath}/${moduleName}/${action}<#if (start > 0)>/${start}</#if><#if (id > -1)>/${id}</#if>${extension}">&#9658;</a>
		</#if>
		
		<span style="margin: 0 0 0 20px;padding-top: 0;font-weight: normal;color: #265827;font-size: 12px;" >输入页码跳转</span>
        <input type="text" title="输入后按回车就可以跳转哦！" size="4" style="margin: 0;padding-top: 0;border-color: #2d2d2d;font-size: 12px;" onkeypress="if(event.keyCode==13){goToAnotherPage(${totalPages}, ${recordsPerPage}, '${contextPath}', '${moduleName}', '${action}', ${id}, '${extension}', this.value);return false;}" />
		<a href="#goto" onClick="goToAnotherPage(${totalPages}, ${recordsPerPage}, '${contextPath}', '${moduleName}', '${action}', ${id}, '${extension}', $(this).prev().val());">GO&#9658;</a>

		</div>
	</#if>
</#macro>

<#macro pageLink page id>
	<#assign start = recordsPerPage * (page - 1)/>
	<#if page != thisPage>
		<#assign link><a href="${contextPath}/${moduleName}/${action}<#if (start > 0)>/${start}</#if><#if (id > -1)>/${id}</#if>${extension}">${page}</a></#assign>
	<#else>
		<#assign link><span class="current">${page}</span></#assign>
	</#if>

	${link}
</#macro>
