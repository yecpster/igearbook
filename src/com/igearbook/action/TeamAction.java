package com.igearbook.action;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import net.jforum.SessionFacade;
import net.jforum.dao.CategoryDAO;
import net.jforum.dao.DataAccessDriver;
import net.jforum.dao.ForumDAO;
import net.jforum.dao.GroupDAO;
import net.jforum.dao.GroupSecurityDAO;
import net.jforum.dao.PostDAO;
import net.jforum.dao.TopicDAO;
import net.jforum.dao.UserDAO;
import net.jforum.entities.Category;
import net.jforum.entities.Forum;
import net.jforum.entities.Group;
import net.jforum.entities.Post;
import net.jforum.entities.Topic;
import net.jforum.entities.User;
import net.jforum.entities.UserSession;
import net.jforum.repository.ForumRepository;
import net.jforum.repository.RolesRepository;
import net.jforum.repository.SecurityRepository;
import net.jforum.security.PermissionControl;
import net.jforum.security.Role;
import net.jforum.security.RoleValue;
import net.jforum.security.RoleValueCollection;
import net.jforum.security.SecurityConstants;
import net.jforum.util.I18n;
import net.jforum.util.SafeHtml;
import net.jforum.util.preferences.ConfigKeys;
import net.jforum.util.preferences.SystemGlobals;
import net.jforum.view.forum.ModerationHelper;
import net.jforum.view.forum.common.ForumCommon;
import net.jforum.view.forum.common.TopicsCommon;
import net.jforum.view.forum.common.ViewCommon;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.igearbook.dao.UserDao;
import com.igearbook.entities.PaginationData;
import com.igearbook.entities.PaginationParams;
import com.igearbook.util.DateUtil;
import com.igearbook.util.ImageHelper;
import com.opensymphony.xwork2.ActionContext;

@Namespace("/team")
public class TeamAction extends BaseAction {
    private static final long serialVersionUID = 7587622153127430L;

    private static final int WIDTH_RANGE = 80;

    private static final int HEIGHT_RANGE = 80;

    private Forum team;

    private int teamId;

    private int userId;

    private int[] banPostUserIds;

    private File upload;

    private String uploadContentType;

    private String uploadFileName;

    @Autowired
    private UserDao userDao;

    public void setUserDao(final UserDao userDao) {
        this.userDao = userDao;
    }

