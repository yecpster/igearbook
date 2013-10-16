package com.igearbook.action;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.imageio.ImageIO;

import net.jforum.SessionFacade;
import net.jforum.dao.CategoryDAO;
import net.jforum.dao.DataAccessDriver;
import net.jforum.dao.ForumDAO;
import net.jforum.dao.GroupDAO;
import net.jforum.dao.GroupSecurityDAO;
import net.jforum.dao.UserDAO;
import net.jforum.entities.Category;
import net.jforum.entities.Forum;
import net.jforum.entities.Group;
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
import net.jforum.util.preferences.ConfigKeys;
import net.jforum.util.preferences.SystemGlobals;
import net.jforum.view.forum.ModerationHelper;
import net.jforum.view.forum.common.ForumCommon;
import net.jforum.view.forum.common.TopicsCommon;
import net.jforum.view.forum.common.ViewCommon;

import org.apache.struts2.ServletActionContext;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;

import com.google.common.collect.Lists;
import com.igearbook.util.DateUtil;
import com.igearbook.util.ImageHelper;
import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionSupport;

@Namespace("/team")
public class TeamAction extends ActionSupport {
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

    @Action(value = "list", results = { @Result(name = SUCCESS, location = "team_list.ftl") })
    public String list() {
        ActionContext context = ServletActionContext.getContext();
        boolean canCreateTeam = SecurityRepository.canAccess(SecurityConstants.PERM_TEAMFORUM_CREATE);
        context.put("canCreateTeam", canCreateTeam);

        List<Category> allCategories = ForumRepository.getAllCategories(SessionFacade.getUserSession().getUserId());
        List<Category> categories = Lists.newArrayList();
        for (Category category : allCategories) {
            if (category.getType() == 1) {
                categories.add(category);
            }
        }
        context.put("categories", categories);

        List<Forum> rankTeams = Lists.newArrayList();
        for (Category category : categories) {
            rankTeams.addAll(category.getForums());
        }
        Collections.sort(rankTeams, new Comparator<Forum>() {
            @Override
            public int compare(Forum o1, Forum o2) {
                return o2.getTotalPosts() - o1.getTotalPosts();
            }
        });
        context.put("rankTeams", rankTeams);

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

    @Action(value = "moderation", results = { @Result(name = SUCCESS, location = "team_show.ftl") })
    public String moderation() {
        ActionContext context = ServletActionContext.getContext();
        context.put("openModeration", true);
        return this.show();
    }

    @Action(value = "join", results = { @Result(name = SUCCESS, location = "show", type = "chain") })
    public String joinTeam() {
        boolean isTeamMember = SecurityRepository.canAccess(SecurityConstants.PERM_TEAMFORUM_USER, String.valueOf(teamId))
                || SecurityRepository.canAccess(SecurityConstants.PERM_TEAMFORUM_OWNER, String.valueOf(teamId))
                || SecurityRepository.canAccess(SecurityConstants.PERM_TEAMFORUM_USER_CANDIDATE, String.valueOf(teamId))
                || SecurityRepository.canAccess(SecurityConstants.PERM_MODERATION_FORUMS, String.valueOf(teamId));
        boolean isBanUser = SecurityRepository.canAccess(SecurityConstants.PERM_TEAMFORUM_USER_BAN2POST, String.valueOf(teamId));
        if (!isTeamMember && !isBanUser) {
            int userId = SessionFacade.getUserSession().getUserId();
            GroupDAO groupDao = DataAccessDriver.getInstance().newGroupDAO();
            UserDAO userDao = DataAccessDriver.getInstance().newUserDAO();
            Group teamUserGorup = groupDao.getEntitlementGroup(SecurityConstants.PERM_TEAMFORUM_USER, teamId);
            if (teamUserGorup == null) {
                teamUserGorup = this.createUserGroup(groupDao, teamId);
            }
            userDao.addToGroup(userId, new int[] { teamUserGorup.getId() });
            SecurityRepository.clean();
            RolesRepository.clear();
        }

        return SUCCESS;
    }

    @Action(value = "manageUser", results = { @Result(name = SUCCESS, location = "team_manageUser.ftl") })
    public String manageUser() {
        boolean isTeamOwner = SecurityRepository.canAccess(SecurityConstants.PERM_TEAMFORUM_OWNER, String.valueOf(teamId));
        if (!isTeamOwner) {
            return ERROR;
        }

        ActionContext context = ServletActionContext.getContext();
        GroupDAO groupDao = DataAccessDriver.getInstance().newGroupDAO();
        UserDAO userDao = DataAccessDriver.getInstance().newUserDAO();

        int start = ViewCommon.getStartPage();
        int usersPerPage = SystemGlobals.getIntValue(ConfigKeys.USERS_PER_PAGE);
        Group teamUserGorup = groupDao.getEntitlementGroup(SecurityConstants.PERM_TEAMFORUM_USER, teamId);
        contextToPagination(start, userDao.getTotalUsersByGroup(teamUserGorup.getId()), usersPerPage);
        List<User> users = userDao.selectAllByGroup(teamUserGorup.getId(), start, Integer.MAX_VALUE);

        Group banUserGorup = groupDao.getEntitlementGroup(SecurityConstants.PERM_TEAMFORUM_USER_BAN2POST, teamId);
        if (banUserGorup == null) {
            banUserGorup = this.createBanToPostUserGroup(groupDao, teamId);
        }
        List<User> banUsers = userDao.selectAllByGroup(banUserGorup.getId(), 0, 100);

        Group moderatorsGorup = groupDao.getEntitlementGroup(SecurityConstants.PERM_MODERATION_FORUMS, teamId);
        List<User> moderators = userDao.selectAllByGroup(moderatorsGorup.getId(), 0, 10);

        Group ownerGorup = groupDao.getEntitlementGroup(SecurityConstants.PERM_TEAMFORUM_OWNER, teamId);
        List<User> owners = userDao.selectAllByGroup(ownerGorup.getId(), 0, 5);

        context.put("users", users);
        context.put("banUsers", banUsers);
        context.put("moderators", moderators);
        context.put("owner", owners.get(0));
        return SUCCESS;
    }

    @Action(value = "setAdmin", results = { @Result(name = SUCCESS, location = "manageUser.action", type = "redirect", params = { "teamId",
            "${teamId}" }) })
    public String setAdmin() {
        boolean isTeamOwner = SecurityRepository.canAccess(SecurityConstants.PERM_TEAMFORUM_OWNER, String.valueOf(teamId));
        boolean isUserValid = SecurityRepository.canAccess(this.userId, SecurityConstants.PERM_TEAMFORUM_USER, String.valueOf(teamId));
        if (!isTeamOwner || !isUserValid) {
            return ERROR;
        }

        GroupDAO groupDao = DataAccessDriver.getInstance().newGroupDAO();
        UserDAO userDao = DataAccessDriver.getInstance().newUserDAO();

        Group teamUserGorup = groupDao.getEntitlementGroup(SecurityConstants.PERM_TEAMFORUM_USER, teamId);
        Group moderatorsGorup = groupDao.getEntitlementGroup(SecurityConstants.PERM_MODERATION_FORUMS, teamId);
        userDao.removeFromGroup(this.userId, new int[] { teamUserGorup.getId() });
        userDao.addToGroup(this.userId, new int[] { moderatorsGorup.getId() });

        SecurityRepository.clean();
        RolesRepository.clear();
        return SUCCESS;
    }

    @Action(value = "cancelAdmin", results = { @Result(name = SUCCESS, location = "manageUser.action", type = "redirect", params = { "teamId",
            "${teamId}" }) })
    public String cancelAdmin() {
        boolean isTeamOwner = SecurityRepository.canAccess(SecurityConstants.PERM_TEAMFORUM_OWNER, String.valueOf(teamId));
        boolean isUserValid = SecurityRepository.canAccess(this.userId, SecurityConstants.PERM_MODERATION_FORUMS, String.valueOf(teamId));
        if (!isTeamOwner || !isUserValid) {
            return ERROR;
        }

        GroupDAO groupDao = DataAccessDriver.getInstance().newGroupDAO();
        UserDAO userDao = DataAccessDriver.getInstance().newUserDAO();

        Group moderatorsGorup = groupDao.getEntitlementGroup(SecurityConstants.PERM_MODERATION_FORUMS, teamId);
        Group teamUserGorup = groupDao.getEntitlementGroup(SecurityConstants.PERM_TEAMFORUM_USER, teamId);
        userDao.removeFromGroup(this.userId, new int[] { moderatorsGorup.getId() });
        userDao.addToGroup(this.userId, new int[] { teamUserGorup.getId() });

        SecurityRepository.clean();
        RolesRepository.clear();
        return SUCCESS;
    }

    @Action(value = "banPostUsers", results = { @Result(name = SUCCESS, location = "manageUser.action", type = "redirect", params = { "teamId",
            "${teamId}" }) })
    public String banPostUsers() {
        boolean isTeamOwner = SecurityRepository.canAccess(SecurityConstants.PERM_TEAMFORUM_OWNER, String.valueOf(teamId));
        if (!isTeamOwner) {
            return ERROR;
        }
        if (banPostUserIds == null || banPostUserIds.length == 0) {
            return SUCCESS;
        }
        for (int banUserId : banPostUserIds) {
            boolean isUserValid = SecurityRepository.canAccess(banUserId, SecurityConstants.PERM_TEAMFORUM_USER, String.valueOf(teamId));
            if (!isUserValid) {
                return ERROR;
            }
        }
        GroupDAO groupDao = DataAccessDriver.getInstance().newGroupDAO();
        UserDAO userDao = DataAccessDriver.getInstance().newUserDAO();

        Group teamUserGorup = groupDao.getEntitlementGroup(SecurityConstants.PERM_TEAMFORUM_USER, teamId);
        Group teamUserBanToPostGorup = groupDao.getEntitlementGroup(SecurityConstants.PERM_TEAMFORUM_USER_BAN2POST, teamId);
        for (int banUserId : banPostUserIds) {
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
        boolean isTeamOwner = SecurityRepository.canAccess(SecurityConstants.PERM_TEAMFORUM_OWNER, String.valueOf(teamId));
        if (!isTeamOwner) {
            return ERROR;
        }
        boolean isUserValid = SecurityRepository.canAccess(userId, SecurityConstants.PERM_TEAMFORUM_USER_BAN2POST, String.valueOf(teamId));
        if (!isUserValid) {
            return ERROR;
        }
        GroupDAO groupDao = DataAccessDriver.getInstance().newGroupDAO();
        UserDAO userDao = DataAccessDriver.getInstance().newUserDAO();

        Group teamUserGorup = groupDao.getEntitlementGroup(SecurityConstants.PERM_TEAMFORUM_USER, teamId);
        Group teamUserBanToPostGorup = groupDao.getEntitlementGroup(SecurityConstants.PERM_TEAMFORUM_USER_BAN2POST, teamId);
        userDao.removeFromGroup(userId, new int[] { teamUserBanToPostGorup.getId() });
        userDao.addToGroup(userId, new int[] { teamUserGorup.getId() });

        SecurityRepository.clean();
        RolesRepository.clear();

        return SUCCESS;
    }

    @Action(value = "show", results = { @Result(name = SUCCESS, location = "team_show.ftl") })
    public String show() {
        ActionContext context = ServletActionContext.getContext();
        ForumDAO fm = DataAccessDriver.getInstance().newForumDAO();

        // The user can access this team?
        Forum team = ForumRepository.getForum(teamId);

        if (team == null || team.getType() != 1 || !ForumRepository.isCategoryAccessible(team.getCategoryId())) {
            new ModerationHelper().denied(I18n.getMessage("ForumListing.denied"));
            return ERROR;
        }

        int start = ViewCommon.getStartPage();

        List<Topic> tmpTopics = TopicsCommon.topicsByForum(teamId, start);

        // Moderation
        UserSession userSession = SessionFacade.getUserSession();
        boolean isLogged = SessionFacade.isLogged();
        boolean isModerator = userSession.isModerator(teamId);
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

        boolean isTeamMember = SecurityRepository.canAccess(SecurityConstants.PERM_TEAMFORUM_USER, String.valueOf(teamId))
                || SecurityRepository.canAccess(SecurityConstants.PERM_TEAMFORUM_OWNER, String.valueOf(teamId))
                || SecurityRepository.canAccess(SecurityConstants.PERM_TEAMFORUM_USER_CANDIDATE, String.valueOf(teamId))
                || SecurityRepository.canAccess(SecurityConstants.PERM_MODERATION_FORUMS, String.valueOf(teamId));
        boolean isBanUser = SecurityRepository.canAccess(SecurityConstants.PERM_TEAMFORUM_USER_BAN2POST, String.valueOf(teamId));
        boolean isTeamOwner = SecurityRepository.canAccess(SecurityConstants.PERM_TEAMFORUM_OWNER, String.valueOf(teamId));

        context.put("isTeamMember", isTeamMember);
        context.put("isBanUser", isBanUser);
        context.put("isTeamOwner", isTeamOwner);

        // Pagination
        int topicsPerPage = SystemGlobals.getIntValue(ConfigKeys.TOPICS_PER_PAGE);
        int postsPerPage = SystemGlobals.getIntValue(ConfigKeys.POSTS_PER_PAGE);
        int totalTopics = team.getTotalTopics();

        contextToPagination(start, totalTopics, topicsPerPage);
        context.put("postsPerPage", new Integer(postsPerPage));

        topicListingBase();

        return SUCCESS;
    }

    public static void contextToPagination(int start, int totalRecords, int recordsPerPage) {
        ActionContext context = ServletActionContext.getContext();

        context.put("totalPages", new Double(Math.ceil((double) totalRecords / (double) recordsPerPage)));
        context.put("recordsPerPage", new Integer(recordsPerPage));
        context.put("totalRecords", new Integer(totalRecords));
        context.put("thisPage", new Double(Math.ceil((double) (start + 1) / (double) recordsPerPage)));
        context.put("start", new Integer(start));
    }

    public static void topicListingBase() {
        ActionContext context = ServletActionContext.getContext();

        // Topic Types
        context.put("TOPIC_ANNOUNCE", new Integer(Topic.TYPE_ANNOUNCE));
        context.put("TOPIC_STICKY", new Integer(Topic.TYPE_STICKY));
        context.put("TOPIC_NORMAL", new Integer(Topic.TYPE_NORMAL));

        // Topic Status
        context.put("STATUS_LOCKED", new Integer(Topic.STATUS_LOCKED));
        context.put("STATUS_UNLOCKED", new Integer(Topic.STATUS_UNLOCKED));

        // Moderation
        PermissionControl pc = SecurityRepository.get(SessionFacade.getUserSession().getUserId());

        context.put("can_remove_posts", pc.canAccess(SecurityConstants.PERM_MODERATION_POST_REMOVE));
        context.put("can_move_topics", pc.canAccess(SecurityConstants.PERM_MODERATION_TOPIC_MOVE));
        context.put("can_lockUnlock_topics", pc.canAccess(SecurityConstants.PERM_MODERATION_TOPIC_LOCK_UNLOCK));
        context.put("rssEnabled", SystemGlobals.getBoolValue(ConfigKeys.RSS_ENABLED));
    }

    @Action(value = "insert", results = { @Result(name = SUCCESS, location = "team_form.ftl") })
    public String insert() {
        boolean canCreateTeam = SecurityRepository.canAccess(SecurityConstants.PERM_TEAMFORUM_CREATE);
        if (canCreateTeam) {
            return SUCCESS;
        } else {
            return ERROR;
        }
    }

    @Action(value = "edit", results = { @Result(name = SUCCESS, location = "team_form.ftl") })
    public String edit() {
        boolean canEditTeam = SecurityRepository.canAccess(SecurityConstants.PERM_MODERATION_FORUMS, String.valueOf(teamId));
        if (canEditTeam || SessionFacade.getUserSession().isAdmin()) {
            this.team = ForumRepository.getForum(teamId);
            return SUCCESS;
        } else {
            return ERROR;
        }
    }

    @Action(value = "save", interceptorRefs = {
            @InterceptorRef("tokenSession"),
            @InterceptorRef(value = "fileUpload", params = { "allowedExtensions ", ".gif,.jpg,.png", "allowedTypes",
                    "image/png,image/gif,image/jpeg,image/pjpeg" }), @InterceptorRef("defaultStackIgearbook") }, results = { @Result(name = SUCCESS, location = "/team/list.action", type = "redirect") })
    public String save() {
        if (team.getId() == 0) {
            return createSave();
        } else {
            return editSave();
        }
    }

    private String editSave() {
        boolean isModerator = SessionFacade.getUserSession().isModerator(team.getId());
        if (!isModerator && !SessionFacade.getUserSession().isAdmin()) {
            return ERROR;
        }

        String logoUrl = this.doUpload();

        ForumDAO forumDao = DataAccessDriver.getInstance().newForumDAO();
        Forum teamUpdate = ForumRepository.getForum(team.getId());
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
        boolean canCreateTeam = SecurityRepository.canAccess(SecurityConstants.PERM_TEAMFORUM_CREATE);
        if (!canCreateTeam) {
            return ERROR;
        }

        String logoUrl = this.doUpload();

        CategoryDAO categoryDao = DataAccessDriver.getInstance().newCategoryDAO();
        GroupDAO groupDao = DataAccessDriver.getInstance().newGroupDAO();
        UserDAO userDao = DataAccessDriver.getInstance().newUserDAO();
        int userId = SessionFacade.getUserSession().getUserId();

        // Remove the create team entitlement if the team has been created.
        Group teamCreateGorup = groupDao.selectByName(SecurityConstants.PERM_TEAMFORUM_CREATE);
        userDao.removeFromGroup(userId, new int[] { teamCreateGorup.getId() });

        ForumDAO forumDao = DataAccessDriver.getInstance().newForumDAO();
        List<Category> categories = categoryDao.selecByType(1);
        if (categories.size() == 1) {
            Category category = categories.get(0);
            team.setType(1);
            team.setIdCategories(category.getId());
            team.setLogo(logoUrl);
            forumDao.addNew(team);
            ForumRepository.addForum(team);
        } else {
            throw new RuntimeException("Team category not found!");
        }

        int teamId = team.getId();

        // Create user group
        createUserGroup(groupDao, teamId);
        // User requested to join will be put in this group
        createUserCandidateGroup(groupDao, teamId);
        // User ban to post
        createBanToPostUserGroup(groupDao, teamId);
        // Create moderation group
        createModerationGroup(groupDao, teamId);
        // Create owner group
        Group ownerGroup = createOwnerGroup(groupDao, teamId);
        userDao.addToGroup(userId, new int[] { ownerGroup.getId() });

        // Create entitlement groups
        GroupSecurityDAO gmodel = DataAccessDriver.getInstance().newGroupSecurityDAO();
        PermissionControl pc = new PermissionControl();
        pc.setSecurityModel(gmodel);

        Group defaultGroup = groupDao.selectById(SystemGlobals.getIntValue(ConfigKeys.DEFAULT_USER_GROUP));
        int[] defaultGroupIds = { defaultGroup.getId() };
        // Access
        Group accessGroup = groupDao.addNewEntitlementGroup(SecurityConstants.PERM_FORUM, teamId);
        this.addRole(pc, SecurityConstants.PERM_FORUM, teamId, accessGroup);
        groupDao.updateChildGroups(accessGroup.getId(), defaultGroupIds);
        int anonymousUserId = SystemGlobals.getIntValue(ConfigKeys.ANONYMOUS_USER_ID);
        userDao.addToGroup(anonymousUserId, new int[] { accessGroup.getId() });

        // Anonymous posts
        Group anonymousPostsGroup = groupDao.addNewEntitlementGroup(SecurityConstants.PERM_ANONYMOUS_POST, teamId);
        this.addRole(pc, SecurityConstants.PERM_ANONYMOUS_POST, teamId, anonymousPostsGroup);

        // Permit to reply
        Group replyGroup = groupDao.addNewEntitlementGroup(SecurityConstants.PERM_REPLY, teamId);
        this.addRole(pc, SecurityConstants.PERM_REPLY, teamId, replyGroup);

        // Permit to new post
        Group newPostGroup = groupDao.addNewEntitlementGroup(SecurityConstants.PERM_NEW_POST, teamId);
        this.addRole(pc, SecurityConstants.PERM_NEW_POST, teamId, newPostGroup);

        // HTML
        Group htmlGroup = groupDao.addNewEntitlementGroup(SecurityConstants.PERM_HTML_DISABLED, teamId);
        this.addRole(pc, SecurityConstants.PERM_HTML_DISABLED, teamId, htmlGroup);

        SecurityRepository.clean();
        RolesRepository.clear();
        return SUCCESS;
    }

    private String doUpload() {
        if (upload == null) {
            return null;
        }
        String savePath = SystemGlobals.getValue(ConfigKeys.ATTACHMENTS_STORE_DIR) + "/teamlogo/";

        int suffixIndex = uploadFileName.lastIndexOf(".");
        String suffix = uploadFileName.substring(suffixIndex, uploadFileName.length());
        String timeFileName = DateUtil.getStringTime();
        String tmpFileSavePath = savePath + timeFileName + "_tmp" + suffix;
        String fileSaveName = timeFileName + suffix;
        String fileSavePath = savePath + fileSaveName;
        saveImage(upload, tmpFileSavePath);// save tmp file
        makeImage(tmpFileSavePath, fileSavePath, fileSaveName);

        File tmpFile = new File(tmpFileSavePath);
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
    public void makeImage(String oldFileUrl, String newUrl, String newFileName) {
        // 读取图片
        BufferedImage bi = null;
        try {
            bi = ImageIO.read(new File(oldFileUrl));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // 判断读入图片的宽和高
        int oldWidth = bi.getWidth();
        int oldHeight = bi.getHeight();
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
    private void saveImage(File file, String savePath) {
        FileOutputStream outputStream = null;
        FileInputStream fileIn = null;
        try {
            outputStream = new FileOutputStream(savePath);
            fileIn = new FileInputStream(file);
            byte[] buffer = new byte[1024];
            int len;
            while ((len = fileIn.read(buffer)) > 0) {
                outputStream.write(buffer, 0, len);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                fileIn.close();
                outputStream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

    private void addRole(PermissionControl pc, String roleName, int forumId, Group group) {
        Role role = new Role();
        role.setName(roleName);

        RoleValueCollection roleValues = new RoleValueCollection();

        RoleValue rv = new RoleValue();
        rv.setValue(Integer.toString(forumId));
        roleValues.add(rv);

        pc.addRoleValue(group.getId(), role, roleValues);
    }

    private Group createUserGroup(GroupDAO groupDao, int teamId) {
        GroupSecurityDAO gmodel = DataAccessDriver.getInstance().newGroupSecurityDAO();
        PermissionControl pc = new PermissionControl();
        pc.setSecurityModel(gmodel);
        Group userGroup = groupDao.addNewEntitlementGroup(SecurityConstants.PERM_TEAMFORUM_USER, teamId);
        this.addRole(pc, SecurityConstants.PERM_TEAMFORUM_USER, teamId, userGroup);
        this.addRole(pc, SecurityConstants.PERM_REPLY, teamId, userGroup);
        this.addRole(pc, SecurityConstants.PERM_NEW_POST, teamId, userGroup);
        this.addRole(pc, SecurityConstants.PERM_HTML_DISABLED, teamId, userGroup);
        return userGroup;
    }

    private Group createUserCandidateGroup(GroupDAO groupDao, int teamId) {
        GroupSecurityDAO gmodel = DataAccessDriver.getInstance().newGroupSecurityDAO();
        PermissionControl pc = new PermissionControl();
        pc.setSecurityModel(gmodel);
        Group userCandidateGroup = groupDao.addNewEntitlementGroup(SecurityConstants.PERM_TEAMFORUM_USER_CANDIDATE, teamId);
        this.addRole(pc, SecurityConstants.PERM_TEAMFORUM_USER_CANDIDATE, teamId, userCandidateGroup);
        return userCandidateGroup;
    }

    private Group createBanToPostUserGroup(GroupDAO groupDao, int teamId) {
        GroupSecurityDAO gmodel = DataAccessDriver.getInstance().newGroupSecurityDAO();
        PermissionControl pc = new PermissionControl();
        pc.setSecurityModel(gmodel);
        Group userBan2PostGroup = groupDao.addNewEntitlementGroup(SecurityConstants.PERM_TEAMFORUM_USER_BAN2POST, teamId);
        this.addRole(pc, SecurityConstants.PERM_TEAMFORUM_USER_BAN2POST, teamId, userBan2PostGroup);
        return userBan2PostGroup;
    }

    private Group createOwnerGroup(GroupDAO groupDao, int teamId) {
        GroupSecurityDAO gmodel = DataAccessDriver.getInstance().newGroupSecurityDAO();
        PermissionControl pc = new PermissionControl();
        pc.setSecurityModel(gmodel);
        Group ownerGroup = groupDao.addNewEntitlementGroup(SecurityConstants.PERM_TEAMFORUM_OWNER, teamId);
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

    private Group createModerationGroup(GroupDAO groupDao, int forumId) {
        GroupSecurityDAO gmodel = DataAccessDriver.getInstance().newGroupSecurityDAO();
        PermissionControl pc = new PermissionControl();
        pc.setSecurityModel(gmodel);
        Group moderationGroup = groupDao.addNewEntitlementGroup(SecurityConstants.PERM_MODERATION_FORUMS, forumId);
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

    public void setTeam(Forum team) {
        this.team = team;
    }

    public int getTeamId() {
        return teamId;
    }

    public void setTeamId(int teamId) {
        this.teamId = teamId;
    }

    public String getUploadContentType() {
        return uploadContentType;
    }

    public void setUploadContentType(String uploadContentType) {
        this.uploadContentType = uploadContentType;
    }

    public void setUpload(File upload) {
        this.upload = upload;
    }

    public File getUpload() {
        return upload;
    }

    public String getUploadFileName() {
        return uploadFileName;
    }

    public void setUploadFileName(String uploadFileName) {
        this.uploadFileName = uploadFileName;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int[] getBanPostUserIds() {
        return banPostUserIds;
    }

    public void setBanPostUserIds(int[] banPostUserIds) {
        this.banPostUserIds = banPostUserIds;
    }

}
