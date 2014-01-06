
<#--side-->
<div id="side">
  
    <div class="box">
      <h3>群组排名</h3>
        <#list rankTeams as rankTeam>
        <#if (rankTeam_index < 10)>
            <div class="rank">
              <div class="rank_info">${rankTeam_index + 1}.<a href="
              <#if rankTeam.uri?exists>
                 <@s.url value="/${rankTeam.uri}" />
              <#else>
                <@s.url value="/team/show/${rankTeam.id}" />
              </#if>
              " title="${rankTeam.description?default("")}">${rankTeam.name}</a><br>文章数：${rankTeam.totalPosts}</div>
              <div class="rank_logo"><div class="logo"><a href="
              <#if rankTeam.uri?exists>
                 <@s.url value="/${rankTeam.uri}" />
              <#else>
                <@s.url value="/team/show/${rankTeam.id}" />
              </#if>
              " title="${rankTeam.description?default("")}"><img src="${rankTeam.logo?default("")}" alt="${rankTeam.name}" height="48" width="48"></a></div></div>
            </div>
        </#if>
        </#list>
    </div>

</div>
<#--side end-->

