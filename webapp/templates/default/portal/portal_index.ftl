<#include "/templates/default/header.htm"/>
<@navHeader "index" />

<div id="forum_tab_show" class="clearfix">
  <div id="slides">
    <#if recommendTopic?exists>
    <a href="${JForumContext.encodeURL("/posts/list/${recommendTopic.topicId}")}" title="${recommendTopic.title?default("")}" target="_blank">
    <img src="${recommendTopic.imageUrl?default("")}" alt="${recommendTopic.title?default("")}-封面图片" width="380" height="285">
    <h4>${recommendTopic.title?default("")}</h4>
    <p>${recommendTopic.desc?default("")}...</p>
    </a>
    </#if>
  </div>
  <div id="new_topics" class="box middle" >
    <h4>最近更新</h4>
    <ul>
        <#list recentTopics as topic>
        <li><a href="${JForumContext.encodeURL("/posts/list/${topic.id}")}" title="${topic.title?default("")}" target="_blank">${topic.title?html} </a></li>
        </#list>
    </ul>
  </div>
  <div class="last">
    <h4>${I18n.getMessage("ForumBase.hottestTopics")}</h4>
    <ul>
      <#list hotTopics as topic>
      <li><a href="${JForumContext.encodeURL("/posts/list/${topic.id}")}" title="${topic.title?default("")}" target="_blank">${topic.title?html}</a></li>
      </#list>
    </ul>
            <div id="top_entry" class="tab_wrapper">
              <div style="float:left;"><a href="${contextPath}/team/list.action" title="进入群组频道"><img src="${contextPath}/images/team_btn.jpg" width="147" height="78" border="0" alt="群组入口" /></a></div>
              <div style="float:right;"><a href="${contextPath}/forums/show/5.page" title="进入团购频道"><img src="${contextPath}/images/group_buy_btn.jpg" width="147" height="78" border="0" alt="团购入口" /></a></div>
            </div>
  </div>
</div>


    <div id="index-category" class="index-group">
      <div class="category"> 
      
        <div class="row index-group">
        <h2 class="category-bar"><a href="${contextPath}/forums/show/1.page">装备网编辑推荐</a></h2>
         <#list igearbookTopics as recommendTopic>
          <div class="item">                        
            <a href="${JForumContext.encodeURL("/posts/list/${recommendTopic.topicId}")}" title="${recommendTopic.title?default("")}" class="index-group"> 
            <img src="${recommendTopic.imageUrl?default("")}" width="246" height="184" alt="${recommendTopic.title?default("")}-封面图片" />
            <h3>${recommendTopic.title?default("")}</h3>
            <p>${recommendTopic.desc?default("")}</p>
            </a>            
          </div>
         </#list>
        </div>
        
        <div class="row index-group">
        <h2 class="category-bar"><a href="${contextPath}/team/list.action">装备网群组精华</a></h2>
        <#list teamRecommends as recommendTopic>
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
        <#list teamRecommends as recommendTopic>
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
        <#list teamRecommends as recommendTopic>
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
