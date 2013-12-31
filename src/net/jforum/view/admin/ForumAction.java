/*
 * Copyright (c) JForum Team
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, 
 * with or without modification, are permitted provided 
 * that the following conditions are met:
 * 
 * 1) Redistributions of source code must retain the above 
 * copyright notice, this list of conditions and the 
 * following  disclaimer.
 * 2)  Redistributions in binary form must reproduce the 
 * above copyright notice, this list of conditions and 
 * the following disclaimer in the documentation and/or 
 * other materials provided with the distribution.
 * 3) Neither the name of "Rafael Steil" nor 
 * the names of its contributors may be used to endorse 
 * or promote products derived from this software without 
 * specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT 
 * HOLDERS AND CONTRIBUTORS "AS IS" AND ANY 
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, 
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF 
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR 
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL 
 * THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE 
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, 
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER 
 * IN CONTRACT, STRICT LIABILITY, OR TORT 
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN 
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF 
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE
 * 
 * This file creation date: Mar 28, 2003 / 8:21:56 PM
 * The JForum Project
 * http://www.jforum.net
 */
package net.jforum.view.admin;

import java.util.List;

import net.jforum.dao.CategoryDAO;
import net.jforum.dao.DataAccessDriver;
import net.jforum.dao.ForumDAO;
import net.jforum.dao.GroupDAO;
import net.jforum.dao.GroupSecurityDAO;
import net.jforum.dao.TopicDAO;
import net.jforum.dao.UserDAO;
import net.jforum.entities.Category;
import net.jforum.entities.Forum;
import net.jforum.entities.Group;
import net.jforum.entities.User;
import net.jforum.repository.ForumRepository;
import net.jforum.repository.RolesRepository;
import net.jforum.repository.SecurityRepository;
import net.jforum.security.PermissionControl;
import net.jforum.security.Role;
import net.jforum.security.RoleValue;
import net.jforum.security.RoleValueCollection;
import net.jforum.security.SecurityConstants;
import net.jforum.util.preferences.ConfigKeys;
import net.jforum.util.preferences.SystemGlobals;
import net.jforum.util.preferences.TemplateKeys;
import net.jforum.view.admin.common.ModerationCommon;

import com.google.common.collect.Lists;

/**
 * @author Rafael Steil
 * @version $Id: ForumAction.java,v 1.34 2007/08/25 00:11:29 rafaelsteil Exp $
 */
public class ForumAction extends AdminCommand {
    // Listing
    @Override
    public void list() {
        this.context.put("categories", DataAccessDriver.getInstance().newCategoryDAO().selectAll());
        this.context.put("repository", new ForumRepository());
        this.setTemplateName(TemplateKeys.FORUM_ADMIN_LIST);
    }

    // One more, one more
    public void insert() {
        final CategoryDAO cm = DataAccessDriver.getInstance().newCategoryDAO();
        final GroupDAO groupDao = DataAccessDriver.getInstance().newGroupDAO();
        final List<Group> selectedGroups = Lists.newArrayList();
        final List<Group> candidateGrups = groupDao.getCandidateGroups(selectedGroups);

        this.context.put("accessCandidateGroups", candidateGrups);
        this.context.put("replyCandidateGroups", candidateGrups);
        this.context.put("postCandidateGroups", candidateGrups);
        this.context.put("htmlCandidateGroups", candidateGrups);
        this.context.put("accessSelectedGroups", selectedGroups);
        this.context.put("replySelectedGroups", selectedGroups);
        this.context.put("postSelectedGroups", selectedGroups);
        this.context.put("htmlSelectedGroups", selectedGroups);
        this.context.put("categories", cm.selectAll());
        this.context.put("action", "insertSave");
        this.setTemplateName(TemplateKeys.FORUM_ADMIN_INSERT);
    }