    @Action(value = "list", results = { @Result(name = SUCCESS, location = "team_list.ftl") })
    public String list() {
        final ActionContext context = ServletActionContext.getContext();
        final boolean canCreateTeam = SecurityRepository.canAccess(SecurityConstants.PERM_TEAMFORUM_CREATE);
        context.put("canCreateTeam", canCreateTeam);

        final List<Category> allCategories = ForumRepository.getAllCategories(SessionFacade.getUserSession().getUserId());
        final List<Category> categories = Lists.newArrayList();
        for (final Category category : allCategories) {
            if (category.getType() == 1) {
                categories.add(category);
            }
        }
        context.put("categories", categories);

        final List<Forum> rankTeams = Lists.newArrayList();
        for (final Category category : categories) {
            rankTeams.addAll(category.getForums());
        }
        Collections.sort(rankTeams, new Comparator<Forum>() {
            @Override
            public int compare(final Forum o1, final Forum o2) {
                return o2.getTotalPosts() - o1.getTotalPosts();
            }
        });
        context.put("rankTeams", rankTeams);

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

    // @Action(value = "moderation", results = { @Result(name = SUCCESS, location = "team_forum.ftl") })
    public String moderation() {
        final boolean canEditTeam = SecurityRepository.canAccess(SecurityConstants.PERM_MODERATION_FORUMS, String.valueOf(teamId));
        if (canEditTeam || SessionFacade.getUserSession().isAdmin()) {
            final ActionContext context = ServletActionContext.getContext();
            context.put("openModeration", true);
            return this.show();
        } else {
            return PERMISSION;
        }
    }

    @Action(value = "join", results = { @Result(name = SUCCESS, location = "show", type = "chain") })
    public String joinTeam() {
        final boolean isTeamMember = SecurityRepository.canAccess(SecurityConstants.PERM_TEAMFORUM_USER, String.valueOf(teamId))
                || SecurityRepository.canAccess(SecurityConstants.PERM_TEAMFORUM_OWNER, String.valueOf(teamId))
                || SecurityRepository.canAccess(SecurityConstants.PERM_TEAMFORUM_USER_CANDIDATE, String.valueOf(teamId))
                || SecurityRepository.canAccess(SecurityConstants.PERM_MODERATION_FORUMS, String.valueOf(teamId));
        final boolean isBanUser = SecurityRepository.canAccess(SecurityConstants.PERM_TEAMFORUM_USER_BAN2POST, String.valueOf(teamId));
        if (!isTeamMember && !isBanUser) {
            final int userId = SessionFacade.getUserSession().getUserId();
            final GroupDAO groupDao = DataAccessDriver.getInstance().newGroupDAO();
            final UserDAO userDao = DataAccessDriver.getInstance().newUserDAO();
            Group teamUserGorup = groupDao.getEntitlementGroup(SecurityConstants.PERM_TEAMFORUM_USER, teamId);
            if (teamUserGorup == null) {
                teamUserGorup = this.createUserGroup(groupDao, teamId);
            }
            userDao.addToGroup(userId, new int[] { teamUserGorup.getId() });
            SecurityRepository.clean();
            RolesRepository.clear();
        }
        final ActionContext context = ServletActionContext.getContext();
        context.put("message", "你已经成功加入此群组");
        return SUCCESS;
    }

    @Action(value = "manageUser", results = { @Result(name = SUCCESS, location = "team_manageUser.ftl") })
    public String manageUser() {
        final boolean isTeamOwner = SecurityRepository.canAccess(SecurityConstants.PERM_TEAMFORUM_OWNER, String.valueOf(teamId));
        if (!isTeamOwner) {
            return PERMISSION;
        }
        this.team = ForumRepository.getForum(teamId);
        if (this.team == null) {
            final ForumDAO forumDao = DataAccessDriver.getInstance().newForumDAO();
            this.team = forumDao.selectById(teamId);
            ForumRepository.addForum(this.team);
        }

        final ActionContext context = ServletActionContext.getContext();
        final GroupDAO groupDao = DataAccessDriver.getInstance().newGroupDAO();
        final UserDAO userDao = DataAccessDriver.getInstance().newUserDAO();

        final int start = ViewCommon.getStartPage();
        final int usersPerPage = SystemGlobals.getIntValue(ConfigKeys.USERS_PER_PAGE);
        final Group teamUserGorup = groupDao.getEntitlementGroup(SecurityConstants.PERM_TEAMFORUM_USER, teamId);
        contextToPagination(start, userDao.getTotalUsersByGroup(teamUserGorup.getId()), usersPerPage);
        final List<User> users = userDao.selectAllByGroup(teamUserGorup.getId(), start, Integer.MAX_VALUE);

        Group banUserGorup = groupDao.getEntitlementGroup(SecurityConstants.PERM_TEAMFORUM_USER_BAN2POST, teamId);
        if (banUserGorup == null) {
            banUserGorup = this.createBanToPostUserGroup(groupDao, teamId);
        }
        final List<User> banUsers = userDao.selectAllByGroup(banUserGorup.getId(), 0, 100);

        final Group moderatorsGorup = groupDao.getEntitlementGroup(SecurityConstants.PERM_MODERATION_FORUMS, teamId);
        final List<User> moderators = userDao.selectAllByGroup(moderatorsGorup.getId(), 0, 10);

        final Group ownerGorup = groupDao.getEntitlementGroup(SecurityConstants.PERM_TEAMFORUM_OWNER, teamId);
        final List<User> owners = userDao.selectAllByGroup(ownerGorup.getId(), 0, 5);

        context.put("users", users);
        context.put("banUsers", banUsers);
        context.put("moderators", moderators);
        context.put("owner", owners.get(0));
        return SUCCESS;
    }

    @Action(value = "setAdmin", results = { @Result(name = SUCCESS, location = "manageUser.action", type = "redirect", params = { "teamId",
            "${teamId}" }) })
    public String setAdmin() {
        final boolean isTeamOwner = SecurityRepository.canAccess(SecurityConstants.PERM_TEAMFORUM_OWNER, String.valueOf(teamId));
        final boolean isUserValid = SecurityRepository.canAccess(this.userId, SecurityConstants.PERM_TEAMFORUM_USER, String.valueOf(teamId));
        if (!isTeamOwner || !isUserValid) {
            return PERMISSION;
        }

        final GroupDAO groupDao = DataAccessDriver.getInstance().newGroupDAO();
        final UserDAO userDao = DataAccessDriver.getInstance().newUserDAO();

        final Group teamUserGorup = groupDao.getEntitlementGroup(SecurityConstants.PERM_TEAMFORUM_USER, teamId);
        final Group moderatorsGorup = groupDao.getEntitlementGroup(SecurityConstants.PERM_MODERATION_FORUMS, teamId);
        userDao.removeFromGroup(this.userId, new int[] { teamUserGorup.getId() });
        userDao.addToGroup(this.userId, new int[] { moderatorsGorup.getId() });

        SecurityRepository.clean();
        RolesRepository.clear();
        return SUCCESS;
    }

    @Action(value = "cancelAdmin", results = { @Result(name = SUCCESS, location = "manageUser.action", type = "redirect", params = { "teamId",
            "${teamId}" }) })
    public String cancelAdmin() {
        final boolean isTeamOwner = SecurityRepository.canAccess(SecurityConstants.PERM_TEAMFORUM_OWNER, String.valueOf(teamId));
        final boolean isUserValid = SecurityRepository.canAccess(this.userId, SecurityConstants.PERM_MODERATION_FORUMS, String.valueOf(teamId));
        if (!isTeamOwner || !isUserValid) {
            return PERMISSION;
        }

        final GroupDAO groupDao = DataAccessDriver.getInstance().newGroupDAO();
        final UserDAO userDao = DataAccessDriver.getInstance().newUserDAO();

        final Group moderatorsGorup = groupDao.getEntitlementGroup(SecurityConstants.PERM_MODERATION_FORUMS, teamId);
        final Group teamUserGorup = groupDao.getEntitlementGroup(SecurityConstants.PERM_TEAMFORUM_USER, teamId);
        userDao.removeFromGroup(this.userId, new int[] { moderatorsGorup.getId() });
        userDao.addToGroup(this.userId, new int[] { teamUserGorup.getId() });

        SecurityRepository.clean();
        RolesRepository.clear();
        return SUCCESS;
    }

    @Action(value = "banPostUsers", results = { @Result(name = SUCCESS, location = "manageUser.action", type = "redirect", params = { "teamId",
            "${teamId}" }) })
    public String banPostUsers() {
        final boolean isTeamOwner = SecurityRepository.canAccess(SecurityConstants.PERM_TEAMFORUM_OWNER, String.valueOf(teamId));
        if (!isTeamOwner) {
            return PERMISSION;
        }
        if (banPostUserIds == null || banPostUserIds.length == 0) {
            return SUCCESS;
        }
        for (final int banUserId : banPostUserIds) {
            final boolean isUserValid = SecurityRepository.canAccess(banUserId, SecurityConstants.PERM_TEAMFORUM_USER, String.valueOf(teamId));
            if (!isUserValid) {
                return ERROR;
            }
        }
        final GroupDAO groupDao = DataAccessDriver.getInstance().newGroupDAO();
        final UserDAO userDao = DataAccessDriver.getInstance().newUserDAO();

        final Group teamUserGorup = groupDao.getEntitlementGroup(SecurityConstants.PERM_TEAMFORUM_USER, teamId);
        final Group teamUserBanToPostGorup = groupDao.getEntitlementGroup(SecurityConstants.PERM_TEAMFORUM_USER_BAN2POST, teamId);
        for (final int banUserId : banPostUserIds) {
            userDao.removeFromGroup(banUserId, new int[] { teamUserGorup.getId() });
            userDao.addToGroup(banUserId, new int[] { teamUserBanToPostGorup.getId() });
        }

        SecurityRepository.clean();
        RolesRepository.clear();

        return SUCCESS;
    }

    @Action(value = "unbanUser", results = { @Result(name = SUCCESS, location = "manageUser.action", type = "redirect", params = { "teamId",
            "${teamId}" }) })
    public String unbanPostUsers() {
        final boolean isTeamOwner = SecurityRepository.canAccess(SecurityConstants.PERM_TEAMFORUM_OWNER, String.valueOf(teamId));
        if (!isTeamOwner) {
            return PERMISSION;
        }
        final boolean isUserValid = SecurityRepository.canAccess(userId, SecurityConstants.PERM_TEAMFORUM_USER_BAN2POST, String.valueOf(teamId));
        if (!isUserValid) {
            return ERROR;
        }
        final GroupDAO groupDao = DataAccessDriver.getInstance().newGroupDAO();
        final UserDAO userDao = DataAccessDriver.getInstance().newUserDAO();

        final Group teamUserGorup = groupDao.getEntitlementGroup(SecurityConstants.PERM_TEAMFORUM_USER, teamId);
        final Group teamUserBanToPostGorup = groupDao.getEntitlementGroup(SecurityConstants.PERM_TEAMFORUM_USER_BAN2POST, teamId);
        userDao.removeFromGroup(userId, new int[] { teamUserBanToPostGorup.getId() });
        userDao.addToGroup(userId, new int[] { teamUserGorup.getId() });

        SecurityRepository.clean();
        RolesRepository.clear();

        return SUCCESS;
    }

    @Action(value = "members", results = { @Result(name = SUCCESS, location = "team_members.ftl") })
    public String members() {
        final ActionContext context = ServletActionContext.getContext();
        final GroupDAO groupDao = DataAccessDriver.getInstance().newGroupDAO();

        // The user can access this team?
        final Forum team = ForumRepository.getForum(teamId);

        if (team == null || team.getType() != 1 || !ForumRepository.isCategoryAccessible(team.getCategoryId())) {
            new ModerationHelper().denied(I18n.getMessage("ForumListing.denied"));
            return ERROR;
        }

        context.put("team", team);

        final Group teamUserGorup = groupDao.getEntitlementGroup(SecurityConstants.PERM_TEAMFORUM_USER, teamId);
        final PaginationParams paginationParams = getPaginationParams();
        final Map<String, Object> queryParams = Maps.newHashMap();
        queryParams.put("groupId", teamUserGorup.getId());
        paginationParams.setQueryParams(queryParams);
        final PaginationData<User> pgData = userDao.listByGroup(paginationParams);
        context.put("pgData", pgData);
        return SUCCESS;
    }

    @Action(value = "show", results = { @Result(name = SUCCESS, location = "team_show.ftl") })
    public String show() {
        final ActionContext context = ServletActionContext.getContext();
        final ForumDAO fm = DataAccessDriver.getInstance().newForumDAO();
        final GroupDAO groupDao = DataAccessDriver.getInstance().newGroupDAO();
        final UserDAO userDao = DataAccessDriver.getInstance().newUserDAO();

        // The user can access this team?
        final Forum team = ForumRepository.getForum(teamId);

        if (team == null || team.getType() != 1 || !ForumRepository.isCategoryAccessible(team.getCategoryId())) {
            new ModerationHelper().denied(I18n.getMessage("ForumListing.denied"));
            return ERROR;
        }

        final int start = ViewCommon.getStartPage();

        final List<Topic> tmpTopics = TopicsCommon.topicsByForum(teamId, start);

        final TopicDAO topicDao = DataAccessDriver.getInstance().newTopicDAO();
        final PostDAO postDao = DataAccessDriver.getInstance().newPostDAO();
        final List<Topic> topics = topicDao.selectByForumByTypeByLimit(teamId, Topic.TYPE_ANNOUNCE, 0, 1);
        if (topics.size() > 0) {
            final Topic announceTopic = topics.get(0);
            final Post announcePost = postDao.selectById(announceTopic.getFirstPostId());
            final String announcement = announcePost.getText();
            if (StringUtils.isNotBlank(announcement)) {
                context.put("announcement", announcement);
            }
        }

        // Moderation
        final UserSession userSession = SessionFacade.getUserSession();
        final boolean isLogged = SessionFacade.isLogged();
        final boolean isModerator = userSession.isModerator(teamId);
        context.put("moderator", isLogged && isModerator);

        context.put("attachmentsEnabled", SecurityRepository.canAccess(SecurityConstants.PERM_ATTACHMENTS_ENABLED, Integer.toString(teamId))
                || SecurityRepository.canAccess(SecurityConstants.PERM_ATTACHMENTS_DOWNLOAD));

        context.put("topics", TopicsCommon.prepareTopics(tmpTopics));
        context.put("allCategories", ForumCommon.getAllCategoriesAndForums(false));
        context.put("team", team);
        context.put("rssEnabled", SystemGlobals.getBoolValue(ConfigKeys.RSS_ENABLED));
        context.put("pageTitle", team.getName());
        context.put("replyOnly", !SecurityRepository.canAccess(SecurityConstants.PERM_NEW_POST, Integer.toString(team.getId())));

        context.put("readonly", !SecurityRepository.canAccess(SecurityConstants.PERM_REPLY, Integer.toString(teamId)));

        context.put("watching", fm.isUserSubscribed(teamId, userSession.getUserId()));

        final boolean isTeamMember = SecurityRepository.canAccess(SecurityConstants.PERM_TEAMFORUM_USER, String.valueOf(teamId))
                || SecurityRepository.canAccess(SecurityConstants.PERM_TEAMFORUM_OWNER, String.valueOf(teamId))
                || SecurityRepository.canAccess(SecurityConstants.PERM_TEAMFORUM_USER_CANDIDATE, String.valueOf(teamId))
                || SecurityRepository.canAccess(SecurityConstants.PERM_MODERATION_FORUMS, String.valueOf(teamId));
        final boolean isBanUser = SecurityRepository.canAccess(SecurityConstants.PERM_TEAMFORUM_USER_BAN2POST, String.valueOf(teamId));
        final boolean isTeamOwner = SecurityRepository.canAccess(SecurityConstants.PERM_TEAMFORUM_OWNER, String.valueOf(teamId));

        context.put("isTeamMember", isTeamMember);
        context.put("isBanUser", isBanUser);
        context.put("isTeamOwner", isTeamOwner);
        context.put("avatarAllowExternalUrl", SystemGlobals.getBoolValue(ConfigKeys.AVATAR_ALLOW_EXTERNAL_URL));

        final Group ownerGorup = groupDao.getEntitlementGroup(SecurityConstants.PERM_TEAMFORUM_OWNER, teamId);
        final List<User> owners = userDao.selectAllByGroup(ownerGorup.getId(), 0, 5);
        final Group teamUserGorup = groupDao.getEntitlementGroup(SecurityConstants.PERM_TEAMFORUM_USER, teamId);
        int totalUsers = userDao.getTotalUsersByGroup(teamUserGorup.getId());
        final List<User> users = userDao.selectAllByGroup(teamUserGorup.getId(), 1, 9);

        final Group moderatorsGorup = groupDao.getEntitlementGroup(SecurityConstants.PERM_MODERATION_FORUMS, teamId);
        totalUsers += userDao.getTotalUsersByGroup(moderatorsGorup.getId()) + 1;
        final List<User> moderators = userDao.selectAllByGroup(moderatorsGorup.getId(), 0, 10);

        context.put("teanOwner", owners.get(0));
        context.put("users", users);
        context.put("moderators", moderators);
        context.put("totalUsers", totalUsers);

        // Pagination
        final int topicsPerPage = SystemGlobals.getIntValue(ConfigKeys.TOPICS_PER_PAGE);
        final int postsPerPage = SystemGlobals.getIntValue(ConfigKeys.POSTS_PER_PAGE);
        final int totalTopics = team.getTotalTopics();

        contextToPagination(start, totalTopics, topicsPerPage);
        context.put("postsPerPage", new Integer(postsPerPage));

        topicListingBase();

        return SUCCESS;
    }

    @Action(value = "editAnnounce", results = { @Result(name = SUCCESS, location = "team_announce_form.ftl") })
    public String editAnnounce() {
        final boolean canEditTeam = SecurityRepository.canAccess(SecurityConstants.PERM_MODERATION_FORUMS, String.valueOf(teamId));
        final UserSession userSession = SessionFacade.getUserSession();
        if (canEditTeam || userSession.isAdmin() || userSession.isWebAdmin()) {
            final ActionContext context = ServletActionContext.getContext();
            this.team = ForumRepository.getForum(teamId);
            final TopicDAO topicDao = DataAccessDriver.getInstance().newTopicDAO();
            final PostDAO postDao = DataAccessDriver.getInstance().newPostDAO();
            final List<Topic> topics = topicDao.selectByForumByTypeByLimit(teamId, Topic.TYPE_ANNOUNCE, 0, 1);
            if (topics.size() > 0) {
                final Topic announceTopic = topics.get(0);
                final Post announcePost = postDao.selectById(announceTopic.getFirstPostId());
                final String announcement = announcePost.getText();
                if (StringUtils.isNotBlank(announcement)) {
                    context.put("announcement", announcement);
                }
            }
            return SUCCESS;
        } else {
            return PERMISSION;
        }
    }

    @Action(value = "saveAnnounce", interceptorRefs = { @InterceptorRef("tokenSession"), @InterceptorRef("defaultStackIgearbook") },
            results = { @Result(name = SUCCESS, location = "show.action", type = "redirect", params = { "teamId", "${teamId}" }) })
    public String saveAnnounce() {
        teamId = team.getId();
        final boolean canEditTeam = SecurityRepository.canAccess(SecurityConstants.PERM_MODERATION_FORUMS, String.valueOf(teamId));
        final UserSession userSession = SessionFacade.getUserSession();
        if (!canEditTeam && !userSession.isAdmin() && !userSession.isWebAdmin()) {
            return PERMISSION;
        }
        boolean newTopic = true;
        final UserSession us = SessionFacade.getUserSession();
        final User u = DataAccessDriver.getInstance().newUserDAO().selectById(us.getUserId());

        final TopicDAO topicDao = DataAccessDriver.getInstance().newTopicDAO();
        final PostDAO postDao = DataAccessDriver.getInstance().newPostDAO();

        Topic t = null;
        Post p = null;

        final List<Topic> topics = topicDao.selectByForumByTypeByLimit(teamId, Topic.TYPE_ANNOUNCE, 0, 1);
        if (topics.size() > 0) {
            t = topics.get(0);
            newTopic = false;
            p = postDao.selectById(t.getFirstPostId());
        } else {
            t = new Topic(-1);
            t.setForumId(teamId);
            t.setType(Topic.TYPE_ANNOUNCE);
            p = new Post();
        }
        t.setTime(new Date());
        t.setTitle(p.getSubject());
        t.setModerated(false);
        t.setPostedBy(u);
        t.setFirstPostTime(ViewCommon.formatDate(t.getTime()));

        p.setForumId(teamId);
        p.setTime(new Date());
        p.setSubject("Announcement:" + new Date());
        p.setBbCodeEnabled(false);
        p.setSmiliesEnabled(false);
        p.setSignatureEnabled(false);
        p.setHtmlEnabled(false);

        final String ip = ServletActionContext.getRequest().getRemoteAddr();
        if (StringUtils.isNotBlank(ip)) {
            p.setUserIp(ip);
        } else {
            p.setUserIp("0.0.0.0");
        }
        p.setUserId(SessionFacade.getUserSession().getUserId());
        p.setText(new SafeHtml().makeSafe(ServletActionContext.getRequest().getParameter("announcement")));

        // Check the elapsed time since the last post from the user
        final int delay = SystemGlobals.getIntValue(ConfigKeys.POSTS_NEW_DELAY);
        if (delay > 0) {
            final Long lastPostTime = (Long) SessionFacade.getAttribute(ConfigKeys.LAST_POST_TIME);
            if (lastPostTime != null) {
                if (System.currentTimeMillis() < (lastPostTime.longValue() + delay)) {
                    return I18n.getMessage("PostForm.tooSoon");
                }
            }
        }

        // Currently for announcement we always update the existing topic and
        // post.
        if (newTopic) {
            final int topicId = topicDao.addNew(t);
            t.setId(topicId);
            p.setTopicId(topicId);
            final int postId = postDao.addNew(p);
            t.setFirstPostId(postId);
            t.setLastPostId(postId);
        } else {
            postDao.update(p);
        }

        t.setLastPostBy(u);
        t.setLastPostDate(p.getTime());
        t.setLastPostTime(p.getFormatedTime());
        topicDao.update(t);

        if (delay > 0) {
            SessionFacade.setAttribute(ConfigKeys.LAST_POST_TIME, new Long(System.currentTimeMillis()));
        }
        return SUCCESS;
    }

    // @Action(value = "forum", results = { @Result(name = SUCCESS, location = "team_forum.ftl") })
    public String forum() {
        final ActionContext context = ServletActionContext.getContext();
        final ForumDAO fm = DataAccessDriver.getInstance().newForumDAO();

        // The user can access this team?
        final Forum team = ForumRepository.getForum(teamId);

        if (team == null || team.getType() != 1 || !ForumRepository.isCategoryAccessible(team.getCategoryId())) {
            new ModerationHelper().denied(I18n.getMessage("ForumListing.denied"));
            return ERROR;
        }

        final int start = ViewCommon.getStartPage();

        final List<Topic> tmpTopics = TopicsCommon.topicsByForum(teamId, start);

        // Moderation
        final UserSession userSession = SessionFacade.getUserSession();
        final boolean isLogged = SessionFacade.isLogged();
        final boolean isModerator = userSession.isModerator(teamId);
        context.put("moderator", isLogged && isModerator);

        context.put("attachmentsEnabled", SecurityRepository.canAccess(SecurityConstants.PERM_ATTACHMENTS_ENABLED, Integer.toString(teamId))
                || SecurityRepository.canAccess(SecurityConstants.PERM_ATTACHMENTS_DOWNLOAD));

        context.put("topics", TopicsCommon.prepareTopics(tmpTopics));
        context.put("allCategories", ForumCommon.getAllCategoriesAndForums(false));
        context.put("team", team);
        context.put("rssEnabled", SystemGlobals.getBoolValue(ConfigKeys.RSS_ENABLED));
        context.put("pageTitle", team.getName());
        context.put("replyOnly", !SecurityRepository.canAccess(SecurityConstants.PERM_NEW_POST, Integer.toString(team.getId())));
        context.put("readonly", !SecurityRepository.canAccess(SecurityConstants.PERM_REPLY, Integer.toString(teamId)));
        context.put("watching", fm.isUserSubscribed(teamId, userSession.getUserId()));

        // Pagination
        final int topicsPerPage = Integer.MAX_VALUE;// TODO
        final int postsPerPage = SystemGlobals.getIntValue(ConfigKeys.POSTS_PER_PAGE);
        final int totalTopics = team.getTotalTopics();

        contextToPagination(start, totalTopics, topicsPerPage);
        context.put("postsPerPage", new Integer(postsPerPage));

        topicListingBase();

        return SUCCESS;
    }

    public static void contextToPagination(final int start, final int totalRecords, final int recordsPerPage) {
        final ActionContext context = ServletActionContext.getContext();

        context.put("totalPages", new Double(Math.ceil((double) totalRecords / (double) recordsPerPage)));
        context.put("recordsPerPage", new Integer(recordsPerPage));
        context.put("totalRecords", new Integer(totalRecords));
        context.put("thisPage", new Double(Math.ceil((double) (start + 1) / (double) recordsPerPage)));
        context.put("start", new Integer(start));
    }

    public static void topicListingBase() {
        final ActionContext context = ServletActionContext.getContext();

        // Topic Types
        context.put("TOPIC_ANNOUNCE", new Integer(Topic.TYPE_ANNOUNCE));
        context.put("TOPIC_STICKY", new Integer(Topic.TYPE_STICKY));
        context.put("TOPIC_GOOD", new Integer(Topic.TYPE_GOOD));
        context.put("TOPIC_NORMAL", new Integer(Topic.TYPE_NORMAL));

        // Topic Status
        context.put("STATUS_LOCKED", new Integer(Topic.STATUS_LOCKED));
        context.put("STATUS_UNLOCKED", new Integer(Topic.STATUS_UNLOCKED));

        // Moderation
        final PermissionControl pc = SecurityRepository.get(SessionFacade.getUserSession().getUserId());

        context.put("can_remove_posts", pc.canAccess(SecurityConstants.PERM_MODERATION_POST_REMOVE));
        context.put("can_move_topics", pc.canAccess(SecurityConstants.PERM_MODERATION_TOPIC_MOVE));
        context.put("can_lockUnlock_topics", pc.canAccess(SecurityConstants.PERM_MODERATION_TOPIC_LOCK_UNLOCK));
        context.put("rssEnabled", SystemGlobals.getBoolValue(ConfigKeys.RSS_ENABLED));
    }

    @Action(value = "insert", results = { @Result(name = SUCCESS, location = "team_form.ftl") })
    public String insert() {
        final boolean canCreateTeam = SecurityRepository.canAccess(SecurityConstants.PERM_TEAMFORUM_CREATE);
        if (canCreateTeam) {
            return SUCCESS;
        } else {
            return PERMISSION;
        }
    }

    @Action(value = "edit", results = { @Result(name = SUCCESS, location = "team_form.ftl") })
    public String edit() {
        final boolean canEditTeam = SecurityRepository.canAccess(SecurityConstants.PERM_MODERATION_FORUMS, String.valueOf(teamId));
        final UserSession userSession = SessionFacade.getUserSession();
        if (canEditTeam || userSession.isAdmin() || userSession.isWebAdmin()) {
            this.team = ForumRepository.getForum(teamId);
            return SUCCESS;
        } else {
            return PERMISSION;
        }
    }

    @Action(value = "save", interceptorRefs = {
            @InterceptorRef("tokenSession"),
            @InterceptorRef(value = "fileUpload", params = { "allowedExtensions ", ".gif,.jpg,.png", "allowedTypes",
                    "image/png,image/gif,image/jpeg,image/pjpeg" }), @InterceptorRef("defaultStackIgearbook") }, results = { @Result(name = SUCCESS,
            location = "/team/list.action", type = "redirect") })
    public String save() {
        if (team.getId() == 0) {
            return createSave();
        } else {
            return editSave();
        }
    }

    private String editSave() {
        final UserSession userSession = SessionFacade.getUserSession();
        final boolean isModerator = userSession.isModerator(team.getId());
        if (!isModerator && !userSession.isAdmin() && !userSession.isWebAdmin()) {
            return PERMISSION;
        }

        final String logoUrl = this.doUpload();

        final ForumDAO forumDao = DataAccessDriver.getInstance().newForumDAO();
        final Forum teamUpdate = ForumRepository.getForum(team.getId());
        if (logoUrl != null) {
            teamUpdate.setLogo(logoUrl);
        }
        teamUpdate.setDescription(team.getDescription());
        if (SessionFacade.getUserSession().isAdmin()) {
            teamUpdate.setName(team.getName());
        }
        forumDao.update(teamUpdate);
        ForumRepository.reloadForum(team.getId());
        return SUCCESS;
    }

    private String createSave() {
        final boolean canCreateTeam = SecurityRepository.canAccess(SecurityConstants.PERM_TEAMFORUM_CREATE);
        if (!canCreateTeam) {
            return PERMISSION;
        }

        final String logoUrl = this.doUpload();

        final CategoryDAO categoryDao = DataAccessDriver.getInstance().newCategoryDAO();
        final GroupDAO groupDao = DataAccessDriver.getInstance().newGroupDAO();
        final UserDAO userDao = DataAccessDriver.getInstance().newUserDAO();
        final int userId = SessionFacade.getUserSession().getUserId();

        // Remove the create team entitlement if the team has been created.
        final Group teamCreateGorup = groupDao.selectByName(SecurityConstants.PERM_TEAMFORUM_CREATE);
        userDao.removeFromGroup(userId, new int[] { teamCreateGorup.getId() });

        final ForumDAO forumDao = DataAccessDriver.getInstance().newForumDAO();
        final List<Category> categories = categoryDao.selecByType(1);
        if (categories.size() == 1) {
            final Category category = categories.get(0);
            team.setType(1);
            team.setIdCategories(category.getId());
            team.setLogo(logoUrl);
            forumDao.addNew(team);
            ForumRepository.addForum(team);
        } else {
            throw new RuntimeException("Team category not found!");
        }

        final int teamId = team.getId();

        // Create user group
        createUserGroup(groupDao, teamId);
        // User requested to join will be put in this group
        createUserCandidateGroup(groupDao, teamId);
        // User ban to post
        createBanToPostUserGroup(groupDao, teamId);
        // Create moderation group
        createModerationGroup(groupDao, teamId);
        // Create owner group
        final Group ownerGroup = createOwnerGroup(groupDao, teamId);
        userDao.addToGroup(userId, new int[] { ownerGroup.getId() });

        // Create entitlement groups
        final GroupSecurityDAO gmodel = DataAccessDriver.getInstance().newGroupSecurityDAO();
        final PermissionControl pc = new PermissionControl();
        pc.setSecurityModel(gmodel);

        final Group defaultGroup = groupDao.selectById(SystemGlobals.getIntValue(ConfigKeys.DEFAULT_USER_GROUP));
        final int[] defaultGroupIds = { defaultGroup.getId() };
        // Access
        final Group accessGroup = groupDao.addNewEntitlementGroup(SecurityConstants.PERM_FORUM, teamId);
        this.addRole(pc, SecurityConstants.PERM_FORUM, teamId, accessGroup);
        groupDao.updateChildGroups(accessGroup.getId(), defaultGroupIds);
        final int anonymousUserId = SystemGlobals.getIntValue(ConfigKeys.ANONYMOUS_USER_ID);
        userDao.addToGroup(anonymousUserId, new int[] { accessGroup.getId() });

        // Anonymous posts
        final Group anonymousPostsGroup = groupDao.addNewEntitlementGroup(SecurityConstants.PERM_ANONYMOUS_POST, teamId);
        this.addRole(pc, SecurityConstants.PERM_ANONYMOUS_POST, teamId, anonymousPostsGroup);

        // Permit to reply
        final Group replyGroup = groupDao.addNewEntitlementGroup(SecurityConstants.PERM_REPLY, teamId);
        this.addRole(pc, SecurityConstants.PERM_REPLY, teamId, replyGroup);

        // Permit to new post
        final Group newPostGroup = groupDao.addNewEntitlementGroup(SecurityConstants.PERM_NEW_POST, teamId);
        this.addRole(pc, SecurityConstants.PERM_NEW_POST, teamId, newPostGroup);

        // HTML
        final Group htmlGroup = groupDao.addNewEntitlementGroup(SecurityConstants.PERM_HTML_DISABLED, teamId);
        this.addRole(pc, SecurityConstants.PERM_HTML_DISABLED, teamId, htmlGroup);

        SecurityRepository.clean();
        RolesRepository.clear();
        return SUCCESS;
    }

    private String doUpload() {
        if (upload == null) {
            return null;
        }
        final String savePath = SystemGlobals.getValue(ConfigKeys.ATTACHMENTS_STORE_DIR) + "/teamlogo/";

        final int suffixIndex = uploadFileName.lastIndexOf(".");
        final String suffix = uploadFileName.substring(suffixIndex, uploadFileName.length());
        final String timeFileName = DateUtil.getStringTime();
        final String tmpFileSavePath = savePath + timeFileName + "_tmp" + suffix;
        final String fileSaveName = timeFileName + suffix;
        final String fileSavePath = savePath + fileSaveName;
        saveImage(upload, tmpFileSavePath);// save tmp file
        makeImage(tmpFileSavePath, fileSavePath, fileSaveName);

        final File tmpFile = new File(tmpFileSavePath);
        if (tmpFile.exists()) {
            tmpFile.delete();
        }

        return ServletActionContext.getRequest().getContextPath() + "/upload/teamlogo/" + fileSaveName;
    }

    /**
     * 用于生成上传完后的图片的副本，如缩略图
     * 
     * @param url
     *            原图的绝对URL
     * @param WidthRange
     *            生成副本的宽度范围
     * @param HeightRange
     *            生成副本的高度范围
     * @param newUrl
     *            生成副本的地址
     * @param formatName
     *            生成图片的格式
     */
    public void makeImage(final String oldFileUrl, final String newUrl, final String newFileName) {
        // 读取图片
        BufferedImage bi = null;
        try {
            bi = ImageIO.read(new File(oldFileUrl));
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }

        // 判断读入图片的宽和高
        final int oldWidth = bi.getWidth();
        final int oldHeight = bi.getHeight();
        if (oldWidth <= WIDTH_RANGE && oldHeight <= HEIGHT_RANGE)
            saveImage(upload, newUrl);
        else {
            ImageHelper.zoomPicture(oldFileUrl, 80, 80, newFileName, true);
        }

    }

    /**
     * 把图片写入硬盘
     * 
     * @param file
     *            要保存的图片文件
     * @param savePath
     *            保存的路径
     * @throws Exception
     */
    private void saveImage(final File file, final String savePath) {
        FileOutputStream outputStream = null;
        FileInputStream fileIn = null;
        try {
            outputStream = new FileOutputStream(savePath);
            fileIn = new FileInputStream(file);
            final byte[] buffer = new byte[1024];
            int len;
            while ((len = fileIn.read(buffer)) > 0) {
                outputStream.write(buffer, 0, len);
            }
        } catch (final Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                fileIn.close();
                outputStream.close();
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

    private void addRole(final PermissionControl pc, final String roleName, final int forumId, final Group group) {
        final Role role = new Role();
        role.setName(roleName);

        final RoleValueCollection roleValues = new RoleValueCollection();

        final RoleValue rv = new RoleValue();
        rv.setValue(Integer.toString(forumId));
        roleValues.add(rv);

        pc.addRoleValue(group.getId(), role, roleValues);
    }

    private Group createUserGroup(final GroupDAO groupDao, final int teamId) {
        final GroupSecurityDAO gmodel = DataAccessDriver.getInstance().newGroupSecurityDAO();
        final PermissionControl pc = new PermissionControl();
        pc.setSecurityModel(gmodel);
        final Group userGroup = groupDao.addNewEntitlementGroup(SecurityConstants.PERM_TEAMFORUM_USER, teamId);
        this.addRole(pc, SecurityConstants.PERM_TEAMFORUM_USER, teamId, userGroup);
        this.addRole(pc, SecurityConstants.PERM_REPLY, teamId, userGroup);
        this.addRole(pc, SecurityConstants.PERM_NEW_POST, teamId, userGroup);
        this.addRole(pc, SecurityConstants.PERM_HTML_DISABLED, teamId, userGroup);
        return userGroup;
    }

    private Group createUserCandidateGroup(final GroupDAO groupDao, final int teamId) {
        final GroupSecurityDAO gmodel = DataAccessDriver.getInstance().newGroupSecurityDAO();
        final PermissionControl pc = new PermissionControl();
        pc.setSecurityModel(gmodel);
        final Group userCandidateGroup = groupDao.addNewEntitlementGroup(SecurityConstants.PERM_TEAMFORUM_USER_CANDIDATE, teamId);
        this.addRole(pc, SecurityConstants.PERM_TEAMFORUM_USER_CANDIDATE, teamId, userCandidateGroup);
        return userCandidateGroup;
    }

    private Group createBanToPostUserGroup(final GroupDAO groupDao, final int teamId) {
        final GroupSecurityDAO gmodel = DataAccessDriver.getInstance().newGroupSecurityDAO();
        final PermissionControl pc = new PermissionControl();
        pc.setSecurityModel(gmodel);
        final Group userBan2PostGroup = groupDao.addNewEntitlementGroup(SecurityConstants.PERM_TEAMFORUM_USER_BAN2POST, teamId);
        this.addRole(pc, SecurityConstants.PERM_TEAMFORUM_USER_BAN2POST, teamId, userBan2PostGroup);
        return userBan2PostGroup;
    }

    private Group createOwnerGroup(final GroupDAO groupDao, final int teamId) {
        final GroupSecurityDAO gmodel = DataAccessDriver.getInstance().newGroupSecurityDAO();
        final PermissionControl pc = new PermissionControl();
        pc.setSecurityModel(gmodel);
        final Group ownerGroup = groupDao.addNewEntitlementGroup(SecurityConstants.PERM_TEAMFORUM_OWNER, teamId);
        this.addRole(pc, SecurityConstants.PERM_TEAMFORUM_OWNER, teamId, ownerGroup);
        this.addRole(pc, SecurityConstants.PERM_CREATE_STICKY_ANNOUNCEMENT_TOPICS, teamId, ownerGroup);
        this.addRole(pc, SecurityConstants.PERM_MODERATION_FORUMS, teamId, ownerGroup);
        this.addRole(pc, SecurityConstants.PERM_MODERATION_APPROVE_MESSAGES, teamId, ownerGroup);
        this.addRole(pc, SecurityConstants.PERM_MODERATION_POST_REMOVE, teamId, ownerGroup);
        this.addRole(pc, SecurityConstants.PERM_MODERATION_POST_EDIT, teamId, ownerGroup);
        this.addRole(pc, SecurityConstants.PERM_MODERATION_TOPIC_LOCK_UNLOCK, teamId, ownerGroup);
        this.addRole(pc, SecurityConstants.PERM_REPLY, teamId, ownerGroup);
        this.addRole(pc, SecurityConstants.PERM_NEW_POST, teamId, ownerGroup);
        this.addRole(pc, SecurityConstants.PERM_HTML_DISABLED, teamId, ownerGroup);
        return ownerGroup;
    }

    private Group createModerationGroup(final GroupDAO groupDao, final int forumId) {
        final GroupSecurityDAO gmodel = DataAccessDriver.getInstance().newGroupSecurityDAO();
        final PermissionControl pc = new PermissionControl();
        pc.setSecurityModel(gmodel);
        final Group moderationGroup = groupDao.addNewEntitlementGroup(SecurityConstants.PERM_MODERATION_FORUMS, forumId);
        this.addRole(pc, SecurityConstants.PERM_CREATE_STICKY_ANNOUNCEMENT_TOPICS, forumId, moderationGroup);
        this.addRole(pc, SecurityConstants.PERM_MODERATION_FORUMS, forumId, moderationGroup);
        this.addRole(pc, SecurityConstants.PERM_MODERATION_APPROVE_MESSAGES, forumId, moderationGroup);
        this.addRole(pc, SecurityConstants.PERM_MODERATION_POST_REMOVE, forumId, moderationGroup);
        this.addRole(pc, SecurityConstants.PERM_MODERATION_POST_EDIT, forumId, moderationGroup);
        this.addRole(pc, SecurityConstants.PERM_MODERATION_TOPIC_LOCK_UNLOCK, forumId, moderationGroup);
        this.addRole(pc, SecurityConstants.PERM_REPLY, forumId, moderationGroup);
        this.addRole(pc, SecurityConstants.PERM_NEW_POST, forumId, moderationGroup);
        this.addRole(pc, SecurityConstants.PERM_HTML_DISABLED, forumId, moderationGroup);
        return moderationGroup;
    }

    public Forum getTeam() {
        return team;
    }

    public void setTeam(final Forum team) {
        this.team = team;
    }

    public int getTeamId() {
        return teamId;
    }

    public void setTeamId(final int teamId) {
        this.teamId = teamId;
    }

    public String getUploadContentType() {
        return uploadContentType;
    }

    public void setUploadContentType(final String uploadContentType) {
        this.uploadContentType = uploadContentType;
    }

    public void setUpload(final File upload) {
        this.upload = upload;
    }

    public File getUpload() {
        return upload;
    }

    public String getUploadFileName() {
        return uploadFileName;
    }

    public void setUploadFileName(final String uploadFileName) {
        this.uploadFileName = uploadFileName;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(final int userId) {
        this.userId = userId;
    }

    public int[] getBanPostUserIds() {
        return banPostUserIds;
    }

    public void setBanPostUserIds(final int[] banPostUserIds) {
        this.banPostUserIds = banPostUserIds;
    }

}
