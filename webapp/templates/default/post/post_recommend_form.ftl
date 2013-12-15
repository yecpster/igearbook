<#include "/templates/default/header.htm"/>
<@navHeader "bbs" />

<link href="${contextPath}/templates/${templateName}/styles/validation_style.css?${startupTime}" media="screen" rel="stylesheet" type="text/css" />
<script type="text/javascript" src="${contextPath}/templates/${templateName}/js/validation/prototype_for_validation.js"></script>
<script type="text/javascript" src="${contextPath}/templates/${templateName}/js/validation/validation_cn.js"></script>
<script type="text/javascript" src="${contextPath}/templates/${templateName}/js/jquery.js?${startupTime}"></script>
<#assign isEdit = isEdit?if_exists/>

<@s.form namespace="/post" action="recommendSave" method="post" enctype="multipart/form-data" onsubmit="return checkSubmit()" id="postFrom">
<@s.if test="rtopic.getId()!=0">
    <@s.hidden name="rtopic.id" />
</@s.if>
<@s.hidden name="rtopic.topicId" />
<table cellspacing="0" cellpadding="10" width="100%" align="center" border="0">
    <tr>
        <td class="bodyline">
            <br clear="all" />

            <table cellspacing="2" cellpadding="2" width="100%" align="center" border="0">
                <tr>
                    <td align="left">
                        <span class="nav">
                            <a class="nav" href="${JForumContext.encodeURL("/forums/list")}">${I18n.getMessage("ForumListing.forumIndex")}</a>
                            <#if forum?exists>
                            &raquo; <a class="nav" href="${JForumContext.encodeURL("/forums/show/${forum.id}")}">${forum.name}</a>
                            </#if>
                        </span>
                    </td>
                </tr>
            </table>
        
            <table class="forumline" cellspacing="1" cellpadding="3" width="100%" border="0">
                <tr>
                    <th class="thhead" colspan="2" height="25">
                        <b>
                            ${I18n.getMessage("PostRecommendForm.title")}
                        </b>
                    </th>
                </tr>

                <#if errorMessage?exists>
                    <tr>
                        <td colspan="2" align="center"><span class="gen"><font color="#ff0000"><b>${errorMessage}</b></font></span></td>
                    </tr>
                </#if>

                <tr>
                    <td class="row1" width="7%"><span class="gen"><b>标题</b></span></td>
                    <td class="row2" width="93%">
                        <span class="gen">
                            <@s.textfield size="30" name="rtopic.title" cssClass="type required min-length-8 max-length-32" maxlength="200" size="110"  />
                        </span>
                    </td>
                </tr>
                <tr>
                    <td class="row1" width="7%"><span class="gen"><b>推荐频道</b></span></td>
                    <td class="row2" width="93%">
                        <span class="gen">
                            <@s.select label="Months" name="rtopic.type" list=r"#{'0':'编辑推荐', '1':'群组精华'}" value="rtopic.type" />
                        </span>
                    </td>
                </tr>
                 <tr>
                    <td class="row1" valign="top"><span class="gen">
                        <span class="gen"><b>${I18n.getMessage("PostRecommendForm.desc")}</b></span>
                    </td>
                    <td class="row2" valign="top">
                        <div class="gen">
                            <table cellspacing="0" cellpadding="2" border="0" width="100%">
                                <tr>
                                    <td valign="top">
                                        <@s.textarea name="rtopic.desc" cols="30" rows="15" cssClass="message required min-length-45 max-length-120" rows="5" cols="35" />
                                    </td>
                                </tr>
                            </table>
                        </div> 
                    </td>
                </tr>
                <tr>
                    <td class="row1" valign="top">
                        <span class="gen"><b>封面图片</b></span>
                    </td>
                    <td class="row2" valign="bottom">
                        <table cellspacing="0" cellpadding="2" border="0" width="100%">
                                <tr>
                                    <td valign="top">
                                        <@s.radio name="coverImgType" list={"fromPost": "从文章选取", "upload": "上传图片"} listKey="key" listValue="value" />
                                    </td>
                                </tr>
                                <tr>
                                    <td>
                                    <div id="coverImgList" class="gen"<#if !hasImg> style="display:none;"</#if>>
                                    <#if hasImg>
                                        <table cellspacing="0" cellpadding="2" border="1" width="100%">
                                            <#list imgList as img>
                                            <#if img_index==0>
                                                <tr>
                                            </#if>
                                                <td valign="bottom">
                                                    <table cellspacing="0" border="0" width="100%">
                                                        <tr>
                                                            <td valign="bottom" align="center">
                                                                <img src="${img}" width=100 />
                                                            </td>
                                                         </tr>
                                                         <tr>
                                                            <td valign="bottom" align="center">
                                                            <input type="radio"<#if img==rtopic.imageUrl> checked="checked"</#if> name="rtopic.imageUrl" value="${img}">
                                                            </td>
                                                         </tr>
                                                    </table>
                                                </td>
                                            
                                            <#if !img_has_next>
                                                </tr>
                                            <#elseif (img_index+1)%7==0>
                                                </tr>
                                                <tr>
                                            </#if>
                                            </#list>
                                            
                                        </table>
                                   <#else>
                                        <span>文章内容不包含图片</span>
                                        <@s.textfield size="30" name="rtopic.imageUrl" cssClass="required" disabled="true" maxlength="200" size="110"  />
                                   </#if>
                                    </div>
                                    <div id="uploadImgDiv" class="gen"<#if hasImg> style="display:none;"</#if>>
                                        <@s.file id="uploadFile" name="upload" cssClass="validate-file-png-jpg-gif" />
                                    </div>
                                    </td>
                                </tr>
                        </table>
                    </td>
                </tr>
               

               
                    <tr>
                        <td class="row1">错误信息:</td>
                        <td class="row2">
                            <div id="error" style="display:none;">
                            
                                标题：<div id="advice-postFrom_rtopic_title" style="display:none" class="validation-advice"></div>
                                <br/>
            主题简介：<div id="advice-postFrom_rtopic_desc" style="display:none" class="validation-advice"></div>
                            </div></td>
                    </tr>
               
                
                <tr>
                    <td align="center" height="28" colspan="2" class="catbottom">
                        <input class="mainoption submit" id="btnSubmit" accesskey="s" tabindex="6" type="submit" value="${I18n.getMessage("PostForm.submit")}" name="post" />
                        <img src="${contextPath}/images/transp.gif" id="icon_saving">
                    </td>
                </tr>
            </table>
        </td>
    </tr>