    // A new one
    public void insertSave() {
        final Forum f = new Forum();
        f.setDescription(this.request.getParameter("description"));
        f.setCategoryId(this.request.getIntParameter("categories_id"));
        f.setName(this.request.getParameter("forum_name"));
        f.setLogo(this.request.getParameter("forum_Logo"));
        f.setModerated("1".equals(this.request.getParameter("moderate")));

        final int forumId = DataAccessDriver.getInstance().newForumDAO().addNew(f);
        f.setId(forumId);

        ForumRepository.addForum(f);

        final GroupSecurityDAO gmodel = DataAccessDriver.getInstance().newGroupSecurityDAO();
        final PermissionControl pc = new PermissionControl();
        pc.setSecurityModel(gmodel);

        // Access
        final GroupDAO groupDao = DataAccessDriver.getInstance().newGroupDAO();
        final Group accessGroup = groupDao.addNewEntitlementGroup(SecurityConstants.PERM_FORUM, forumId);
        this.addRole(pc, SecurityConstants.PERM_FORUM, forumId, accessGroup);
        final String[] groupsAccess = request.getParameterValues("groupsAccess");
        updateChildGroups(accessGroup, groupsAccess, groupDao);

        // Anonymous posts
        final Group anonymousPostsGroup = groupDao.addNewEntitlementGroup(SecurityConstants.PERM_ANONYMOUS_POST, forumId);
        this.addRole(pc, SecurityConstants.PERM_ANONYMOUS_POST, forumId, anonymousPostsGroup);

        final boolean permitAnonymousPosts = "1".equals(this.request.getParameter("permitAnonymousPosts"));
        if (permitAnonymousPosts) {
            final UserDAO um = DataAccessDriver.getInstance().newUserDAO();
            final int anonymousUid = Integer.parseInt(SystemGlobals.getValue(ConfigKeys.ANONYMOUS_USER_ID));
            um.addToGroup(anonymousUid, new int[] { anonymousPostsGroup.getId() });
        }

        // Permit to replay
        final Group replyGroup = groupDao.addNewEntitlementGroup(SecurityConstants.PERM_REPLY, forumId);
        this.addRole(pc, SecurityConstants.PERM_REPLY, forumId, replyGroup);
        final String[] groupsReply = request.getParameterValues("groupsReply");
        updateChildGroups(replyGroup, groupsReply, groupDao);

        // Permit to new post
        final Group newPostGroup = groupDao.addNewEntitlementGroup(SecurityConstants.PERM_NEW_POST, forumId);
        this.addRole(pc, SecurityConstants.PERM_NEW_POST, forumId, newPostGroup);
        final String[] groupsPost = request.getParameterValues("groupsPost");
        updateChildGroups(newPostGroup, groupsPost, groupDao);

        // HTML
        final Group htmlGroup = groupDao.addNewEntitlementGroup(SecurityConstants.PERM_HTML_DISABLED, forumId);
        this.addRole(pc, SecurityConstants.PERM_HTML_DISABLED, forumId, htmlGroup);
        final String[] groupsHtml = request.getParameterValues("groupsHtml");
        updateChildGroups(htmlGroup, groupsHtml, groupDao);

        SecurityRepository.clean();
        RolesRepository.clear();

        // this.handleMailIntegration();

        this.list();
    }

