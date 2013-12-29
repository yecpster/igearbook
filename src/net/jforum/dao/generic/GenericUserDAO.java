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
 * This file creation date: Apr 5, 2003 / 11:43:46 PM
 * The JForum Project
 * http://www.jforum.net
 */
package net.jforum.dao.generic;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import net.jforum.JForumExecutionContext;
import net.jforum.dao.DataAccessDriver;
import net.jforum.dao.UserDAO;
import net.jforum.entities.Group;
import net.jforum.entities.KarmaStatus;
import net.jforum.entities.User;
import net.jforum.exceptions.DatabaseException;
import net.jforum.exceptions.ForumException;
import net.jforum.sso.LoginAuthenticator;
import net.jforum.util.DbUtils;
import net.jforum.util.preferences.ConfigKeys;
import net.jforum.util.preferences.SystemGlobals;

/**
 * @author Rafael Steil
 * @version $Id: GenericUserDAO.java,v 1.30 2008/06/07 05:29:40 andowson Exp $
 */
public class GenericUserDAO extends AutoKeys implements UserDAO {
    private static LoginAuthenticator loginAuthenticator;

    public GenericUserDAO() {
        loginAuthenticator = (LoginAuthenticator) SystemGlobals.getObjectValue(ConfigKeys.LOGIN_AUTHENTICATOR_INSTANCE);

        if (loginAuthenticator == null) {
            throw new ForumException("There is no login.authenticator configured. Check your login.authenticator configuration key.");
        }

        loginAuthenticator.setUserModel(this);
    }

    /**
     * @see net.jforum.dao.UserDAO#pendingActivations()
     */
    @Override
    public List pendingActivations() {
        PreparedStatement p = null;
        ResultSet rs = null;

        final List l = new ArrayList();

        try {
            p = JForumExecutionContext.getConnection().prepareStatement(SystemGlobals.getSql("UserModel.pendingActivations"));

            rs = p.executeQuery();

            while (rs.next()) {
                final User user = new User();

                user.setId(rs.getInt("user_id"));
                user.setUsername(rs.getString("username"));
                user.setRegistrationDate(new Date(rs.getTimestamp("user_regdate").getTime()));

                l.add(user);
            }
        } catch (final SQLException e) {
            throw new DatabaseException(e);
        } finally {
            DbUtils.close(rs, p);
        }

        return l;
    }

    /**
     * @see net.jforum.dao.UserDAO#selectById(int)
     */
    @Override
    public User selectById(final int userId) {
        final String q = SystemGlobals.getSql("UserModel.selectById");
        PreparedStatement p = null;
        ResultSet rs = null;

        try {
            p = JForumExecutionContext.getConnection().prepareStatement(q);
            p.setInt(1, userId);

            rs = p.executeQuery();
            final User u = new User();

            if (rs.next()) {
                this.fillUserFromResultSet(u, rs);
                u.setPrivateMessagesCount(rs.getInt("private_messages"));

                rs.close();
                p.close();

                // User groups
                p = JForumExecutionContext.getConnection().prepareStatement(SystemGlobals.getSql("UserModel.selectGroups"));
                p.setInt(1, userId);

                rs = p.executeQuery();
                while (rs.next()) {
                    final Group g = new Group();
                    g.setName(rs.getString("group_name"));
                    g.setId(rs.getInt("group_id"));

                    u.getGroupsList().add(g);
                }
            }

            return u;
        } catch (final SQLException e) {
            throw new DatabaseException(e);
        } finally {
            DbUtils.close(rs, p);
        }
    }

    @Override
    public User selectByName(final String username) {
        PreparedStatement p = null;
        ResultSet rs = null;
        try {
            p = JForumExecutionContext.getConnection().prepareStatement(SystemGlobals.getSql("UserModel.selectByName"));
            p.setString(1, username);

            rs = p.executeQuery();
            User u = null;

            if (rs.next()) {
                u = new User();
                this.fillUserFromResultSet(u, rs);
            }

            return u;
        } catch (final SQLException e) {
            throw new DatabaseException(e);
        } finally {
            DbUtils.close(rs, p);
        }
    }

