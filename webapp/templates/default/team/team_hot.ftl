    <div class="box">
      <h3>近期热门群组</h3>
      <#list hotTeams as hotTeam>
        <#if (hotTeam_index < 10)>
        <div class="clearfix hot_group">
          <div class="logo"><a href="
                  <#if hotTeam.uri?exists>
                      <@s.url value="/${hotTeam.uri}" />
                  <#else>
                      <@s.url value="/team/show/${hotTeam.id}" />
                  </#if>
                      " title="${hotTeam.description}"><img src="${hotTeam.logo?default("")}" alt="${hotTeam.name}" height="48" width="48"></a></div>
          <div class="info" style="margin-left: 70px;">
            <a href="
            <#if hotTeam.uri?exists>
                <@s.url value="/${hotTeam.uri}" />
            <#else>
                <@s.url value="/team/show/${hotTeam.id}" />
            </#if>
                " title="${hotTeam.description}">${hotTeam.name}</a><br>
                <#if hotTeam.description?exists && (hotTeam.description?length > 20)>
                    ${hotTeam.description?substring(0, 20)} ...
                <#else>
                    ${hotTeam.description}
                </#if>
          </div>
        </div>
        </#if>
       </#list>
    </div>
<#--boxes end-->