    // Edit
    public void edit() {
        final int forumId = this.request.getIntParameter("forum_id");
        final ForumDAO forumDao = DataAccessDriver.getInstance().newForumDAO();

        final CategoryDAO cm = DataAccessDriver.getInstance().newCategoryDAO();

        final GroupDAO groupDao = DataAccessDriver.getInstance().newGroupDAO();
        final Group accessGroup = groupDao.getEntitlementGroup(SecurityConstants.PERM_FORUM, forumId);
        final List<Group> accessSelectedGroups = groupDao.getChildGroups(accessGroup.getId());
        this.context.put("accessSelectedGroups", accessSelectedGroups);
        this.context.put("accessCandidateGroups", groupDao.getCandidateGroups(accessSelectedGroups));

        final int anonymousUid = Integer.parseInt(SystemGlobals.getValue(ConfigKeys.ANONYMOUS_USER_ID));
        final boolean permitAnonymousPosts = SecurityRepository.canAccess(anonymousUid, SecurityConstants.PERM_ANONYMOUS_POST, String.valueOf(forumId));
        this.context.put("permitAnonymousPosts", permitAnonymousPosts);

        Group moderationGroup = groupDao.getEntitlementGroup(SecurityConstants.PERM_MODERATION_FORUMS, forumId);
        if (moderationGroup == null) {
            moderationGroup = this.createModerationGroup(groupDao, forumId);
        }
        final List<User> moderators = DataAccessDriver.getInstance().newUserDAO().selectAllByGroup(moderationGroup.getId(), 0, 50);
        this.context.put("moderationGroup", moderationGroup);
        this.context.put("moderators", moderators);

        final Group replyGroup = groupDao.getEntitlementGroup(SecurityConstants.PERM_REPLY, forumId);
        final List<Group> replySelectedGroups = groupDao.getChildGroups(replyGroup.getId());
        this.context.put("replySelectedGroups", replySelectedGroups);
        this.context.put("replyCandidateGroups", groupDao.getCandidateGroups(replySelectedGroups));

        final Group newPostGroup = groupDao.getEntitlementGroup(SecurityConstants.PERM_NEW_POST, forumId);
        final List<Group> postSelectedGroups = groupDao.getChildGroups(newPostGroup.getId());
        this.context.put("postSelectedGroups", postSelectedGroups);
        this.context.put("postCandidateGroups", groupDao.getCandidateGroups(postSelectedGroups));

        final Group htmlPostGroup = groupDao.getEntitlementGroup(SecurityConstants.PERM_HTML_DISABLED, forumId);
        final List<Group> htmlSelectedGroups = groupDao.getChildGroups(htmlPostGroup.getId());
        this.context.put("htmlSelectedGroups", htmlSelectedGroups);
        this.context.put("htmlCandidateGroups", groupDao.getCandidateGroups(htmlSelectedGroups));

        this.setTemplateName(TemplateKeys.FORUM_ADMIN_EDIT);
        this.context.put("categories", cm.selectAll());
        this.context.put("action", "editSave");
        this.context.put("forum", forumDao.selectById(forumId));

        // Mail Integration
        // MailIntegrationDAO integrationDao =
        // DataAccessDriver.getInstance().newMailIntegrationDAO();
        // this.context.put("mailIntegration", integrationDao.find(forumId));
    }

    public void editSave() {
        final ForumDAO forumDao = DataAccessDriver.getInstance().newForumDAO();
        final int forumId = this.request.getIntParameter("forum_id");
        final Forum f = forumDao.selectById(forumId);

        final boolean moderated = f.isModerated();
        final int categoryId = f.getCategoryId();

        f.setDescription(this.request.getParameter("description"));
        f.setCategoryId(this.request.getIntParameter("categories_id"));
        f.setName(this.request.getParameter("forum_name"));
        f.setLogo(this.request.getParameter("forum_logo"));
        f.setModerated("1".equals(this.request.getParameter("moderate")));

        forumDao.update(f);

        if (moderated != f.isModerated()) {
            new ModerationCommon().setTopicModerationStatus(f.getId(), f.isModerated());
        }

        if (categoryId != f.getCategoryId()) {
            f.setCategoryId(categoryId);
            ForumRepository.removeForum(f);

            f.setCategoryId(this.request.getIntParameter("categories_id"));
            ForumRepository.addForum(f);
        } else {
            ForumRepository.reloadForum(f.getId());
        }

        // this.handleMailIntegration();

        // Forum access
        final GroupDAO groupDao = DataAccessDriver.getInstance().newGroupDAO();
        final Group accessGroup = groupDao.getEntitlementGroup(SecurityConstants.PERM_FORUM, forumId);
        final String[] groupsAccess = request.getParameterValues("groupsAccess");
        updateChildGroups(accessGroup, groupsAccess, groupDao);

        // Anonymous post
        final boolean permitAnonymousPosts = "1".equals(this.request.getParameter("permitAnonymousPosts"));
        final Group anonymousPostsGroup = groupDao.getEntitlementGroup(SecurityConstants.PERM_ANONYMOUS_POST, forumId);
        final UserDAO um = DataAccessDriver.getInstance().newUserDAO();
        final int anonymousUid = Integer.parseInt(SystemGlobals.getValue(ConfigKeys.ANONYMOUS_USER_ID));
        final int[] groupIds = { anonymousPostsGroup.getId() };
        um.removeFromGroup(anonymousUid, groupIds);
        if (permitAnonymousPosts) {
            um.addToGroup(anonymousUid, groupIds);
        }

        // Permit to reply
        final Group replyGroup = groupDao.getEntitlementGroup(SecurityConstants.PERM_REPLY, forumId);
        final String[] groupsReply = request.getParameterValues("groupsReply");
        updateChildGroups(replyGroup, groupsReply, groupDao);

        // Permit to new post
        final Group newPostGroup = groupDao.getEntitlementGroup(SecurityConstants.PERM_NEW_POST, forumId);
        final String[] groupsPost = request.getParameterValues("groupsPost");
        updateChildGroups(newPostGroup, groupsPost, groupDao);

        // HTML
        final Group htmlGroup = groupDao.getEntitlementGroup(SecurityConstants.PERM_HTML_DISABLED, forumId);
        final String[] groupsHtml = request.getParameterValues("groupsHtml");
        updateChildGroups(htmlGroup, groupsHtml, groupDao);

        SecurityRepository.clean();
        RolesRepository.clear();

        this.list();
    }

