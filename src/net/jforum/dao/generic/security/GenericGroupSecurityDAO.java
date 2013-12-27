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
 * This file creation date: 19/03/2004 - 18:44:56
 * The JForum Project
 * http://www.jforum.net
 */
package net.jforum.dao.generic.security;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.jforum.JForumExecutionContext;
import net.jforum.dao.DataAccessDriver;
import net.jforum.dao.GroupDAO;
import net.jforum.dao.GroupSecurityDAO;
import net.jforum.dao.generic.AutoKeys;
import net.jforum.entities.Group;
import net.jforum.entities.User;
import net.jforum.exceptions.DatabaseException;
import net.jforum.security.Role;
import net.jforum.security.RoleCollection;
import net.jforum.security.RoleValue;
import net.jforum.security.RoleValueCollection;
import net.jforum.util.DbUtils;
import net.jforum.util.preferences.SystemGlobals;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * @author Rafael Steil
 * @version $Id: GenericGroupSecurityDAO.java,v 1.16 2007/08/25 00:11:29 rafaelsteil Exp $
 */
public class GenericGroupSecurityDAO extends AutoKeys implements GroupSecurityDAO {
    private List<Integer> selectForumRoles(final int forumId) {
        final List<Integer> l = Lists.newArrayList();

        PreparedStatement p = null;
        ResultSet rs = null;

        try {
            p = JForumExecutionContext.getConnection().prepareStatement(SystemGlobals.getSql("PermissionControl.selectForumRoles"));
            p.setString(1, String.valueOf(forumId));

            rs = p.executeQuery();

            while (rs.next()) {
                l.add(new Integer(rs.getInt("role_id")));
            }
        } catch (final SQLException e) {
            throw new DatabaseException(e);
        } finally {
            DbUtils.close(rs, p);
        }

        return l;
    }

    @Override
    public void deleteForumRoles(final int forumId) {
        PreparedStatement p = null;

        final List<Integer> roleIds = this.selectForumRoles(forumId);

        try {
            final StringBuffer ids = new StringBuffer();

            for (final Integer id : roleIds) {
                ids.append(id).append(',');
            }

            ids.append("-1");

            // Role values
            String sql = SystemGlobals.getSql("PermissionControl.deleteRoleValues");
            sql = StringUtils.replace(sql, "#IDS#", ids.toString());

            p = JForumExecutionContext.getConnection().prepareStatement(sql);
            p.setString(1, String.valueOf(forumId));
            p.executeUpdate();
        } catch (final SQLException e) {
            throw new DatabaseException(e);
        } finally {
            DbUtils.close(p);
        }
    }

    /**
     * @see net.jforum.dao.security.SecurityDAO#deleteAllRoles(int)
     */
    @Override
    public void deleteAllRoles(final int groupId) {
        PreparedStatement p = null;

        try {
            p = JForumExecutionContext.getConnection().prepareStatement(SystemGlobals.getSql("PermissionControl.deleteAllRoleValues"));
            p.setInt(1, groupId);
            p.executeUpdate();
            p.close();

            p = JForumExecutionContext.getConnection().prepareStatement(SystemGlobals.getSql("PermissionControl.deleteAllGroupRoles"));
            p.setInt(1, groupId);
            p.executeUpdate();
        } catch (final SQLException e) {
            throw new DatabaseException(e);
        } finally {
            DbUtils.close(p);
        }
    }

    /**
     * @see net.jforum.dao.security.SecurityDAO#addRole(int, net.jforum.security.Role)
     */
    @Override
    public void addRole(final int id, final Role role) {
        this.addRole(id, role, null);
    }

    /**
     * @see net.jforum.dao.security.SecurityDAO#addRole(int, net.jforum.security.Role, net.jforum.security.RoleValueCollection)
     */
    @Override
    public void addRole(final int id, final Role role, final RoleValueCollection roleValues) {
        this.setAutoGeneratedKeysQuery(SystemGlobals.getSql("PermissionControl.lastGeneratedRoleId"));
        SecurityCommon.executeAddRole(SystemGlobals.getSql("PermissionControl.addGroupRole"), id, role, roleValues, this.supportAutoGeneratedKeys(),
                this.getAutoGeneratedKeysQuery());
    }