    protected void fillUserFromResultSet(final User u, final ResultSet rs) throws SQLException {
        u.setAim(rs.getString("user_aim"));
        u.setAvatar(rs.getString("user_avatar"));
        u.setGender(rs.getString("gender"));
        u.setRankId(rs.getInt("rank_id"));
        u.setThemeId(rs.getInt("themes_id"));
        u.setPrivateMessagesEnabled(rs.getInt("user_allow_pm") == 1);
        u.setNotifyOnMessagesEnabled(rs.getInt("user_notify") == 1);
        u.setViewOnlineEnabled(rs.getInt("user_viewonline") == 1);
        u.setPassword(rs.getString("user_password"));
        u.setViewEmailEnabled(rs.getInt("user_viewemail") == 1);
        u.setViewOnlineEnabled(rs.getInt("user_viewonline") == 1);
        u.setAvatarEnabled(rs.getInt("user_allowavatar") == 1);
        u.setBbCodeEnabled(rs.getInt("user_allowbbcode") == 1);
        u.setHtmlEnabled(rs.getInt("user_allowhtml") == 1);
        u.setSmiliesEnabled(rs.getInt("user_allowsmilies") == 1);
        u.setEmail(rs.getString("user_email"));
        u.setFrom(rs.getString("user_from"));
        u.setIcq(rs.getString("user_icq"));
        u.setId(rs.getInt("user_id"));
        u.setInterests(rs.getString("user_interests"));
        u.setBiography(rs.getString("user_biography"));
        u.setLastVisit(rs.getTimestamp("user_lastvisit"));
        u.setOccupation(rs.getString("user_occ"));
        u.setTotalPosts(rs.getInt("user_posts"));
        u.setRegistrationDate(new Date(rs.getTimestamp("user_regdate").getTime()));
        u.setSignature(rs.getString("user_sig"));
        u.setWebSite(rs.getString("user_website"));
        u.setYim(rs.getString("user_yim"));
        u.setUsername(rs.getString("username"));
        u.setAttachSignatureEnabled(rs.getInt("user_attachsig") == 1);
        u.setMsnm(rs.getString("user_msnm"));
        u.setLang(rs.getString("user_lang"));
        u.setActive(rs.getInt("user_active")==1);
        u.setKarma(new KarmaStatus(u.getId(), rs.getDouble("user_karma")));
        u.setNotifyPrivateMessagesEnabled(rs.getInt("user_notify_pm") == 1);
        u.setDeleted(rs.getInt("deleted")==1);
        u.setNotifyAlways(rs.getInt("user_notify_always") == 1);
        u.setNotifyText(rs.getInt("user_notify_text") == 1);

        final String actkey = rs.getString("user_actkey");
        u.setActivationKey(actkey == null || "".equals(actkey) ? null : actkey);
    }

    /**
     * @see net.jforum.dao.UserDAO#delete(int)
     */
    @Override
    public void delete(final int userId) {
        PreparedStatement p = null;
        try {
            p = JForumExecutionContext.getConnection().prepareStatement(SystemGlobals.getSql("UserModel.deletedStatus"));
            p.setInt(1, 1);
            p.setInt(2, userId);

            p.executeUpdate();
        } catch (final SQLException e) {
            throw new DatabaseException(e);
        } finally {
            DbUtils.close(p);
        }
    }

    /**
     * @see net.jforum.dao.UserDAO#update(net.jforum.entities.User)
     */
    @Override
    public void update(final User user) {
        PreparedStatement p = null;
        try {
            p = JForumExecutionContext.getConnection().prepareStatement(SystemGlobals.getSql("UserModel.update"));

            p.setString(1, user.getAim());
            p.setString(2, user.getAvatar());
            p.setString(3, user.getGender());
            p.setInt(4, user.getThemeId());
            p.setInt(5, user.isPrivateMessagesEnabled() ? 1 : 0);
            p.setInt(6, user.isAvatarEnabled() ? 1 : 0);
            p.setInt(7, user.isBbCodeEnabled() ? 1 : 0);
            p.setInt(8, user.isHtmlEnabled() ? 1 : 0);
            p.setInt(9, user.isSmiliesEnabled() ? 1 : 0);
            p.setString(10, user.getEmail());
            p.setString(11, user.getFrom());
            p.setString(12, user.getIcq());
            p.setString(13, user.getInterests());
            p.setString(14, user.getOccupation());
            p.setString(15, user.getSignature());
            p.setString(16, user.getWebSite());
            p.setString(17, user.getYim());
            p.setString(18, user.getMsnm());
            p.setString(19, user.getPassword());
            p.setInt(20, user.isViewEmailEnabled() ? 1 : 0);
            p.setInt(21, user.isViewOnlineEnabled() ? 1 : 0);
            p.setInt(22, user.isNotifyOnMessagesEnabled() ? 1 : 0);
            p.setInt(23, user.getAttachSignatureEnabled() ? 1 : 0);
            p.setString(24, user.getUsername());
            p.setString(25, user.getLang());
            p.setInt(26, user.isNotifyPrivateMessagesEnabled() ? 1 : 0);
            p.setString(27, user.getBiography());

            if (user.getLastVisit() == null) {
                user.setLastVisit(new Date());
            }

            p.setTimestamp(28, new Timestamp(user.getLastVisit().getTime()));
            p.setInt(29, user.notifyAlways() ? 1 : 0);
            p.setInt(30, user.notifyText() ? 1 : 0);
            p.setInt(31, user.getRankId());
            p.setInt(32, user.getId());

            p.executeUpdate();
        } catch (final SQLException e) {
            throw new DatabaseException(e);
        } finally {
            DbUtils.close(p);
        }
    }

