package com.igearbook.action;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.jforum.SessionFacade;
import net.jforum.entities.Category;
import net.jforum.entities.Forum;
import net.jforum.entities.MostUsersEverOnline;
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
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Lists;
import com.igearbook.dao.RecommendDao;
import com.igearbook.entities.PaginationData;
import com.igearbook.entities.Recommendation;
import com.opensymphony.xwork2.ActionContext;

@Namespace("/portal")
public class PortalAction extends BaseAction {
    private static final long serialVersionUID = 7587622153127430L;

    private int type;
    private PaginationData<Recommendation> pageData;

    @Autowired
    private RecommendDao recommendDao;

    public void setRecommendDao(final RecommendDao recommendDao) {
        this.recommendDao = recommendDao;
    }

    public int getType() {
        return type;
    }

    public void setType(final int type) {
        this.type = type;
    }

    public PaginationData<Recommendation> getPageData() {
        return pageData;
    }

    public void setPageData(final PaginationData<Recommendation> pageData) {
        this.pageData = pageData;
    }

    @Action(value = "index", results = { @Result(name = SUCCESS, location = "portal_index.ftl") })
    public String index() {
        final ActionContext context = ServletActionContext.getContext();

        context.put("topicsPerPage", new Integer(SystemGlobals.getIntValue(ConfigKeys.TOPICS_PER_PAGE)));
        context.put("totalMessages", new Integer(ForumRepository.getTotalMessages()));
        context.put("totalRegisteredUsers", ForumRepository.totalUsers());
        context.put("lastUser", ForumRepository.lastRegisteredUser());

        // Online Users
        context.put("totalOnlineUsers", new Integer(SessionFacade.size()));
        final int aid = SystemGlobals.getIntValue(ConfigKeys.ANONYMOUS_USER_ID);

        final List<UserSession> onlineUsersList = SessionFacade.getLoggedSessions();
        // If there are only guest users, then just register a single one.
        // In any other situation, we do not show the "guest" username
        if (onlineUsersList.size() == 0) {
            final UserSession us = new UserSession();
            us.setUserId(aid);
            us.setUsername(I18n.getMessage("Guest"));

            onlineUsersList.add(us);
        }

        final int registeredSize = SessionFacade.registeredSize();
        final int anonymousSize = SessionFacade.anonymousSize();
        final int totalOnlineUsers = registeredSize + anonymousSize;

        context.put("userSessions", onlineUsersList);
        context.put("totalOnlineUsers", totalOnlineUsers);
        context.put("totalRegisteredOnlineUsers", registeredSize);
        context.put("totalAnonymousUsers", anonymousSize);

        // Most users ever online
        final MostUsersEverOnline mostUsersEverOnline = ForumRepository.getMostUsersEverOnline();

        if (totalOnlineUsers > mostUsersEverOnline.getTotal()) {
            mostUsersEverOnline.setTotal(totalOnlineUsers);
            mostUsersEverOnline.setTimeInMillis(System.currentTimeMillis());

            ForumRepository.updateMostUsersEverOnline(mostUsersEverOnline);
        }

        context.put("mostUsersEverOnline", mostUsersEverOnline);
        final List<Recommendation> portalRecommends = recommendDao.listByTypeByLimit(Recommendation.TYPE_INDEX_IMG, 4);

        if (portalRecommends.size() > 0) {
            context.put("recommendTopic", portalRecommends.get(0));
        }
        if (portalRecommends.size() > 3) {
            final List<Recommendation> igearbookTopics = portalRecommends.subList(1, 4);
            context.put("igearbookTopics", igearbookTopics);
        }
        final List<Recommendation> teamRecommends = recommendDao.listByTypeByLimit(Recommendation.TYPE_INDEX_TEAM, 9);
        context.put("teamRecommends", teamRecommends);
        context.put("topicsPerPage", SystemGlobals.getIntValue(ConfigKeys.TOPICS_PER_PAGE));
        context.put("recentTopics", TopicRepository.getRecentTopics());
        context.put("recentRepliedTopics", TopicRepository.getRecentRepliedTopics());

        final List<Category> allCategories = ForumRepository.getAllCategories(SessionFacade.getUserSession().getUserId());
        final List<Category> categories = Lists.newArrayList();
        for (final Category category : allCategories) {
            if (category.getType() == 1) {
                categories.add(category);
            }
        }
        final List<Forum> hotTeams = Lists.newArrayList();
        for (final Category category : categories) {
            hotTeams.addAll(category.getForums());
        }
        Collections.sort(hotTeams, new Comparator<Forum>() {
            @Override
            public int compare(final Forum o1, final Forum o2) {
                return o2.getLastPostId() - o1.getLastPostId();
            }
        });
        context.put("hotTeams", hotTeams);

        return SUCCESS;
    }

    @Action(value = "recommend-more", results = { @Result(name = SUCCESS, location = "portal_recommend_more.ftl") })
    public String recommendMore() {
        this.pageData = recommendDao.doPaginationByType(type, getPaginationParams());
        final List<Category> categories = Lists.newArrayList();

        final List<Category> allCategories = ForumRepository.getAllCategories(SessionFacade.getUserSession().getUserId());
        for (final Category category : allCategories) {
            if (category.getType() == 1) {
                categories.add(category);
            }
        }
        final List<Forum> hotTeams = Lists.newArrayList();
        for (final Category category : categories) {
            hotTeams.addAll(category.getForums());
        }
        Collections.sort(hotTeams, new Comparator<Forum>() {
            @Override
            public int compare(final Forum o1, final Forum o2) {
                return o2.getLastPostId() - o1.getLastPostId();
            }
        });
        this.getContext().put("hotTeams", hotTeams);
        return SUCCESS;
    }

}
