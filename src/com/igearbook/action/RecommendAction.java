package com.igearbook.action;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.jforum.SessionFacade;
import net.jforum.dao.DataAccessDriver;
import net.jforum.entities.Forum;
import net.jforum.entities.Post;
import net.jforum.entities.Topic;
import net.jforum.entities.User;
import net.jforum.entities.UserSession;
import net.jforum.repository.ForumRepository;
import net.jforum.repository.PostRepository;
import net.jforum.repository.TopicRepository;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.igearbook.common.ImageCommon;
import com.igearbook.constant.ImageSize;
import com.igearbook.dao.RecommendDao;
import com.igearbook.entities.ImageVo;
import com.igearbook.entities.PaginationData;
import com.igearbook.entities.Recommendation;
import com.igearbook.util.PostUtils;
import com.opensymphony.xwork2.ActionContext;

@Namespace("/recommend")
public class RecommendAction extends BaseAction {
    private static final long serialVersionUID = 8123243588174068662L;
    private static final Pattern IMG_PT = Pattern.compile("<img.*?src\\s*=\\s*['\"](.+?)['\"]", Pattern.CASE_INSENSITIVE);
    private static final Pattern IMG_SRC_PT = Pattern.compile(".+?_\\d+_\\d+_\\d\\.[a-zA-Z]+", Pattern.CASE_INSENSITIVE);
    private int id;
    private int topicId;
    private Recommendation rtopic;
    private PaginationData<Recommendation> data;
    private List<Integer> selectedRtopics;
    private File upload;
    private String uploadFileName;
    private String coverImgType;
    @Autowired
    private RecommendDao recommendDao;

    public void setRecommendDao(final RecommendDao recommendDao) {
        this.recommendDao = recommendDao;
    }

    @Action(value = "manage", results = { @Result(name = SUCCESS, location = "recommend_list.ftl") })
    public String manage() {
        final UserSession userSession = SessionFacade.getUserSession();
        final boolean canEdit = userSession.isWebAdmin() || userSession.isAdmin();
        if (!canEdit) {
            return ERROR;
        }
        data = recommendDao.doPagination(getPaginationParams());
        return SUCCESS;
    }

    @Action(value = "delete", results = { @Result(name = SUCCESS, location = "manage.action", type = "redirect") })
    public String delete() {
        final UserSession userSession = SessionFacade.getUserSession();
        final boolean canEdit = userSession.isWebAdmin() || userSession.isAdmin();
        if (!canEdit) {
            return ERROR;
        }
        if (selectedRtopics != null) {
            for (final Integer selectedRtopic : selectedRtopics) {
                final Recommendation recommend = new Recommendation();
                recommend.setId(selectedRtopic);
                recommendDao.delete(recommend);
            }
        }
        return SUCCESS;
    }

    @Action(value = "add", results = { @Result(name = SUCCESS, location = "recommend_form.ftl") })
    public String add() {
        return recommend();
    }

    @Action(value = "edit", results = { @Result(name = SUCCESS, location = "recommend_form.ftl") })
    public String edit() {
        return recommend();
    }

    private String recommend() {
        Topic topic = TopicRepository.getTopic(new Topic(topicId));
        if (topic == null) {
            topic = DataAccessDriver.getInstance().newTopicDAO().selectById(topicId);
        }
        final UserSession userSession = SessionFacade.getUserSession();
        final boolean isModerator = userSession.isModerator(topic.getForumId());
        final boolean canEditTeam = isModerator || userSession.isAdmin() || userSession.isWebAdmin();
        if (!canEditTeam) {
            return ERROR;
        }

        final Post post = PostRepository.selectAllByTopicByLimit(topicId, 0, 1).get(0);
        final String text = post.getText();
        final Matcher matcher = IMG_PT.matcher(text);
        final Set<String> imgList = Sets.newLinkedHashSet();
        while (matcher.find()) {
            final String src = matcher.group(1).trim();
            if (IMG_SRC_PT.matcher(src).matches()) {
                imgList.add(src.replaceAll("_[023456789]\\.", "_1."));
            } else {
                imgList.add(src);
            }
        }
        rtopic = recommendDao.getByTopicId(topicId);
        if (rtopic == null) {
            rtopic = new Recommendation();
            final String shotPostText = PostUtils.shortPostText(post, 120);
            final String description = StringUtils.isBlank(shotPostText) ? post.getSubject() : shotPostText;
            final Forum forum = ForumRepository.getForum(topic.getForumId());
            rtopic.setType(forum.getType() == Forum.TYPE_TEAM ? Recommendation.TYPE_INDEX_TEAM : Recommendation.TYPE_INDEX_IMG);
            rtopic.setTitle(topic.getTitle());
            rtopic.setTopicId(topic.getId());
            rtopic.setDesc(description);
            if (imgList.size() > 0) {
                rtopic.setImageUrl(imgList.iterator().next());
            }
        } else {
            imgList.add(rtopic.getImageUrl());
        }

        final ActionContext context = ServletActionContext.getContext();
        final boolean hasImg = imgList.size() > 0;
        context.put("imgList", imgList);
        context.put("hasImg", hasImg);
        context.put("coverImgType", hasImg ? "fromPost" : "upload");
        return SUCCESS;
    }