    /**
     * @see net.jforum.dao.UserDAO#addNew(net.jforum.entities.User)
     */
    @Override
    public int addNew(final User user) {
        PreparedStatement p = null;
        try {
            p = this.getStatementForAutoKeys("UserModel.addNew");

            this.initNewUser(user, p);

            this.setAutoGeneratedKeysQuery(SystemGlobals.getSql("UserModel.lastGeneratedUserId"));
            final int id = this.executeAutoKeysQuery(p);

            this.addToGroup(id, new int[] { SystemGlobals.getIntValue(ConfigKeys.DEFAULT_USER_GROUP) });

            user.setId(id);
            return id;
        } catch (final SQLException e) {
            throw new DatabaseException(e);
        } finally {
            DbUtils.close(p);
        }
    }

    protected void initNewUser(final User user, final PreparedStatement p) throws SQLException {
        p.setString(1, user.getUsername());
        p.setString(2, user.getPassword());
        p.setString(3, user.getEmail());
        p.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
        p.setString(5, user.getActivationKey());
    }

    /**
     * @see net.jforum.dao.UserDAO#addNewWithId(net.jforum.entities.User)
     */
    @Override
    public void addNewWithId(final User user) {
        PreparedStatement p = null;
        try {
            p = this.getStatementForAutoKeys("UserModel.addNewWithId");

            this.initNewUser(user, p);
            p.setInt(6, user.getId());

            p.executeUpdate();

            this.addToGroup(user.getId(), new int[] { SystemGlobals.getIntValue(ConfigKeys.DEFAULT_USER_GROUP) });
        } catch (final SQLException e) {
            throw new DatabaseException(e);
        } finally {
            DbUtils.close(p);
        }
    }

    /**
     * @see net.jforum.dao.UserDAO#decrementPosts(int)
     */
    @Override
    public void decrementPosts(final int userId) {
        PreparedStatement p = null;
        try {
            p = JForumExecutionContext.getConnection().prepareStatement(SystemGlobals.getSql("UserModel.decrementPosts"));
            p.setInt(1, userId);

            p.executeUpdate();
        } catch (final SQLException e) {
            throw new DatabaseException(e);
        } finally {
            DbUtils.close(p);
        }
    }

    /**
     * @see net.jforum.dao.UserDAO#incrementPosts(int)
     */
    @Override
    public void incrementPosts(final int userId) {
        PreparedStatement p = null;
        try {
            p = JForumExecutionContext.getConnection().prepareStatement(SystemGlobals.getSql("UserModel.incrementPosts"));
            p.setInt(1, userId);

            p.executeUpdate();
        } catch (final SQLException e) {
            throw new DatabaseException(e);
        } finally {
            DbUtils.close(p);
        }
    }

    /**
     * @see net.jforum.dao.UserDAO#incrementRanking(int)
     */
    @Override
    public void setRanking(final int userId, final int rankingId) {
        PreparedStatement p = null;
        try {
            p = JForumExecutionContext.getConnection().prepareStatement(SystemGlobals.getSql("UserModel.rankingId"));
            p.setInt(1, rankingId);
            p.setInt(2, userId);

            p.executeUpdate();
        } catch (final SQLException e) {
            throw new DatabaseException(e);
        } finally {
            DbUtils.close(p);
        }
    }