    // private void handleMailIntegration() {
    // int forumId = this.request.getIntParameter("forum_id");
    // MailIntegrationDAO dao =
    // DataAccessDriver.getInstance().newMailIntegrationDAO();
    //
    // if (!"1".equals(this.request.getParameter("mail_integration"))) {
    // dao.delete(forumId);
    // } else {
    // boolean exists = dao.find(forumId) != null;
    //
    // MailIntegration m = this.fillMailIntegrationFromRequest();
    //
    // if (exists) {
    // dao.update(m);
    // } else {
    // dao.add(m);
    // }
    // }
    // }
    //
    // private MailIntegration fillMailIntegrationFromRequest() {
    // MailIntegration m = new MailIntegration();
    //
    // m.setForumId(this.request.getIntParameter("forum_id"));
    // m.setForumEmail(this.request.getParameter("forum_email"));
    // m.setPopHost(this.request.getParameter("pop_host"));
    // m.setPopUsername(this.request.getParameter("pop_username"));
    // m.setPopPassword(this.request.getParameter("pop_password"));
    // m.setPopPort(this.request.getIntParameter("pop_port"));
    // m.setSSL("1".equals(this.request.getParameter("requires_ssl")));
    //
    // return m;
    // }

    public void up() {
        this.processOrdering(true);
    }

    public void down() {
        this.processOrdering(false);
    }

    private void processOrdering(final boolean up) {
        final Forum toChange = new Forum(ForumRepository.getForum(Integer.parseInt(this.request.getParameter("forum_id"))));

        final Category category = ForumRepository.getCategory(toChange.getCategoryId());
        final List<Forum> forums = Lists.newArrayList(category.getForums());
        final int index = forums.indexOf(toChange);

        if (index == -1 || (up && index == 0) || (!up && index + 1 == forums.size())) {
            this.list();
            return;
        }

        final ForumDAO fm = DataAccessDriver.getInstance().newForumDAO();

        if (up) {
            // Get the forum which comes *before* the forum we're changing
            final Forum otherForum = new Forum(forums.get(index - 1));
            fm.setOrderUp(toChange, otherForum);
        } else {
            // Get the forum which comes *after* the forum we're changing
            final Forum otherForum = new Forum(forums.get(index + 1));
            fm.setOrderDown(toChange, otherForum);
        }

        category.changeForumOrder(toChange);
        ForumRepository.refreshCategory(category);

        this.list();
    }

    // Delete
    public void delete() {
        final String ids[] = this.request.getParameterValues("forum_id");

        final ForumDAO forumDao = DataAccessDriver.getInstance().newForumDAO();
        final TopicDAO topicDao = DataAccessDriver.getInstance().newTopicDAO();

        if (ids != null) {
            for (int i = 0; i < ids.length; i++) {
                final int forumId = Integer.parseInt(ids[i]);

                topicDao.deleteByForum(forumId);
                forumDao.delete(forumId);

                final Forum f = new Forum(ForumRepository.getForum(forumId));
                ForumRepository.removeForum(f);
            }

            SecurityRepository.clean();
            RolesRepository.clear();
        }

        this.list();
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
        this.addRole(pc, SecurityConstants.PERM_MODERATION_TOPIC_MOVE, forumId, moderationGroup);
        this.addRole(pc, SecurityConstants.PERM_MODERATION_TOPIC_LOCK_UNLOCK, forumId, moderationGroup);
        return moderationGroup;
    }

    private void updateChildGroups(final Group parentGroup, String[] groups, final GroupDAO groupDao) {
        if (groups == null) {
            groups = new String[] {};
        }
        final int[] childGroupIDs = new int[groups.length];
        for (int i = 0; i < groups.length; i++) {
            childGroupIDs[i] = Integer.parseInt(groups[i]);
        }
        groupDao.updateChildGroups(parentGroup.getId(), childGroupIDs);
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
}