</table>
<@s.token />
</@s.form>

<script>
var valid = new Validation('postFrom', {immediate : true});
function checkSubmit() {
    var element = document.getElementById('error');
    if(!valid.validate()) {
        if(element!=null){
            element.style.display = "block";
        }
        return false;
    } else {
        return true;
    }
}
function resizeImg(){ 
$("#coverImgList").find("img").each(function() {
    var img = $(this);
    var src = img.attr("src");
    var index = src.lastIndexOf("/");
    var srcSub = src.substr(index);
    var shouldZoom = (srcSub.match(/\d+_\d+_\d\./) != null) && (srcSub.indexOf("_0.") < 0);
    var width = img.attr("width");
    var height = img.attr("height");

    if (shouldZoom) {
          var zoomSrc = src.replace(/(.+)_\d\.(.+)/i, "$1_0.$2");
          img.attr("title", "点击查看原始大小图片");
          img.css("cursor", "url(${contextPath}/images/zoom.cur), pointer");
          img.click(function(){window.open(zoomSrc);});
    }
    if(width>100){
     img.attr("width", 100);
     img.attr("height", height/width*100);
     img.attr("title", "点击查看原始大小图片");
     img.css("cursor", "url(${contextPath}/images/zoom.cur), pointer");
     img.click(function(){window.open(src);});
    }
});
}
$(document).ready(function() {
    resizeImg();
    $("input[type='radio'][name='coverImgType']").change(function(){
        var coverImgType = $("input[type='radio'][name='coverImgType']:checked").val();
        if ("upload"==coverImgType){
            $("#coverImgList").hide("slow");
            $("#uploadImgDiv").show("slow");
            $("#uploadFile").addClass("required");
        } else{
            $("#coverImgList").show("slow");
            $("#uploadImgDiv").hide("slow");
            $("#uploadFile").removeClass("required");
        }
    }
    );
});

</script>

<#include "/templates/default/bottom.htm" />
