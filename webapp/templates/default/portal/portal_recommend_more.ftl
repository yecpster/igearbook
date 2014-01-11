<#include "/templates/default/header.htm"/>
<@navHeader "index" />
<#import "/templates/macros/paginationStruts.ftl" as pagination/>

    <div id="index-category" class="index-group">
      <div class="category"> 
        <table width="100%">
           <tr>
            <td align="right">
                <#assign paginationData><@pagination.doPagination "/portal", "recommend-more", pageData /></#assign>${paginationData}
            </td>
          </tr>
        </table>
      
        <div class="row index-group">
        <h2 class="category-bar">
        <#if type==0>
            <a href="<@s.url value="/recommend-more" />">装备网编辑推荐</a>
        <#elseif type==1>
            <a href="<@s.url value="/team-recommend-more" />">装备网群组精华</a>
        </#if>
        </h2>
        <#list pageData.list as recommendTopic>
           <#if recommendTopic_index < 3>
           <div class="item">                        
            <a href="${JForumContext.encodeURL("/posts/list/${recommendTopic.topicId}")}" title="${recommendTopic.title?default("")}" class="index-group"> 
            <img src="${recommendTopic.imageUrl?default("")}" width="246" height="184" alt="${recommendTopic.title?default("")}-封面图片" />
            <h3>${recommendTopic.title?default("")}</h3>
            <p>${recommendTopic.desc?default("")}</p>
            </a>            
          </div>
          </#if>
        </#list>
        </div>
        
        <div class="row index-group">
        <h2 class="category-bar"></h2>
        <#list pageData.list as recommendTopic>
           <#if (recommendTopic_index >= 3) && (recommendTopic_index < 6)>
           <div class="item">                        
            <a href="${JForumContext.encodeURL("/posts/list/${recommendTopic.topicId}")}" title="${recommendTopic.title?default("")}" class="index-group"> 
            <img src="${recommendTopic.imageUrl?default("")}" width="246" height="184" alt="${recommendTopic.title?default("")}" />
            <h3>${recommendTopic.title?default("")}</h3>
            <p>${recommendTopic.desc?default("")}</p>
            </a>            
          </div>
          </#if>
        </#list>
        </div>
        
       <div class="row index-group">
        <h2 class="category-bar"></h2>
        <#list pageData.list as recommendTopic>
           <#if (recommendTopic_index >= 6) && (recommendTopic_index < 9)>
           <div class="item">                        
            <a href="${JForumContext.encodeURL("/posts/list/${recommendTopic.topicId}")}" title="${recommendTopic.title?default("")}" class="index-group"> 
            <img src="${recommendTopic.imageUrl?default("")}" width="246" height="184" alt="${recommendTopic.title?default("")}" />
            <h3>${recommendTopic.title?default("")}</h3>
            <p>${recommendTopic.desc?default("")}</p>
            </a>            
          </div>
          </#if>
        </#list>
        </div>
        
        <div class="row index-group">
        <h2 class="category-bar"></h2>
        <#list pageData.list as recommendTopic>
           <#if (recommendTopic_index >= 9) && (recommendTopic_index < 12)>
           <div class="item">                        
            <a href="${JForumContext.encodeURL("/posts/list/${recommendTopic.topicId}")}" title="${recommendTopic.title?default("")}" class="index-group"> 
            <img src="${recommendTopic.imageUrl?default("")}" width="246" height="184" alt="${recommendTopic.title?default("")}" />
            <h3>${recommendTopic.title?default("")}</h3>
            <p>${recommendTopic.desc?default("")}</p>
            </a>            
          </div>
          </#if>
        </#list>
        </div>
       <table width="100%">
           <tr>
            <td align="right">
            ${paginationData}
            </td>
          </tr>
        </table>
      </div>
      <!-- category end -->
      <div class="sidebar">
        <div class="inner"> 
