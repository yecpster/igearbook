package com.igearbook.action;

import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.jforum.SessionFacade;
import net.jforum.dao.DataAccessDriver;
import net.jforum.dao.PostDAO;
import net.jforum.dao.RecommendationDAO;
import net.jforum.entities.Forum;
import net.jforum.entities.Post;
import net.jforum.entities.Recommendation;
import net.jforum.entities.Topic;
import net.jforum.entities.User;
import net.jforum.entities.UserSession;
import net.jforum.repository.ForumRepository;
import net.jforum.repository.PostRepository;
import net.jforum.repository.SecurityRepository;
import net.jforum.repository.TopicRepository;
import net.jforum.security.SecurityConstants;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;

import com.google.common.collect.Lists;
import com.igearbook.util.HtmlUtil;
import com.opensymphony.xwork2.ActionContext;

@Namespace("/post")
public class PostAction extends BaseAction {
    private static final long serialVersionUID = 8123243588174068662L;
    private static final Pattern IMG_PT = Pattern.compile("<img.*?src\\s*=\\s*['\"](.+?)['\"]", Pattern.CASE_INSENSITIVE);
    private int id;
    private int topicId;
    private Recommendation rtopic;

    @Action(value = "manageRecommend", results = { @Result(name = SUCCESS, location = "post_recommend_list.ftl") })
    public String manageRecommend() {
        UserSession userSession = SessionFacade.getUserSession();
        boolean canEditTeam = userSession.isSuperModerator() || userSession.isAdmin();
        if (!canEditTeam) {
            return ERROR;
        }
        int start = 0;
        RecommendationDAO recommendationDao = DataAccessDriver.getInstance().newRecommendationDAO();
        recommendationDao.selectAllByLimit(start, 1);
        return SUCCESS;
    }

    @Action(value = "recommend", results = { @Result(name = SUCCESS, location = "post_recommend_form.ftl") })
    public String recommend() {
        Topic topic = TopicRepository.getTopic(new Topic(topicId));
        if (topic == null) {
            topic = DataAccessDriver.getInstance().newTopicDAO().selectById(topicId);
        }

        boolean canEditTeam = SecurityRepository.canAccess(SecurityConstants.PERM_MODERATION_FORUMS, String.valueOf(topic.getForumId()));
        if (!canEditTeam && !SessionFacade.getUserSession().isAdmin()) {
            return ERROR;
        }
        PostDAO postDao = DataAccessDriver.getInstance().newPostDAO();

        Post post = PostRepository.selectAllByTopicByLimit(topicId, 0, 1).get(0);
        String text = post.getText();
        Matcher matcher = IMG_PT.matcher(text);
        List<String> imgList = Lists.newArrayList();
        while (matcher.find()) {
            imgList.add(matcher.group(1));
        }
        rtopic = postDao.selectRecommendByTopicId(topicId);
        if (rtopic == null) {
            rtopic = new Recommendation();
            String description = HtmlUtil.removeAllHTML(text);
            if (StringUtils.isNotBlank(description)) {
                description = description.trim();
                description = description.replaceAll("\\s+", " ");
                if (description.length() > 120) {
                    description = description.substring(0, 120);
                }
            } else {
                description = post.getSubject();
            }

            rtopic.setTitle(topic.getTitle());
            rtopic.setTopicId(topic.getId());
            rtopic.setDesc(description);
        }

        ActionContext context = ServletActionContext.getContext();
        context.put("imgList", imgList);
        context.put("hasImg", imgList.size() > 0);
        return SUCCESS;
    }

    @Action(value = "recommendSave", results = { @Result(name = SUCCESS, location = "/", type = "redirect") })
    public String recommendSave() {
        Topic topic = TopicRepository.getTopic(new Topic(rtopic.getTopicId()));
        if (topic == null) {
            topic = DataAccessDriver.getInstance().newTopicDAO().selectById(rtopic.getTopicId());
        }

        boolean canEditTeam = SecurityRepository.canAccess(SecurityConstants.PERM_MODERATION_FORUMS, String.valueOf(topic.getForumId()));
        if (!canEditTeam && !SessionFacade.getUserSession().isAdmin()) {
            return ERROR;
        }
        boolean isNew = (rtopic.getId() == 0);
        int userId = SessionFacade.getUserSession().getUserId();
        Forum forum = ForumRepository.getForum(topic.getForumId());
        int type = Recommendation.TYPE_INDEX_IMG;
        if (forum.getType() == Forum.TYPE_TEAM) {
            type = Recommendation.TYPE_INDEX_TEAM;
        }
        User user = DataAccessDriver.getInstance().newUserDAO().selectById(userId);

        PostDAO postDao = DataAccessDriver.getInstance().newPostDAO();
        Date now = new Date();
        rtopic.setLastUpdateBy(user);
        rtopic.setLastUpdateTime(now);

        if (isNew) {
            rtopic.setCreateBy(user);
            rtopic.setCreateTime(now);
            rtopic.setType(type);
            postDao.saveRecommend(rtopic);
        } else {
            postDao.updateRecommend(rtopic);
        }

        TopicRepository.loadRecommendTopics(type);
        return SUCCESS;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public int getTopicId() {
        return topicId;
    }

    public void setTopicId(int topicId) {
        this.topicId = topicId;
    }

    public Recommendation getRtopic() {
        return rtopic;
    }

    public void setRtopic(Recommendation rtopic) {
        this.rtopic = rtopic;
    }

}