    /**
     * @see net.jforum.dao.UserDAO#setActive(int, boolean)
     */
    @Override
    public void setActive(final int userId, final boolean active) {
        PreparedStatement p = null;
        try {
            p = JForumExecutionContext.getConnection().prepareStatement(SystemGlobals.getSql("UserModel.activeStatus"));
            p.setInt(1, active ? 1 : 0);
            p.setInt(2, userId);

            p.executeUpdate();
        } catch (final SQLException e) {
            throw new DatabaseException(e);
        } finally {
            DbUtils.close(p);
        }
    }

    /**
     * @see net.jforum.dao.UserDAO#undelete(int)
     */
    @Override
    public void undelete(final int userId) {
        PreparedStatement p = null;
        try {
            p = JForumExecutionContext.getConnection().prepareStatement(SystemGlobals.getSql("UserModel.deletedStatus"));
            p.setInt(1, 0);
            p.setInt(2, userId);

            p.executeUpdate();
        } catch (final SQLException e) {
            throw new DatabaseException(e);
        } finally {
            DbUtils.close(p);
        }
    }

    /**
     * @see net.jforum.dao.UserDAO#selectAll()
     */
    @Override
    public List selectAll() {
        return selectAll(0, 0);
    }

    /**
     * @see net.jforum.dao.UserDAO#selectAll(int, int)
     */
    @Override
    public List selectAll(final int startFrom, final int count) {
        PreparedStatement p = null;
        ResultSet rs = null;

        try {
            if (count > 0) {
                p = JForumExecutionContext.getConnection().prepareStatement(SystemGlobals.getSql("UserModel.selectAllByLimit"));
                p.setInt(1, startFrom);
                p.setInt(2, count);
            } else {
                p = JForumExecutionContext.getConnection().prepareStatement(SystemGlobals.getSql("UserModel.selectAll"));
            }

            rs = p.executeQuery();

            return this.processSelectAll(rs);
        } catch (final SQLException e) {
            throw new DatabaseException(e);
        } finally {
            DbUtils.close(rs, p);
        }
    }

    /**
     * @see net.jforum.dao.UserDAO#selectAllWithKarma()
     */
    @Override
    public List selectAllWithKarma() {
        return this.selectAllWithKarma(0, 0);
    }

    /**
     * @see net.jforum.dao.UserDAO#selectAllWithKarma(int, int)
     */
    @Override
    public List selectAllWithKarma(final int startFrom, final int count) {
        return this.loadKarma(this.selectAll(startFrom, count));
    }

    protected List processSelectAll(final ResultSet rs) throws SQLException {
        final List list = new ArrayList();

        while (rs.next()) {
            final User u = new User();

            u.setEmail(rs.getString("user_email"));
            u.setId(rs.getInt("user_id"));
            u.setAvatar(rs.getString("user_avatar"));
            u.setTotalPosts(rs.getInt("user_posts"));
            u.setRegistrationDate(new Date(rs.getTimestamp("user_regdate").getTime()));
            u.setUsername(rs.getString("username"));
            u.setDeleted(rs.getInt("deleted")==1);
            final KarmaStatus karma = new KarmaStatus();
            karma.setKarmaPoints(rs.getInt("user_karma"));
            u.setKarma(karma);
            u.setFrom(rs.getString("user_from"));
            u.setWebSite(rs.getString("user_website"));
            u.setViewEmailEnabled(rs.getInt("user_viewemail") == 1);

            list.add(u);
        }

        return list;
    }

    /**
     * @see net.jforum.dao.UserDAO#selectAllByGroup(int, int, int)
     */
    @Override
    public List selectAllByGroup(final int groupId, final int start, final int count) {
        PreparedStatement p = null;
        ResultSet rs = null;
        try {
            p = JForumExecutionContext.getConnection().prepareStatement(SystemGlobals.getSql("UserModel.selectAllByGroup"));
            p.setInt(1, groupId);
            p.setInt(2, start);
            p.setInt(3, count);

            rs = p.executeQuery();

            return this.processSelectAll(rs);
        } catch (final SQLException e) {
            throw new DatabaseException(e);
        } finally {
            DbUtils.close(rs, p);
        }
    }

    /**
     * @see net.jforum.dao.UserDAO#getLastUserInfo()
     */
    @Override
    public User getLastUserInfo() {
        PreparedStatement p = null;
        ResultSet rs = null;
        try {
            final User u = new User();

            p = JForumExecutionContext.getConnection().prepareStatement(SystemGlobals.getSql("UserModel.lastUserRegistered"));
            rs = p.executeQuery();
            rs.next();

            u.setUsername(rs.getString("username"));
            u.setId(rs.getInt("user_id"));

            return u;
        } catch (final SQLException e) {
            throw new DatabaseException(e);
        } finally {
            DbUtils.close(rs, p);
        }
    }

