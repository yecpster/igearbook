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
import net.jforum.repository.ForumRepository;
import net.jforum.repository.RolesRepository;
import net.jforum.repository.SecurityRepository;
import net.jforum.security.PermissionControl;
import net.jforum.security.Role;
import net.jforum.security.RoleValue;
import net.jforum.security.RoleValueCollection;
import net.jforum.security.SecurityConstants;
import net.jforum.util.GroupNode;
import net.jforum.util.TreeGroup;
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
    public void list() {
        this.context.put("categories", DataAccessDriver.getInstance().newCategoryDAO().selectAll());
        this.context.put("repository", new ForumRepository());
        this.setTemplateName(TemplateKeys.FORUM_ADMIN_LIST);
    }

    // One more, one more
    public void insert() {
        CategoryDAO cm = DataAccessDriver.getInstance().newCategoryDAO();
        List<GroupNode> grups = new TreeGroup().getNodes();

        this.context.put("accessCandidateGroups", grups);
        this.context.put("readOnlyCandidateGroups", grups);
        this.context.put("htmlCandidateGroups", grups);
        this.context.put("selectedList", Lists.newArrayList());
        this.setTemplateName(TemplateKeys.FORUM_ADMIN_INSERT);
        this.context.put("categories", cm.selectAll());
        this.context.put("action", "insertSave");
    }

    // Edit
    public void edit() {
        int forumId = this.request.getIntParameter("forum_id");
        ForumDAO forumDao = DataAccessDriver.getInstance().newForumDAO();

        CategoryDAO cm = DataAccessDriver.getInstance().newCategoryDAO();

        GroupDAO groupDao = DataAccessDriver.getInstance().newGroupDAO();
        Group accessGroup = groupDao.getEntitlementGroup(SecurityConstants.PERM_FORUM, forumId);
        List<Group> accessSelectedGroups = groupDao.getChildGroups(accessGroup.getId());
        this.context.put("accessSelectedGroups", accessSelectedGroups);
        this.context.put("accessCandidateGroups", groupDao.getCandidateGroups(accessSelectedGroups));

        int anonymousUid = Integer.parseInt(SystemGlobals.getValue(ConfigKeys.ANONYMOUS_USER_ID));
        boolean permitAnonymousPosts = SecurityRepository.canAccess(anonymousUid, SecurityConstants.PERM_ANONYMOUS_POST, String.valueOf(forumId));
        this.context.put("permitAnonymousPosts", permitAnonymousPosts);

        Group replyGroup = groupDao.getEntitlementGroup(SecurityConstants.PERM_REPLY, forumId);
        List<Group> replySelectedGroups = groupDao.getChildGroups(replyGroup.getId());
        this.context.put("replySelectedGroups", replySelectedGroups);
        this.context.put("replyCandidateGroups", groupDao.getCandidateGroups(replySelectedGroups));

        Group newPostGroup = groupDao.getEntitlementGroup(SecurityConstants.PERM_NEW_POST, forumId);
        List<Group> postSelectedGroups = groupDao.getChildGroups(newPostGroup.getId());
        this.context.put("postSelectedGroups", postSelectedGroups);
        this.context.put("postCandidateGroups", groupDao.getCandidateGroups(postSelectedGroups));

        Group htmlPostGroup = groupDao.getEntitlementGroup(SecurityConstants.PERM_HTML_DISABLED, forumId);
        List<Group> htmlSelectedGroups = groupDao.getChildGroups(htmlPostGroup.getId());
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
        ForumDAO forumDao = DataAccessDriver.getInstance().newForumDAO();
        int forumId = this.request.getIntParameter("forum_id");
        Forum f = forumDao.selectById(forumId);

        boolean moderated = f.isModerated();
        int categoryId = f.getCategoryId();

        f.setDescription(this.request.getParameter("description"));
        f.setIdCategories(this.request.getIntParameter("categories_id"));
        f.setName(this.request.getParameter("forum_name"));
        f.setLogo(this.request.getParameter("forum_logo"));
        f.setModerated("1".equals(this.request.getParameter("moderate")));

        forumDao.update(f);

        if (moderated != f.isModerated()) {
            new ModerationCommon().setTopicModerationStatus(f.getId(), f.isModerated());
        }

        if (categoryId != f.getCategoryId()) {
            f.setIdCategories(categoryId);
            ForumRepository.removeForum(f);

            f.setIdCategories(this.request.getIntParameter("categories_id"));
            ForumRepository.addForum(f);
        } else {
            ForumRepository.reloadForum(f.getId());
        }

        // this.handleMailIntegration();

        // Forum access
        GroupDAO groupDao = DataAccessDriver.getInstance().newGroupDAO();
        Group accessGroup = groupDao.getEntitlementGroup(SecurityConstants.PERM_FORUM, forumId);
        String[] groupsAccess = request.getParameterValues("groupsAccess");
        updateChildGroups(accessGroup, groupsAccess, groupDao);

        // Anonymous post
        boolean permitAnonymousPosts = "1".equals(this.request.getParameter("permitAnonymousPosts"));
        Group anonymousPostsGroup = groupDao.getEntitlementGroup(SecurityConstants.PERM_ANONYMOUS_POST, forumId);
        UserDAO um = DataAccessDriver.getInstance().newUserDAO();
        int anonymousUid = Integer.parseInt(SystemGlobals.getValue(ConfigKeys.ANONYMOUS_USER_ID));
        int[] groupIds = { anonymousPostsGroup.getId() };
        um.removeFromGroup(anonymousUid, groupIds);
        if (permitAnonymousPosts) {
            um.addToGroup(anonymousUid, groupIds);
        }

        // Permit to reply
        Group replyGroup = groupDao.getEntitlementGroup(SecurityConstants.PERM_REPLY, forumId);
        String[] groupsReply = request.getParameterValues("groupsReply");
        updateChildGroups(replyGroup, groupsReply, groupDao);

        // Permit to new post
        Group newPostGroup = groupDao.getEntitlementGroup(SecurityConstants.PERM_NEW_POST, forumId);
        String[] groupsPost = request.getParameterValues("groupsPost");
        updateChildGroups(newPostGroup, groupsPost, groupDao);

        // HTML
        Group htmlGroup = groupDao.getEntitlementGroup(SecurityConstants.PERM_HTML_DISABLED, forumId);
        String[] groupsHtml = request.getParameterValues("groupsHtml");
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

    private void processOrdering(boolean up) {
        Forum toChange = new Forum(ForumRepository.getForum(Integer.parseInt(this.request.getParameter("forum_id"))));

        Category category = ForumRepository.getCategory(toChange.getCategoryId());
        List<Forum> forums = Lists.newArrayList(category.getForums());
        int index = forums.indexOf(toChange);

        if (index == -1 || (up && index == 0) || (!up && index + 1 == forums.size())) {
            this.list();
            return;
        }

        ForumDAO fm = DataAccessDriver.getInstance().newForumDAO();

        if (up) {
            // Get the forum which comes *before* the forum we're changing
            Forum otherForum = new Forum(forums.get(index - 1));
            fm.setOrderUp(toChange, otherForum);
        } else {
            // Get the forum which comes *after* the forum we're changing
            Forum otherForum = new Forum(forums.get(index + 1));
            fm.setOrderDown(toChange, otherForum);
        }

        category.changeForumOrder(toChange);
        ForumRepository.refreshCategory(category);

        this.list();
    }

    // Delete
    public void delete() {
        String ids[] = this.request.getParameterValues("forum_id");

        ForumDAO forumDao = DataAccessDriver.getInstance().newForumDAO();
        TopicDAO topicDao = DataAccessDriver.getInstance().newTopicDAO();

        if (ids != null) {
            for (int i = 0; i < ids.length; i++) {
                int forumId = Integer.parseInt(ids[i]);

                topicDao.deleteByForum(forumId);
                forumDao.delete(forumId);

                Forum f = new Forum(ForumRepository.getForum(forumId));
                ForumRepository.removeForum(f);
            }

            SecurityRepository.clean();
            RolesRepository.clear();
        }

        this.list();
    }

    // A new one
    public void insertSave() {
        Forum f = new Forum();
        f.setDescription(this.request.getParameter("description"));
        f.setIdCategories(this.request.getIntParameter("categories_id"));
        f.setName(this.request.getParameter("forum_name"));
        f.setLogo(this.request.getParameter("forum_Logo"));
        f.setModerated("1".equals(this.request.getParameter("moderate")));

        int forumId = DataAccessDriver.getInstance().newForumDAO().addNew(f);
        f.setId(forumId);

        ForumRepository.addForum(f);

        GroupSecurityDAO gmodel = DataAccessDriver.getInstance().newGroupSecurityDAO();
        PermissionControl pc = new PermissionControl();
        pc.setSecurityModel(gmodel);

        // Access
        GroupDAO groupDao = DataAccessDriver.getInstance().newGroupDAO();
        Group accessGroup = groupDao.addNewEntitlementGroup(SecurityConstants.PERM_FORUM, forumId);
        this.addRole(pc, SecurityConstants.PERM_FORUM, forumId, accessGroup);
        String[] groupsAccess = request.getParameterValues("groupsAccess");
        updateChildGroups(accessGroup, groupsAccess, groupDao);

        // Anonymous posts
        Group anonymousPostsGroup = groupDao.addNewEntitlementGroup(SecurityConstants.PERM_ANONYMOUS_POST, forumId);
        this.addRole(pc, SecurityConstants.PERM_ANONYMOUS_POST, forumId, anonymousPostsGroup);

        boolean permitAnonymousPosts = "1".equals(this.request.getParameter("permitAnonymousPosts"));
        if (permitAnonymousPosts) {
            UserDAO um = DataAccessDriver.getInstance().newUserDAO();
            int anonymousUid = Integer.parseInt(SystemGlobals.getValue(ConfigKeys.ANONYMOUS_USER_ID));
            um.addToGroup(anonymousUid, new int[] { anonymousPostsGroup.getId() });
        }

        // Permit to replay
        Group replyGroup = groupDao.addNewEntitlementGroup(SecurityConstants.PERM_REPLY, forumId);
        this.addRole(pc, SecurityConstants.PERM_REPLY, forumId, replyGroup);
        String[] groupsReply = request.getParameterValues("groupsReply");
        updateChildGroups(replyGroup, groupsReply, groupDao);

        // Permit to new post
        Group newPostGroup = groupDao.addNewEntitlementGroup(SecurityConstants.PERM_NEW_POST, forumId);
        this.addRole(pc, SecurityConstants.PERM_NEW_POST, forumId, newPostGroup);
        String[] groupsPost = request.getParameterValues("groupsPost");
        updateChildGroups(newPostGroup, groupsPost, groupDao);

        // HTML
        Group htmlGroup = groupDao.addNewEntitlementGroup(SecurityConstants.PERM_HTML_DISABLED, forumId);
        this.addRole(pc, SecurityConstants.PERM_HTML_DISABLED, forumId, htmlGroup);
        String[] groupsHtml = request.getParameterValues("groupsHtml");
        updateChildGroups(htmlGroup, groupsHtml, groupDao);

        SecurityRepository.clean();
        RolesRepository.clear();

        // this.handleMailIntegration();

        this.list();
    }

    private void updateChildGroups(Group parentGroup, String[] groups, GroupDAO groupDao) {
        if (groups == null) {
            groups = new String[] {};
        }
        int[] childGroupIDs = new int[groups.length];
        for (int i = 0; i < groups.length; i++) {
            childGroupIDs[i] = Integer.parseInt(groups[i]);
        }
        groupDao.updateChildGroups(parentGroup.getId(), childGroupIDs);
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
}