<div class="widget fast-ad">
  <h2>热门群组</h2>
  <#list hotTeams as hotTeam>
        <#if (hotTeam_index < 10)>
  <a href="
      <#if hotTeam.uri?exists>
        <@s.url value="/${hotTeam.uri}" />
      <#else>
        <@s.url value="/team/show/${hotTeam.id}" />
      </#if>
  " title="${hotTeam.name}" target="_blank">
  <div class="row index-group"> <img src="${contextPath}${hotTeam.logo?default("")}" alt="${hotTeam.name}" width="60" height="60" border="0" />
    <p>
        <#if hotTeam.description?exists && (hotTeam.description?length > 23)>
                    ${hotTeam.description?substring(0, 23)} ...
                <#else>
                    ${hotTeam.description}
        </#if>
    </p>
  </div>
  </a>
  </#if>
 </#list>
  
</div>
<#--
<div class="widget article">
  <h2><a href="http://www.mobile01.com/newslist.php">装备网新聞眼</a></h2>
  
  <a href="http://www.mobile01.com/newsdetail.php?id=14141">
  <div class="row"> <img src="/iGearBook/Mobile01_files/0173447f8b446ec53f52309bc75dc911.jpg" alt="【採訪】啟動全球電子商務服務-uitox電子商務集團上線記者會">
    <h3>【採訪】啟動全球電子商務服務-uitox電子商務集團上線記者會</h3>
    <p>對現代人而言電子商務應該已經成為生活中不可或缺的一部分，有不少上班族的食衣住行幾...</p>
  </div>
  </a>
  
  <a href="http://www.mobile01.com/newsdetail.php?id=14140">
  <div class="row"> <img src="/iGearBook/Mobile01_files/4aa26c6e7f0a292bfcc6d47f8eac9218.jpg" alt="【採訪】第五屆 YAMAHA CUP 快樂踢球趣足球賽 賽前說明會">
    <h3>【採訪】第五屆 YAMAHA CUP 快樂踢球趣足球賽 賽前說明會</h3>
    <p>YAMAHA CUP 快樂踢球趣不知不覺已經來到了第五屆，從2009年開辦以來參...</p>
  </div>
  </a>
  
</div>
-->
<div class="widget"> 
        <a href="${homepageLink}" target="_blank">
            <div class="row first"> <img src="${contextPath}/images/2dbcode.png" width="130" height="130" alt="www.igearbook.com">
              <p>手机可扫描此二维码进入<br>http://www.igearbook.com</p>
            </div>
            </a> 
</div>
<br class="clear">
</div>
      </div>
      <!-- sidebar end -->
    </div>
<#--
<script type="text/javascript">
  var current_index = 0;
  var slideArray = [<#list recommends as r>"\u003Ca href=\"${JForumContext.encodeURL("/posts/list/${r.topicId}")}\" title=\"${r.title}\" target='_blank'\u003E\u003Cimg src=\"${r.imageUrl}\" width=\"370\" height=\"180\"\u003E\u003C/a\u003E"<#if r_has_next>,</#if></#list>];
  Event.observe(window,'load',function() {
    show_slide(0);
    new PeriodicalExecuter(show_slide, 411);
  });
  var desc = "<p>xxx...</p>";
  function show_slide(s_index) {
    if(typeof(s_index) != 'number') s_index = current_index + 1;
    if(s_index >= slideArray.length) s_index = 0;
    $("slides").innerHTML = slideArray[s_index];
    var s_pagination = slideArray.inject("<span class='pagination'>", function(memo, value, i) {
      return memo + (s_index == i ? "<span class='current'>" + (i + 1) + "</span>" : "<a href='#' onclick='show_slide(" + i + ");return false;'>" + (i + 1) + "</a>");
    }) + "</span>"+desc;


    $("slides").insert(s_pagination);

    current_index = s_index;
  }
</script>
-->
<#include "/templates/default/bottom.htm" />
