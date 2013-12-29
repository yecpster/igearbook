<#-- ------------------------------------------------------------------------------- -->
<#-- Pagination macro base code inspired from PHPBB's generate_pagination() function -->
<#-- ------------------------------------------------------------------------------- -->
<#macro doPagination namespace action data>
	<#if (data.totalRecords > data.recordsPerPage)>
		<div class="pagination">
		<#assign link = ""/>

		<#-- ------------- -->
		<#-- Previous page -->
		<#-- ------------- -->
		<#if (data.currentPage > 1)>
			<#assign start = (data.currentPage - 2) * data.recordsPerPage/>
			<a href="<@s.url namespace="${namespace}" action="${action}">
			             <#if (start > 0)>
			             <@s.param name="start" value="${start}" />
			             </#if>
			             <#list data.webParams?keys as key>
                            <@s.param name="${key}" value="${data.webParams[key]}" />
                         </#list>
			         </@s.url>">
			             &#9668;
			</a>
		</#if>

		<#if (data.totalPages > 10)>
			<#-- ------------------------------ -->
			<#-- Always write the first 3 links -->
			<#-- ------------------------------ -->
			<#list 1 .. 3 as page>
				<@pageLink page, data namespace action />
			</#list>

			<#-- ------------------ -->
			<#-- Intermediate links -->
			<#-- ------------------ -->
			<#if (data.currentPage > 1 && data.currentPage < data.totalPages)>
				<#if (data.currentPage > 5)><span class="gensmall">...</span></#if>

				<#if (data.currentPage > 4)>
					<#assign min = data.currentPage - 1/>
				<#else>
					<#assign min = 4/>
				</#if>

				<#if (data.currentPage < data.totalPages - 4)>
					<#assign max = data.currentPage + 2/>
				<#else>
					<#assign max = data.totalPages - 2/>
				</#if>

				<#if (max >= min + 1)>
					<#list min .. max - 1 as page>
						<@pageLink page, data namespace action />
					</#list>
				</#if>

				<#if (data.currentPage < data.totalPages - 4)><span class="gensmall">...</span></#if>
			<#else>
				<span class="gensmall">...</span>
			</#if>

			<#-- ---------------------- -->
			<#-- Write the last 3 links -->
			<#-- ---------------------- -->
			<#list data.totalPages - 2 .. data.totalPages as page>
				<@pageLink page, data namespace action />
			</#list>
		<#else>
			<#list 1 .. data.totalPages as page>
				<@pageLink page, data namespace action />
			</#list>
		</#if>

		<#-- ------------- -->
		<#-- Next page -->
		<#-- ------------- -->
		<#if (data.currentPage < data.totalPages)>
			<#assign start = data.currentPage * data.recordsPerPage />
			<a href="<@s.url namespace="${namespace}" action="${action}">
                         <#if (start > 0)>
                         <@s.param name="start" value="${start}" />
                         </#if>
                         <#list data.webParams?keys as key>
                            <@s.param name="${key}" value="${data.webParams[key]}" />
                         </#list>
                     </@s.url>">&#9658;</a>
		</#if>

        <span style="margin: 0 0 0 20px;padding-top: 0;font-weight: normal;color: #265827;font-size: 12px;" >输入页码跳转</span>
        <input type="text" title="输入后按回车就可以跳转哦！" size="4" style="margin: 0;padding-top: 0;border-color: #2d2d2d;font-size: 12px;" onkeypress="if(event.keyCode==13){goToAnotherPage(${data.totalPages}, ${data.recordsPerPage}, '${contextPath}', '${namespace}', '${action}', this.value);return false;}" />
        <a href="#goto" onClick="goToAnotherPage(${data.totalPages}, ${data.recordsPerPage}, '${contextPath}', '${namespace}', '${action}', $(this).prev().val());">GO&#9658;</a>

		</div>
	</#if>
</#macro>

<#macro pageLink page data namespace action>
	<#assign start = data.recordsPerPage * (page - 1)/>
	<#if page != data.currentPage>
		<#assign link><a href="<@s.url namespace="${namespace}" action="${action}">
                                     <#if (start > 0)>
                                     <@s.param name="start" value="${start}" />
                                     </#if>
                                     <#list data.webParams?keys as key>
                                        <@s.param name="${key}" value="${data.webParams[key]}" />
                                      </#list>
                               </@s.url>">${page}</a></#assign>
	<#else>
		<#assign link><span class="current">${page}</span></#assign>
	</#if>

	${link}
</#macro>