    /**
     * @see net.jforum.dao.UserDAO#getTotalUsers()
     */
    @Override
    public int getTotalUsers() {
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = JForumExecutionContext.getConnection().prepareStatement(SystemGlobals.getSql("UserModel.totalUsers"));
            return this.getTotalUsersCommon(preparedStatement);
        } catch (final SQLException e) {
            throw new DatabaseException(e);
        } finally {
            DbUtils.close(preparedStatement);
        }
    }

    /**
     * @see net.jforum.dao.UserDAO#getTotalUsersByGroup(int)
     */
    @Override
    public int getTotalUsersByGroup(final int groupId) {
        PreparedStatement p = null;
        try {
            p = JForumExecutionContext.getConnection().prepareStatement(SystemGlobals.getSql("UserModel.totalUsersByGroup"));
            p.setInt(1, groupId);

            return this.getTotalUsersCommon(p);
        } catch (final SQLException e) {
            throw new DatabaseException(e);
        } finally {
            DbUtils.close(p);
        }
    }

    protected int getTotalUsersCommon(final PreparedStatement p) throws SQLException {
        final ResultSet rs = p.executeQuery();
        rs.next();

        final int total = rs.getInt(1);

        rs.close();

        return total;
    }

    /**
     * @see net.jforum.dao.UserDAO#isDeleted(int user_id)
     */
    @Override
    public boolean isDeleted(final int userId) {
        PreparedStatement p = null;
        ResultSet rs = null;
        try {
            p = JForumExecutionContext.getConnection().prepareStatement(SystemGlobals.getSql("UserModel.isDeleted"));
            p.setInt(1, userId);

            int deleted = 0;

            rs = p.executeQuery();
            if (rs.next()) {
                deleted = rs.getInt("deleted");
            }

            return deleted == 1;
        } catch (final SQLException e) {
            throw new DatabaseException(e);
        } finally {
            DbUtils.close(rs, p);
        }
    }

    /**
     * @see net.jforum.dao.UserDAO#isUsernameRegistered(java.lang.String)
     */
    @Override
    public boolean isUsernameRegistered(final String username) {
        boolean status = false;

        PreparedStatement p = null;
        ResultSet rs = null;
        try {
            p = JForumExecutionContext.getConnection().prepareStatement(SystemGlobals.getSql("UserModel.isUsernameRegistered"));
            p.setString(1, username);

            rs = p.executeQuery();
            if (rs.next() && rs.getInt("registered") > 0) {
                status = true;
            }

            return status;
        } catch (final SQLException e) {
            throw new DatabaseException(e);
        } finally {
            DbUtils.close(rs, p);
        }
    }

    /**
     * @see net.jforum.dao.UserDAO#validateLogin(java.lang.String,
     *      java.lang.String)
     */
    @Override
    public User validateLogin(final String username, final String password) {
        return loginAuthenticator.validateLogin(username, password, null);
    }

    /**
     * @see net.jforum.dao.UserDAO#addToGroup(int, int[])
     */
    @Override
    public void addToGroup(final int userId, final int[] groupId) {
        PreparedStatement p = null;
        try {
            p = JForumExecutionContext.getConnection().prepareStatement(SystemGlobals.getSql("UserModel.addToGroup"));
            p.setInt(1, userId);

            for (int i = 0; i < groupId.length; i++) {
                p.setInt(2, groupId[i]);
                p.executeUpdate();
            }
        } catch (final SQLException e) {
            throw new DatabaseException(e);
        } finally {
            DbUtils.close(p);
        }

    }

    /**
     * @see net.jforum.dao.UserDAO#removeFromGroup(int, int[])
     */
    @Override
    public void removeFromGroup(final int userId, final int[] groupId) {
        PreparedStatement p = null;
        try {
            p = JForumExecutionContext.getConnection().prepareStatement(SystemGlobals.getSql("UserModel.removeFromGroup"));
            p.setInt(1, userId);

            for (int i = 0; i < groupId.length; i++) {
                p.setInt(2, groupId[i]);
                p.executeUpdate();
            }
        } catch (final SQLException e) {
            throw new DatabaseException(e);
        } finally {
            DbUtils.close(p);
        }
    }

    /**
     * @see net.jforum.dao.UserDAO#saveNewPassword(java.lang.String,
     *      java.lang.String)
     */
    @Override
    public void saveNewPassword(final String password, final String email) {
        PreparedStatement p = null;
        try {
            p = JForumExecutionContext.getConnection().prepareStatement(SystemGlobals.getSql("UserModel.saveNewPassword"));
            p.setString(1, password);
            p.setString(2, email);
            p.executeUpdate();
        } catch (final SQLException e) {
            throw new DatabaseException(e);
        } finally {
            DbUtils.close(p);
        }
    }

    /**
     * @see net.jforum.dao.UserDAO#validateLostPasswordHash(java.lang.String,
     *      java.lang.String)
     */
    @Override
    public boolean validateLostPasswordHash(final String email, final String hash) {
        PreparedStatement p = null;
        ResultSet rs = null;
        try {
            p = JForumExecutionContext.getConnection().prepareStatement(SystemGlobals.getSql("UserModel.validateLostPasswordHash"));
            p.setString(1, hash);
            p.setString(2, email);

            boolean status = false;

            rs = p.executeQuery();
            if (rs.next() && rs.getInt("valid") > 0) {
                status = true;

                this.writeLostPasswordHash(email, "");
            }

            return status;
        } catch (final SQLException e) {
            throw new DatabaseException(e);
        } finally {
            DbUtils.close(rs, p);
        }
    }

    /**
     * @see net.jforum.dao.UserDAO#writeLostPasswordHash(java.lang.String,
     *      java.lang.String)
     */
    @Override
    public void writeLostPasswordHash(final String email, final String hash) {
        PreparedStatement p = null;
        try {
            p = JForumExecutionContext.getConnection().prepareStatement(SystemGlobals.getSql("UserModel.writeLostPasswordHash"));
            p.setString(1, hash);
            p.setString(2, email);
            p.executeUpdate();
        } catch (final SQLException e) {
            throw new DatabaseException(e);
        } finally {
            DbUtils.close(p);
        }
    }

    /**
     * @see net.jforum.dao.UserDAO#getUsernameByEmail(java.lang.String)
     */
    @Override
    public String getUsernameByEmail(final String email) {
        PreparedStatement p = null;
        ResultSet rs = null;
        try {
            p = JForumExecutionContext.getConnection().prepareStatement(SystemGlobals.getSql("UserModel.getUsernameByEmail"));
            p.setString(1, email);

            String username = "";

            rs = p.executeQuery();
            if (rs.next()) {
                username = rs.getString("username");
            }

            return username;
        } catch (final SQLException e) {
            throw new DatabaseException(e);
        } finally {
            DbUtils.close(rs, p);
        }
    }

    /**
     * @see net.jforum.dao.UserDAO#findByName(java.lang.String, boolean)
     */
    @Override
    public List findByName(final String input, final boolean exactMatch) {
        final List namesList = new ArrayList();

        PreparedStatement p = null;
        ResultSet rs = null;
        try {
            p = JForumExecutionContext.getConnection().prepareStatement(SystemGlobals.getSql("UserModel.findByName"));
            p.setString(1, exactMatch ? input : "%" + input + "%");

            rs = p.executeQuery();
            while (rs.next()) {
                final User u = new User();

                u.setId(rs.getInt("user_id"));
                u.setUsername(rs.getString("username"));
                u.setEmail(rs.getString("user_email"));
                u.setDeleted(rs.getInt("deleted")==1);

                namesList.add(u);
            }
            return namesList;
        } catch (final SQLException e) {
            throw new DatabaseException(e);
        } finally {
            DbUtils.close(rs, p);
        }
    }

    /**
     * @see net.jforum.dao.UserDAO#validateActivationKeyHash(int,
     *      java.lang.String)
     */
    @Override
    public boolean validateActivationKeyHash(final int userId, final String hash) {
        PreparedStatement p = null;
        ResultSet rs = null;
        try {
            p = JForumExecutionContext.getConnection().prepareStatement(SystemGlobals.getSql("UserModel.validateActivationKeyHash"));
            p.setString(1, hash);
            p.setInt(2, userId);

            boolean status = false;

            rs = p.executeQuery();
            if (rs.next() && rs.getInt("valid") == 1) {
                status = true;
            }

            return status;
        } catch (final SQLException e) {
            throw new DatabaseException(e);
        } finally {
            DbUtils.close(rs, p);
        }
    }

    /**
     * @see net.jforum.dao.UserDAO#writeUserActive(int)
     */
    @Override
    public void writeUserActive(final int userId) {
        PreparedStatement p = null;
        try {
            p = JForumExecutionContext.getConnection().prepareStatement(SystemGlobals.getSql("UserModel.writeUserActive"));
            p.setInt(1, userId);
            p.executeUpdate();
        } catch (final SQLException e) {
            throw new DatabaseException(e);
        } finally {
            DbUtils.close(p);
        }
    }

    /**
     * @see net.jforum.dao.UserDAO#updateUsername(int, String)
     */
    @Override
    public void updateUsername(final int userId, final String username) {
        PreparedStatement p = null;
        try {
            p = JForumExecutionContext.getConnection().prepareStatement(SystemGlobals.getSql("UserModel.updateUsername"));
            p.setString(1, username);
            p.setInt(2, userId);
            p.executeUpdate();
        } catch (final SQLException e) {
            throw new DatabaseException(e);
        } finally {
            DbUtils.close(p);
        }
    }

    /**
     * @see net.jforum.dao.UserDAO#hasUsernameChanged(int, java.lang.String)
     */
    @Override
    public boolean hasUsernameChanged(final int userId, final String usernameToCheck) {
        PreparedStatement p = null;
        ResultSet rs = null;
        try {
            p = JForumExecutionContext.getConnection().prepareStatement(SystemGlobals.getSql("UserModel.getUsername"));
            p.setString(1, usernameToCheck);
            p.setInt(2, userId);

            String dbUsername = null;

            rs = p.executeQuery();
            if (rs.next()) {
                dbUsername = rs.getString("username");
            }

            boolean status = false;

            if (!usernameToCheck.equals(dbUsername)) {
                status = true;
            }

            return status;
        } catch (final SQLException e) {
            throw new DatabaseException(e);
        } finally {
            DbUtils.close(rs, p);
        }
    }

    /**
     * Load KarmaStatus from a list of users.
     * 
     * @param users
     *            List
     * @return List
     * @throws SQLException
     */
    protected List loadKarma(final List users) {
        final List result = new ArrayList(users.size());

        for (final Iterator iter = users.iterator(); iter.hasNext();) {
            final User user = (User) iter.next();

            // Load Karma
            DataAccessDriver.getInstance().newKarmaDAO().getUserTotalKarma(user);
            result.add(user);
        }

        return result;
    }

    /**
     * @see net.jforum.dao.UserDAO#saveUserAuthHash(int, java.lang.String)
     */
    @Override
    public void saveUserAuthHash(final int userId, final String hash) {
        PreparedStatement p = null;
        try {
            p = JForumExecutionContext.getConnection().prepareStatement(SystemGlobals.getSql("UserModel.saveUserAuthHash"));
            p.setString(1, hash);
            p.setInt(2, userId);
            p.executeUpdate();
        } catch (final SQLException e) {
            throw new DatabaseException(e);
        } finally {
            DbUtils.close(p);
        }
    }

    /**
     * @see net.jforum.dao.UserDAO#getUserAuthHash(int)
     */
    @Override
    public String getUserAuthHash(final int userId) {
        PreparedStatement p = null;
        ResultSet rs = null;
        try {
            p = JForumExecutionContext.getConnection().prepareStatement(SystemGlobals.getSql("UserModel.getUserAuthHash"));
            p.setInt(1, userId);

            rs = p.executeQuery();

            String hash = null;
            if (rs.next()) {
                hash = rs.getString(1);
            }

            return hash;
        } catch (final SQLException e) {
            throw new DatabaseException(e);
        } finally {
            DbUtils.close(rs, p);
        }
    }

    /**
     * @see net.jforum.dao.UserDAO#findByEmail(java.lang.String)
     */
    @Override
    public User findByEmail(final String email) {
        PreparedStatement p = null;
        ResultSet rs = null;

        User u = null;

        try {
            p = JForumExecutionContext.getConnection().prepareStatement(SystemGlobals.getSql("UserModel.findByEmail"));
            p.setString(1, email);
            rs = p.executeQuery();

            if (rs.next()) {
                u = new User();
                fillUserFromResultSet(u, rs);
            }
        } catch (final SQLException e) {
            throw new DatabaseException(e);
        } finally {
            DbUtils.close(rs, p);
        }

        return u;
    }
}