    /**
     * @see net.jforum.dao.security.SecurityDAO#loadRoles(int)
     */
    @Override
    public RoleCollection loadRoles(final int groupId) {
        final Group group = new Group();
        group.setId(groupId);
        final int[] groupsIds = getAllParentGroups(Lists.newArrayList(group));
        return this.loadRoles(groupsIds);
    }

    private int[] getAllParentGroups(final List<Group> groups) {
        final Set<Integer> allGroupIdSet = Sets.newHashSet();
        for (final Group group : groups) {
            final Set<Integer> tmpGroupIds = Sets.newHashSet();
            enrichWithParentGroups(group.getId(), tmpGroupIds);
            allGroupIdSet.addAll(tmpGroupIds);
            allGroupIdSet.add(group.getId());
        }

        final int[] groupIds = new int[allGroupIdSet.size()];
        int i = 0;
        for (final Integer allGroupId : allGroupIdSet) {
            groupIds[i++] = allGroupId;
        }
        Arrays.sort(groupIds);

        return groupIds;
    }

    private void enrichWithParentGroups(final int groupId, final Set<Integer> childIds) {
        final GroupDAO groupDAO = DataAccessDriver.getInstance().newGroupDAO();
        final Set<Integer> parentGroupIds = groupDAO.getParentGroups(groupId);
        for (final Integer parentGroupId : parentGroupIds) {
            if (childIds.contains(parentGroupId) || parentGroupId == groupId) {
                throw new RuntimeException("Found cycle group relationship!");
            }
        }
        childIds.addAll(parentGroupIds);
        for (final Integer parentGroupId : parentGroupIds) {
            enrichWithParentGroups(parentGroupId, parentGroupIds);
        }
    }

    protected RoleCollection loadRoles(final int[] groupIds) {
        String sql = SystemGlobals.getSql("PermissionControl.loadGroupRoles");
        final String groupIdAsString = DbUtils.makeString(groupIds);
        sql = sql.replaceAll("#IN#", groupIdAsString);

        RoleCollection roles = new RoleCollection();

        PreparedStatement p = null;
        ResultSet rs = null;

        try {
            p = JForumExecutionContext.getConnection().prepareStatement(sql);
            rs = p.executeQuery();

            roles = SecurityCommon.loadRoles(rs);
        } catch (final Exception e) {
            throw new DatabaseException(e);
        } finally {
            DbUtils.close(rs, p);
        }

        return roles;
    }

    /**
     * @see net.jforum.dao.GroupSecurityDAO#addRoleValue(int, net.jforum.security.Role, net.jforum.security.RoleValueCollection)
     */
    @Override
    public void addRoleValue(final int groupId, final Role role, final RoleValueCollection rvc) {
        PreparedStatement p = null;
        ResultSet rs = null;

        try {
            p = JForumExecutionContext.getConnection().prepareStatement(SystemGlobals.getSql("PermissionControl.getRoleIdByName"));
            p.setString(1, role.getName());
            p.setInt(2, groupId);

            int roleId = -1;

            rs = p.executeQuery();
            if (rs.next()) {
                roleId = rs.getInt("role_id");
            }

            rs.close();
            rs = null;
            p.close();
            p = null;

            if (roleId == -1) {
                this.addRole(groupId, role, rvc);
            } else {
                p = JForumExecutionContext.getConnection().prepareStatement(SystemGlobals.getSql("PermissionControl.addRoleValues"));
                p.setInt(1, roleId);

                for (final Iterator iter = rvc.iterator(); iter.hasNext();) {
                    final RoleValue rv = (RoleValue) iter.next();
                    p.setString(2, rv.getValue());
                    p.executeUpdate();
                }
            }
        } catch (final SQLException e) {
            throw new DatabaseException(e);
        } finally {
            DbUtils.close(rs, p);
        }
    }

    /**
     * @see net.jforum.dao.GroupSecurityDAO#loadRolesByUserGroups(net.jforum.entities.User)
     */
    @Override
    public RoleCollection loadRolesByUserGroups(final User user) {
        final List<Group> groups = user.getGroupsList();

        // When the user is associated to more than one group, we
        // should check the merged roles
        final int[] groupIds = getAllParentGroups(groups);

        final RoleCollection groupRoles = this.loadRoles(groupIds);

        return groupRoles;
    }
}