    @Action(value = "save", interceptorRefs = {
            @InterceptorRef("tokenSession"),
            @InterceptorRef(value = "fileUpload", params = { "allowedExtensions ", ".gif,.jpg,.png", "allowedTypes",
                    "image/png,image/gif,image/jpeg,image/pjpeg" }), @InterceptorRef("defaultStackIgearbook") }, results = { @Result(name = SUCCESS,
            location = "/", type = "redirect") })
    public String save() {
        Topic topic = TopicRepository.getTopic(new Topic(rtopic.getTopicId()));
        if (topic == null) {
            topic = DataAccessDriver.getInstance().newTopicDAO().selectById(rtopic.getTopicId());
        }

        final UserSession userSession = SessionFacade.getUserSession();
        final boolean isModerator = userSession.isModerator(topic.getForumId());
        final boolean canEditTeam = isModerator || userSession.isAdmin() || userSession.isWebAdmin();
        if (!canEditTeam) {
            return ERROR;
        }

        if ("upload".equals(coverImgType)) {
            final ImageCommon imgCommon = new ImageCommon(upload, uploadFileName, 0, Lists.newArrayList(ImageSize.SIZE_480));
            final Map<ImageSize, ImageVo> imgMap = imgCommon.doUpload();
            ImageVo imageVo = imgMap.get(ImageSize.ORIGINAL);
            if (imgMap.containsKey(ImageSize.SIZE_480)) {
                imageVo = imgMap.get(ImageSize.SIZE_480);
            }
            rtopic.setImageUrl(imageVo.getUrl());
        }

        final boolean isNew = (rtopic.getId() == 0);
        final int userId = SessionFacade.getUserSession().getUserId();
        final User user = DataAccessDriver.getInstance().newUserDAO().selectById(userId);

        final Date now = new Date();
        rtopic.setLastUpdateBy(user);
        rtopic.setLastUpdateTime(now);

        if (isNew) {
            rtopic.setCreateBy(user);
            rtopic.setCreateTime(now);
            recommendDao.add(rtopic);
        } else {
            recommendDao.update(rtopic);
        }

        return SUCCESS;
    }

    @Override
    public void setId(final int id) {
        this.id = id;
    }

    @Override
    public int getId() {
        return id;
    }

    public int getTopicId() {
        return topicId;
    }

    public void setTopicId(final int topicId) {
        this.topicId = topicId;
    }

    public Recommendation getRtopic() {
        return rtopic;
    }

    public void setRtopic(final Recommendation rtopic) {
        this.rtopic = rtopic;
    }

    public PaginationData<Recommendation> getData() {
        return data;
    }

    public void setData(final PaginationData<Recommendation> data) {
        this.data = data;
    }

    public List<Integer> getSelectedRtopics() {
        return selectedRtopics;
    }

    public void setSelectedRtopics(final List<Integer> selectedRtopics) {
        this.selectedRtopics = selectedRtopics;
    }

    public File getUpload() {
        return upload;
    }

    public void setUpload(final File upload) {
        this.upload = upload;
    }

    public String getUploadFileName() {
        return uploadFileName;
    }

    public void setUploadFileName(final String uploadFileName) {
        this.uploadFileName = uploadFileName;
    }

    public String getCoverImgType() {
        return coverImgType;
    }

    public void setCoverImgType(final String coverImgType) {
        this.coverImgType = coverImgType;
    }

}
