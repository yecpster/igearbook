package com.igearbook.action;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.jforum.SessionFacade;
import net.jforum.entities.Category;
import net.jforum.entities.Forum;
import net.jforum.entities.MostUsersEverOnline;
import net.jforum.entities.Recommendation;
import net.jforum.entities.UserSession;
import net.jforum.repository.ForumRepository;
import net.jforum.repository.TopicRepository;
import net.jforum.util.I18n;
import net.jforum.util.preferences.ConfigKeys;
import net.jforum.util.preferences.SystemGlobals;

import org.apache.struts2.ServletActionContext;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;

import com.google.common.collect.Lists;
import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionSupport;

@Namespace("/portal")
public class PortalAction extends ActionSupport {
    private static final long serialVersionUID = 7587622153127430L;

    @Action(value = "index", results = { @Result(name = SUCCESS, location = "portal_index.ftl") })
    public String index() {
        ActionContext context = ServletActionContext.getContext();

        context.put("topicsPerPage", new Integer(SystemGlobals.getIntValue(ConfigKeys.TOPICS_PER_PAGE)));
        context.put("totalMessages", new Integer(ForumRepository.getTotalMessages()));
        context.put("totalRegisteredUsers", ForumRepository.totalUsers());
        context.put("lastUser", ForumRepository.lastRegisteredUser());

        // Online Users
        context.put("totalOnlineUsers", new Integer(SessionFacade.size()));
        int aid = SystemGlobals.getIntValue(ConfigKeys.ANONYMOUS_USER_ID);

        List<UserSession> onlineUsersList = SessionFacade.getLoggedSessions();
        // If there are only guest users, then just register
        // a single one. In any other situation, we do not
        // show the "guest" username
        if (onlineUsersList.size() == 0) {
            UserSession us = new UserSession();
            us.setUserId(aid);
            us.setUsername(I18n.getMessage("Guest"));

            onlineUsersList.add(us);
        }

        int registeredSize = SessionFacade.registeredSize();
        int anonymousSize = SessionFacade.anonymousSize();
        int totalOnlineUsers = registeredSize + anonymousSize;

        context.put("userSessions", onlineUsersList);
        context.put("totalOnlineUsers", totalOnlineUsers);
        context.put("totalRegisteredOnlineUsers", registeredSize);
        context.put("totalAnonymousUsers", anonymousSize);

        // Most users ever online
        MostUsersEverOnline mostUsersEverOnline = ForumRepository.getMostUsersEverOnline();

        if (totalOnlineUsers > mostUsersEverOnline.getTotal()) {
            mostUsersEverOnline.setTotal(totalOnlineUsers);
            mostUsersEverOnline.setTimeInMillis(System.currentTimeMillis());

            ForumRepository.updateMostUsersEverOnline(mostUsersEverOnline);
        }

        context.put("mostUsersEverOnline", mostUsersEverOnline);

        List<Recommendation> portalRecommends = TopicRepository.getRecommendTopics(Recommendation.TYPE_INDEX_IMG);
        if (portalRecommends != null && portalRecommends.size() > 0) {

            context.put("recommendTopic", portalRecommends.get(0));
        }
        if (portalRecommends != null && portalRecommends.size() > 3) {
            List<Recommendation> igearbookTopics = Lists.newArrayList();
            igearbookTopics.add(portalRecommends.get(1));
            igearbookTopics.add(portalRecommends.get(2));
            igearbookTopics.add(portalRecommends.get(3));
            context.put("igearbookTopics", igearbookTopics);
        }
        List<Recommendation> teamRecommends = TopicRepository.getRecommendTopics(Recommendation.TYPE_INDEX_TEAM);
        context.put("teamRecommends", teamRecommends);
        context.put("recentTopics", TopicRepository.getRecentTopics());
        context.put("hotTopics", TopicRepository.getHottestTopics());

        List<Category> allCategories = ForumRepository.getAllCategories(SessionFacade.getUserSession().getUserId());
        List<Category> categories = Lists.newArrayList();
        for (Category category : allCategories) {
            if (category.getType() == 1) {
                categories.add(category);
            }
        }
        List<Forum> hotTeams = Lists.newArrayList();
        for (Category category : categories) {
            hotTeams.addAll(category.getForums());
        }
        Collections.sort(hotTeams, new Comparator<Forum>() {
            @Override
            public int compare(Forum o1, Forum o2) {
                return o2.getLastPostId() - o1.getLastPostId();
            }
        });
        context.put("hotTeams", hotTeams);

        return SUCCESS;
    }

}
