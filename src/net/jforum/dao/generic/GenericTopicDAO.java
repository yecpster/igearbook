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
 * This file creation date: Apr 6, 2003 / 2:38:28 PM
 * The JForum Project
 * http://www.jforum.net
 */
package net.jforum.dao.generic;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.jforum.JForumExecutionContext;
import net.jforum.SessionFacade;
import net.jforum.dao.DataAccessDriver;
import net.jforum.dao.ForumDAO;
import net.jforum.dao.PollDAO;
import net.jforum.dao.PostDAO;
import net.jforum.dao.TopicDAO;
import net.jforum.entities.KarmaStatus;
import net.jforum.entities.Topic;
import net.jforum.entities.User;
import net.jforum.exceptions.DatabaseException;
import net.jforum.repository.ForumRepository;
import net.jforum.search.SearchArgs;
import net.jforum.search.SearchResult;
import net.jforum.util.DbUtils;
import net.jforum.util.preferences.ConfigKeys;
import net.jforum.util.preferences.SystemGlobals;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * @author Rafael Steil
 * @version $Id: GenericTopicDAO.java,v 1.33 2007/09/12 14:43:15 rafaelsteil Exp $
 */
public class GenericTopicDAO extends AutoKeys implements TopicDAO {
    /**
     * @see net.jforum.dao.TopicDAO#findTopicsByDateRange(net.jforum.search.SearchArgs)
     */
    @Override
    public SearchResult findTopicsByDateRange(final SearchArgs args) {
        SearchResult result = null;

        PreparedStatement p = null;
        ResultSet rs = null;

        try {
            p = JForumExecutionContext.getConnection().prepareStatement(SystemGlobals.getSql("TopicModel.findTopicsByDateRange"));

            p.setTimestamp(1, new Timestamp(args.getFromDate().getTime()));
            p.setTimestamp(2, new Timestamp(args.getToDate().getTime()));

            rs = p.executeQuery();
            final List l = new ArrayList();

            int counter = 0;

            while (rs.next()) {
                if (counter >= args.startFrom() && counter < args.startFrom() + args.fetchCount()) {
                    l.add(new Integer(rs.getInt(1)));
                }

                counter++;
            }

            result = new SearchResult(this.newMessages(l), counter);
        } catch (final Exception e) {
            throw new DatabaseException(e);
        } finally {
            DbUtils.close(rs, p);
        }

        return result;
    }

    /**
     * @see net.jforum.dao.TopicDAO#fixFirstLastPostId(int)
     */
    @Override
    public void fixFirstLastPostId(final int topicId) {
        PreparedStatement p = null;
        ResultSet rs = null;
        try {
            p = JForumExecutionContext.getConnection().prepareStatement(SystemGlobals.getSql("TopicModel.getFirstLastPostId"));
            p.setInt(1, topicId);

            rs = p.executeQuery();
            if (rs.next()) {
                final int first = rs.getInt("first_post_id");
                final int last = rs.getInt("last_post_id");

                rs.close();
                rs = null;
                p.close();
                p = null;

                p = JForumExecutionContext.getConnection().prepareStatement(SystemGlobals.getSql("TopicModel.fixFirstLastPostId"));
                p.setInt(1, first);
                p.setInt(2, last);
                p.setInt(3, topicId);
                p.executeUpdate();
            }
        } catch (final SQLException e) {
            throw new DatabaseException(e);
        } finally {
            DbUtils.close(rs, p);
        }
    }

    /**
     * @see net.jforum.dao.TopicDAO#selectById(int)
     */
    @Override
    public Topic selectById(final int topicId) {
        PreparedStatement p = null;
        try {
            p = JForumExecutionContext.getConnection().prepareStatement(SystemGlobals.getSql("TopicModel.selectById"));
            p.setInt(1, topicId);

            Topic t = new Topic();
            final List l = this.fillTopicsData(p);
            p = null;

            if (l.size() > 0) {
                t = (Topic) l.get(0);
            }

            return t;
        } catch (final SQLException e) {
            throw new DatabaseException(e);
        }
    }

    /**
     * @see net.jforum.dao.TopicDAO#selectRaw(int)
     */
    @Override
    public Topic selectRaw(final int topicId) {
        PreparedStatement p = null;
        ResultSet rs = null;
        try {
            p = JForumExecutionContext.getConnection().prepareStatement(SystemGlobals.getSql("TopicModel.selectRaw"));
            p.setInt(1, topicId);

            Topic t = new Topic();
            rs = p.executeQuery();
            if (rs.next()) {
                t = this.getBaseTopicData(rs);
            }

            return t;
        } catch (final SQLException e) {
            throw new DatabaseException(e);
        } finally {
            DbUtils.close(rs, p);
        }
    }

    /**
     * @see net.jforum.dao.TopicDAO#delete(net.jforum.entities.Topic)
     */
    @Override
    public void delete(final Topic topic, final boolean fromModeration) {
        final List l = new ArrayList();
        l.add(topic);
        this.deleteTopics(l, fromModeration);
    }

    @Override
    public void deleteTopics(final List topics, final boolean fromModeration) {
        // Topic
        PreparedStatement p = null;
        try {
            p = JForumExecutionContext.getConnection().prepareStatement(SystemGlobals.getSql("TopicModel.delete"));

            final ForumDAO forumDao = DataAccessDriver.getInstance().newForumDAO();

            final PostDAO postDao = DataAccessDriver.getInstance().newPostDAO();
            final PollDAO pollDao = DataAccessDriver.getInstance().newPollDAO();

            for (final Iterator iter = topics.iterator(); iter.hasNext();) {
                final Topic topic = (Topic) iter.next();

                // Remove watches
                this.removeSubscriptionByTopic(topic.getId());

                // Remove the messages
                postDao.deleteByTopic(topic.getId());

                // Remove the poll
                pollDao.deleteByTopicId(topic.getId());

                // Delete the topic itself
                p.setInt(1, topic.getId());
                p.executeUpdate();

                if (!fromModeration) {
                    forumDao.decrementTotalTopics(topic.getForumId(), 1);
                }
            }
        } catch (final SQLException e) {
            throw new DatabaseException(e);
        } finally {
            DbUtils.close(p);
        }

    }

    /**
     * @see net.jforum.dao.TopicDAO#deleteByForum(int)
     */
    @Override
    public void deleteByForum(final int forumId) {
        PreparedStatement p = null;
        ResultSet rs = null;

        try {
            p = JForumExecutionContext.getConnection().prepareStatement(SystemGlobals.getSql("TopicModel.deleteByForum"));
            p.setInt(1, forumId);

            rs = p.executeQuery();
            final List topics = new ArrayList();

            while (rs.next()) {
                final Topic t = new Topic();
                t.setId(rs.getInt("topic_id"));
                t.setForumId(forumId);

                topics.add(t);
            }

            this.deleteTopics(topics, false);
        } catch (final SQLException e) {
            throw new DatabaseException(e);
        } finally {
            DbUtils.close(rs, p);
        }
    }

    /**
     * @see net.jforum.dao.TopicDAO#update(net.jforum.entities.Topic)
     */
    @Override
    public void update(final Topic topic) {
        PreparedStatement p = null;
        try {
            p = JForumExecutionContext.getConnection().prepareStatement(SystemGlobals.getSql("TopicModel.update"));

            p.setString(1, topic.getTitle());
            p.setInt(2, topic.getLastPostId());
            p.setInt(3, topic.getFirstPostId());
            p.setInt(4, topic.getType());
            p.setInt(5, topic.isModerated() ? 1 : 0);
            p.setInt(6, topic.getVoteId());
            p.setInt(7, topic.getId());
            p.executeUpdate();
        } catch (final SQLException e) {
            throw new DatabaseException(e);
        } finally {
            DbUtils.close(p);
        }

    }

    /**
     * @see net.jforum.dao.TopicDAO#addNew(net.jforum.entities.Topic)
     */
    @Override
    public int addNew(final Topic topic) {
        PreparedStatement p = null;

        try {
            p = this.getStatementForAutoKeys("TopicModel.addNew");

            p.setInt(1, topic.getForumId());
            p.setString(2, topic.getTitle());
            p.setInt(3, topic.getPostedBy().getId());
            p.setTimestamp(4, new Timestamp(topic.getTime().getTime()));
            p.setInt(5, topic.getFirstPostId());
            p.setInt(6, topic.getLastPostId());
            p.setInt(7, topic.getType());
            p.setInt(8, topic.isModerated() ? 1 : 0);

            this.setAutoGeneratedKeysQuery(SystemGlobals.getSql("TopicModel.lastGeneratedTopicId"));

            final int topicId = this.executeAutoKeysQuery(p);

            topic.setId(topicId);

            return topicId;
        } catch (final SQLException e) {
            throw new DatabaseException(e);
        } finally {
            DbUtils.close(p);
        }
    }

    /**
     * @see net.jforum.dao.TopicDAO#incrementTotalViews(int)
     */
    @Override
    public void incrementTotalViews(final int topicId) {
        PreparedStatement p = null;
        try {
            p = JForumExecutionContext.getConnection().prepareStatement(SystemGlobals.getSql("TopicModel.incrementTotalViews"));
            p.setInt(1, topicId);
            p.executeUpdate();
        } catch (final SQLException e) {
            throw new DatabaseException(e);
        } finally {
            DbUtils.close(p);
        }
    }

    /**
     * @see net.jforum.dao.TopicDAO#incrementTotalReplies(int)
     */
    @Override
    public void incrementTotalReplies(final int topicId) {
        PreparedStatement p = null;
        try {
            p = JForumExecutionContext.getConnection().prepareStatement(SystemGlobals.getSql("TopicModel.incrementTotalReplies"));
            p.setInt(1, topicId);
            p.executeUpdate();
        } catch (final SQLException e) {
            throw new DatabaseException(e);
        } finally {
            DbUtils.close(p);
        }
    }

    /**
     * @see net.jforum.dao.TopicDAO#decrementTotalReplies(int)
     */
    @Override
    public void decrementTotalReplies(final int topicId) {
        PreparedStatement p = null;
        try {
            p = JForumExecutionContext.getConnection().prepareStatement(SystemGlobals.getSql("TopicModel.decrementTotalReplies"));
            p.setInt(1, topicId);
            p.executeUpdate();
        } catch (final SQLException e) {
            throw new DatabaseException(e);
        } finally {
            DbUtils.close(p);
        }
    }

    /**
     * @see net.jforum.dao.TopicDAO#setLastPostId(int, int)
     */
    @Override
    public void setLastPostId(final int topicId, final int postId) {
        PreparedStatement p = null;
        try {
            p = JForumExecutionContext.getConnection().prepareStatement(SystemGlobals.getSql("TopicModel.setLastPostId"));
            p.setInt(1, postId);
            p.setInt(2, topicId);
            p.executeUpdate();
        } catch (final SQLException e) {
            throw new DatabaseException(e);
        } finally {
            DbUtils.close(p);
        }
    }

    /**
     * @see net.jforum.dao.TopicDAO#selectAllByForum(int)
     */
    @Override
    public List<Topic> selectAllByForum(final int forumId) {
        return this.selectAllByForumByLimit(forumId, 0, Integer.MAX_VALUE);
    }

    /**
     * @see net.jforum.dao.TopicDAO#selectAllByForumByLimit(int, int, int)
     */
    @Override
    public List<Topic> selectAllByForumByLimit(final int forumId, final int startFrom, final int count) {
        final String sql = SystemGlobals.getSql("TopicModel.selectAllByForumByLimit");

        PreparedStatement p = null;

        try {
            p = JForumExecutionContext.getConnection().prepareStatement(sql);
            p.setInt(1, forumId);
            p.setInt(2, startFrom);
            p.setInt(3, count);

            return this.fillTopicsData(p);
        } catch (final SQLException e) {
            throw new DatabaseException(e);
        }
    }

    /**
     * @see net.jforum.dao.TopicDAO#selectByUserByLimit(int, int, int)
     */
    @Override
    public List selectByUserByLimit(final int userId, final int startFrom, final int count) {
        PreparedStatement p = null;
        try {
            p = JForumExecutionContext.getConnection().prepareStatement(
                    SystemGlobals.getSql("TopicModel.selectByUserByLimit").replaceAll(":fids:", ForumRepository.getListAllowedForums()));

            p.setInt(1, userId);
            p.setInt(2, startFrom);
            p.setInt(3, count);

            final List list = this.fillTopicsData(p);
            p = null;
            return list;
        } catch (final SQLException e) {
            throw new DatabaseException(e);
        }
    }

    /**
     * @see net.jforum.dao.TopicDAO#countUserTopics(int)
     */
    @Override
    public int countUserTopics(final int userId) {
        int total = 0;

        PreparedStatement p = null;
        ResultSet rs = null;
        try {
            p = JForumExecutionContext.getConnection().prepareStatement(
                    SystemGlobals.getSql("TopicModel.countUserTopics").replaceAll(":fids:", ForumRepository.getListAllowedForums()));
            p.setInt(1, userId);

            rs = p.executeQuery();

            if (rs.next()) {
                total = rs.getInt(1);
            }

            return total;
        } catch (final SQLException e) {
            throw new DatabaseException(e);
        } finally {
            DbUtils.close(rs, p);
        }
    }

    protected Topic getBaseTopicData(final ResultSet rs) throws SQLException {
        final Topic t = new Topic();

        t.setTitle(rs.getString("topic_title"));
        t.setId(rs.getInt("topic_id"));
        t.setTime(new Date(rs.getTimestamp("topic_time").getTime()));
        t.setStatus(rs.getInt("topic_status"));
        t.setTotalViews(rs.getInt("topic_views"));
        t.setTotalReplies(rs.getInt("topic_replies"));
        t.setFirstPostId(rs.getInt("topic_first_post_id"));
        t.setLastPostId(rs.getInt("topic_last_post_id"));
        t.setType(rs.getInt("topic_type"));
        t.setForumId(rs.getInt("forum_id"));
        t.setModerated(rs.getInt("moderated") == 1);
        t.setVoteId(rs.getInt("topic_vote_id"));
        t.setMovedId(rs.getInt("topic_moved_id"));

        final User user = new User();
        user.setId(rs.getInt("user_id"));

        t.setPostedBy(user);

        return t;
    }

    /**
     * @see net.jforum.dao.TopicDAO#autoSetLastPostId(int)
     */
    @Override
    public int getMaxPostId(final int topicId) {
        int id = -1;

        PreparedStatement p = null;
        ResultSet rs = null;
        try {
            p = JForumExecutionContext.getConnection().prepareStatement(SystemGlobals.getSql("TopicModel.getMaxPostId"));
            p.setInt(1, topicId);

            rs = p.executeQuery();
            if (rs.next()) {
                id = rs.getInt("post_id");
            }

            return id;
        } catch (final SQLException e) {
            throw new DatabaseException(e);
        } finally {
            DbUtils.close(rs, p);
        }
    }

    /**
     * @see net.jforum.dao.TopicDAO#getTotalPosts(int)
     */
    @Override
    public int getTotalPosts(final int topicId) {
        int total = 0;

        PreparedStatement p = null;
        ResultSet rs = null;
        try {
            p = JForumExecutionContext.getConnection().prepareStatement(SystemGlobals.getSql("TopicModel.getTotalPosts"));
            p.setInt(1, topicId);

            rs = p.executeQuery();
            if (rs.next()) {
                total = rs.getInt("total");
            }

            return total;
        } catch (final SQLException e) {
            throw new DatabaseException(e);
        } finally {
            DbUtils.close(rs, p);
        }
    }

    /**
     * @see net.jforum.dao.TopicDAO#notifyUsers(net.jforum.entities.Topic)
     */
    @Override
    public List notifyUsers(final Topic topic) {
        final int posterId = SessionFacade.getUserSession().getUserId();
        final int anonUser = SystemGlobals.getIntValue(ConfigKeys.ANONYMOUS_USER_ID);

        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            stmt = JForumExecutionContext.getConnection().prepareStatement(SystemGlobals.getSql("TopicModel.notifyUsers"));

            stmt.setInt(1, topic.getId());
            stmt.setInt(2, posterId); // don't notify the poster
            stmt.setInt(3, anonUser); // don't notify the anonimous user

            rs = stmt.executeQuery();

            final List users = new ArrayList();

            while (rs.next()) {
                final User user = new User();

                user.setId(rs.getInt("user_id"));
                user.setEmail(rs.getString("user_email"));
                user.setUsername(rs.getString("username"));
                user.setLang(rs.getString("user_lang"));
                user.setNotifyText(rs.getInt("user_notify_text") == 1);

                users.add(user);
            }

            rs.close();
            stmt.close();

            // Set read status to false
            stmt = JForumExecutionContext.getConnection().prepareStatement(SystemGlobals.getSql("TopicModel.markAllAsUnread"));
            stmt.setInt(1, topic.getId());
            stmt.setInt(2, posterId); // don't notify the poster
            stmt.setInt(3, anonUser); // don't notify the anonimous user

            stmt.executeUpdate();

            return users;
        } catch (final SQLException e) {
            throw new DatabaseException(e);
        } finally {
            DbUtils.close(rs, stmt);
        }
    }

    /**
     * @see net.jforum.dao.TopicDAO#subscribeUsers(int, java.util.List)
     */
    @Override
    public void subscribeUsers(final int topicId, final List users) {
        PreparedStatement p = null;

        try {
            p = JForumExecutionContext.getConnection().prepareStatement(SystemGlobals.getSql("TopicModel.subscribeUser"));

            p.setInt(1, topicId);

            for (final Iterator iter = users.iterator(); iter.hasNext();) {
                final int userId = ((User) iter.next()).getId();

                p.setInt(2, userId);
                p.executeUpdate();
            }
        } catch (final SQLException e) {
            throw new DatabaseException(e);
        } finally {
            DbUtils.close(p);
        }
    }

    /**
     * @see net.jforum.dao.TopicDAO#subscribeUser(int, int)
     */
    @Override
    public void subscribeUser(final int topicId, final int userId) {
        final User user = new User();
        user.setId(userId);

        final List l = new ArrayList();
        l.add(user);

        this.subscribeUsers(topicId, l);
    }

    /**
     * @see net.jforum.dao.TopicDAO#isUserSubscribing(int, int)
     */
    @Override
    public boolean isUserSubscribed(final int topicId, final int userId) {
        PreparedStatement p = null;
        ResultSet rs = null;

        try {
            p = JForumExecutionContext.getConnection().prepareStatement(SystemGlobals.getSql("TopicModel.isUserSubscribed"));

            p.setInt(1, topicId);
            p.setInt(2, userId);

            rs = p.executeQuery();

            return rs.next() && rs.getInt(1) > 0;
        } catch (final SQLException e) {
            throw new DatabaseException(e);
        } finally {
            DbUtils.close(rs, p);
        }
    }

    /**
     * @see net.jforum.dao.TopicDAO#removeSubscription(int, int)
     */
    @Override
    public void removeSubscription(final int topicId, final int userId) {
        PreparedStatement p = null;
        try {
            p = JForumExecutionContext.getConnection().prepareStatement(SystemGlobals.getSql("TopicModel.removeSubscription"));
            p.setInt(1, topicId);
            p.setInt(2, userId);

            p.executeUpdate();
        } catch (final SQLException e) {
            throw new DatabaseException(e);
        } finally {
            DbUtils.close(p);
        }
    }

    /**
     * @see net.jforum.dao.TopicDAO#removeSubscriptionByTopic(int)
     */
    @Override
    public void removeSubscriptionByTopic(final int topicId) {
        PreparedStatement p = null;
        try {
            p = JForumExecutionContext.getConnection().prepareStatement(SystemGlobals.getSql("TopicModel.removeSubscriptionByTopic"));
            p.setInt(1, topicId);

            p.executeUpdate();
        } catch (final SQLException e) {
            throw new DatabaseException(e);
        } finally {
            DbUtils.close(p);
        }
    }

    /**
     * @see net.jforum.dao.TopicDAO#updateReadStatus(int, int, boolean)
     */
    @Override
    public void updateReadStatus(final int topicId, final int userId, final boolean read) {
        if (this.isUserSubscribed(topicId, userId)) {
            PreparedStatement p = null;
            try {
                p = JForumExecutionContext.getConnection().prepareStatement(SystemGlobals.getSql("TopicModel.updateReadStatus"));
                p.setInt(1, read ? 1 : 0);
                p.setInt(2, topicId);
                p.setInt(3, userId);

                p.executeUpdate();
            } catch (final SQLException e) {
                throw new DatabaseException(e);
            } finally {
                DbUtils.close(p);
            }
        }
    }

    /**
     * @see net.jforum.dao.TopicDAO#lockUnlock(int[], int)
     */
    @Override
    public void lockUnlock(final int[] topicId, final int status) {
        PreparedStatement p = null;
        try {
            p = JForumExecutionContext.getConnection().prepareStatement(SystemGlobals.getSql("TopicModel.lockUnlock"));
            p.setInt(1, status);

            for (int i = 0; i < topicId.length; i++) {
                p.setInt(2, topicId[i]);
                p.executeUpdate();
            }
        } catch (final SQLException e) {
            throw new DatabaseException(e);
        } finally {
            DbUtils.close(p);
        }
    }

    private List newMessages(final List topicIds) {
        if (topicIds.size() == 0) {
            return new ArrayList();
        }

        PreparedStatement p = null;

        try {
            String sql = SystemGlobals.getSql("TopicModel.selectForNewMessages");

            final StringBuffer sb = new StringBuffer();

            for (final Iterator iter = topicIds.iterator(); iter.hasNext();) {
                sb.append(iter.next()).append(',');
            }

            sb.append("-1");

            sql = sql.replaceAll(":topicIds:", sb.toString());

            p = JForumExecutionContext.getConnection().prepareStatement(sql);

            return this.fillTopicsData(p);
        } catch (final SQLException e) {
            throw new DatabaseException(e);
        }
    }

    /**
     * Fills all topic data. The method will try to get all fields from the topics table, as well information about the user who made the first and
     * the last post in the topic. <br>
     * <b>The method <i>will</i> close the <i>PreparedStatement</i></b>
     * 
     * @param p
     *            the PreparedStatement to execute
     * @return A list with all topics found
     * @throws SQLException
     */
    public List<Topic> fillTopicsData(PreparedStatement p) {
        final List<Topic> l = Lists.newArrayList();
        ResultSet rs = null;

        try {
            rs = p.executeQuery();

            final SimpleDateFormat df = new SimpleDateFormat(SystemGlobals.getValue(ConfigKeys.DATE_TIME_FORMAT));

            final StringBuffer sbFirst = new StringBuffer(128);
            final StringBuffer sbLast = new StringBuffer(128);

            while (rs.next()) {
                final Topic t = this.getBaseTopicData(rs);

                // Posted by
                User u = new User();
                u.setId(rs.getInt("user_id"));
                t.setPostedBy(u);

                // Last post by
                u = new User();
                u.setId(rs.getInt("last_user_id"));
                t.setLastPostBy(u);

                t.setHasAttach(rs.getInt("attach") > 0);
                t.setFirstPostTime(df.format(rs.getTimestamp("topic_time")));
                t.setLastPostTime(df.format(rs.getTimestamp("post_time")));
                t.setLastPostDate(new Date(rs.getTimestamp("post_time").getTime()));

                l.add(t);

                sbFirst.append(rs.getInt("user_id")).append(',');
                sbLast.append(rs.getInt("last_user_id")).append(',');
            }

            rs.close();
            rs = null;
            p.close();
            p = null;

            // Users
            if (sbFirst.length() > 0) {
                sbLast.delete(sbLast.length() - 1, sbLast.length());

                String sql = SystemGlobals.getSql("TopicModel.getUserInformation");
                sql = sql.replaceAll("#ID#", sbFirst.toString() + sbLast.toString());

                final Map users = new HashMap();

                p = JForumExecutionContext.getConnection().prepareStatement(sql);
                rs = p.executeQuery();

                while (rs.next()) {
                    users.put(new Integer(rs.getInt("user_id")), rs.getString("username"));
                }

                rs.close();
                rs = null;
                p.close();
                p = null;

                for (final Iterator iter = l.iterator(); iter.hasNext();) {
                    final Topic t = (Topic) iter.next();
                    t.getPostedBy().setUsername((String) users.get(new Integer(t.getPostedBy().getId())));
                    t.getLastPostBy().setUsername((String) users.get(new Integer(t.getLastPostBy().getId())));
                }
            }

            return l;
        } catch (final SQLException e) {
            throw new DatabaseException(e);
        } finally {
            DbUtils.close(rs, p);
        }
    }

    /**
     * @see net.jforum.dao.TopicDAO#selectRecentTopics(int)
     */
    @Override
    public List<Topic>  selectRecentTopics(final int limit) {
        PreparedStatement p = null;
        try {
            p = JForumExecutionContext.getConnection().prepareStatement(SystemGlobals.getSql("TopicModel.selectRecentTopicsByLimit"));
            p.setInt(1, limit);

            final List<Topic>  list = this.fillTopicsData(p);
            return list;
        } catch (final SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public List<Topic> selectRecentReplied(final int limit) {
        PreparedStatement p = null;
        try {
            p = JForumExecutionContext.getConnection().prepareStatement(SystemGlobals.getSql("TopicModel.selectRecentRepliedTopicsByLimit"));
            p.setInt(1, limit);

            final List<Topic>  list = this.fillTopicsData(p);
            return list;
        } catch (final SQLException e) {
            throw new DatabaseException(e);
        }
    }

    /**
     * @see net.jforum.dao.TopicDAO#selectHottestTopics(int)
     */
    @Override
    public List selectHottestTopics(final int limit) {
        PreparedStatement p = null;
        try {
            p = JForumExecutionContext.getConnection().prepareStatement(SystemGlobals.getSql("TopicModel.selectHottestTopicsByLimit"));
            p.setInt(1, limit);

            final List list = this.fillTopicsData(p);
            p = null;
            return list;
        } catch (final SQLException e) {
            throw new DatabaseException(e);
        }
    }

    /**
     * @see net.jforum.dao.TopicDAO#setFirstPostId(int, int)
     */
    @Override
    public void setFirstPostId(final int topicId, final int postId) {
        PreparedStatement p = null;
        try {
            p = JForumExecutionContext.getConnection().prepareStatement(SystemGlobals.getSql("TopicModel.setFirstPostId"));
            p.setInt(1, postId);
            p.setInt(2, topicId);
            p.executeUpdate();
        } catch (final SQLException e) {
            throw new DatabaseException(e);
        } finally {
            DbUtils.close(p);
        }
    }

    /**
     * @see net.jforum.dao.TopicDAO#getMinPostId(int)
     */
    @Override
    public int getMinPostId(final int topicId) {
        int id = -1;

        PreparedStatement p = null;
        try {
            p = JForumExecutionContext.getConnection().prepareStatement(SystemGlobals.getSql("TopicModel.getMinPostId"));
            p.setInt(1, topicId);

            final ResultSet rs = p.executeQuery();
            if (rs.next()) {
                id = rs.getInt("post_id");
            }

            return id;
        } catch (final SQLException e) {
            throw new DatabaseException(e);
        } finally {
            DbUtils.close(p);
        }
    }

    /**
     * @see net.jforum.dao.TopicDAO#setModerationStatus(int, boolean)
     */
    @Override
    public void setModerationStatus(final int forumId, final boolean status) {
        PreparedStatement p = null;
        try {
            p = JForumExecutionContext.getConnection().prepareStatement(SystemGlobals.getSql("TopicModel.setModerationStatus"));
            p.setInt(1, status ? 1 : 0);
            p.setInt(2, forumId);
            p.executeUpdate();
        } catch (final SQLException e) {
            throw new DatabaseException(e);
        } finally {
            DbUtils.close(p);
        }
    }

    /**
     * @see net.jforum.dao.TopicDAO#setModerationStatusByTopic(int, boolean)
     */
    @Override
    public void setModerationStatusByTopic(final int topicId, final boolean status) {
        PreparedStatement p = null;
        try {
            p = JForumExecutionContext.getConnection().prepareStatement(SystemGlobals.getSql("TopicModel.setModerationStatusByTopic"));
            p.setInt(1, status ? 1 : 0);
            p.setInt(2, topicId);
            p.executeUpdate();
        } catch (final SQLException e) {
            throw new DatabaseException(e);
        } finally {
            DbUtils.close(p);
        }
    }

    /**
     * @see net.jforum.dao.TopicDAO#selectTopicTitlesByIds(java.util.Collection)
     */
    @Override
    public List selectTopicTitlesByIds(final Collection idList) {
        final List l = new ArrayList();
        String sql = SystemGlobals.getSql("TopicModel.selectTopicTitlesByIds");

        final StringBuffer sb = new StringBuffer(idList.size() * 2);
        for (final Iterator iter = idList.iterator(); iter.hasNext();) {
            sb.append(iter.next()).append(",");
        }

        final int len = sb.length();
        sql = sql.replaceAll(":ids:", len > 0 ? sb.toString().substring(0, len - 1) : "0");
        PreparedStatement p = null;
        ResultSet rs = null;
        try {
            p = JForumExecutionContext.getConnection().prepareStatement(sql);

            rs = p.executeQuery();
            while (rs.next()) {
                final Map m = new HashMap();
                m.put("id", new Integer(rs.getInt("topic_id")));
                m.put("title", rs.getString("topic_title"));

                l.add(m);
            }
            return l;
        } catch (final SQLException e) {
            throw new DatabaseException(e);
        } finally {
            DbUtils.close(rs, p);
        }
    }

    /**
     * @see net.jforum.model.UserModel#topicPosters(int)
     */
    @Override
    public Map<Integer, User> topicPosters(final int topicId) {
        final Map<Integer, User> m = Maps.newHashMap();

        PreparedStatement p = null;
        ResultSet rs = null;

        try {
            final StringBuffer sql = new StringBuffer(SystemGlobals.getSql("TopicModel.topicPosters"));

            p = JForumExecutionContext.getConnection().prepareStatement(SystemGlobals.getSql("TopicModel.distinctPosters"));
            p.setInt(1, topicId);

            rs = p.executeQuery();

            final StringBuffer sb = new StringBuffer();

            while (rs.next()) {
                sb.append(rs.getInt("user_id")).append(',');
            }

            rs.close();
            p.close();

            final int index = sql.indexOf(":ids:");
            if (index > -1) {
                sql.replace(index, index + 5, sb.substring(0, sb.length() - 1));
            }

            p = JForumExecutionContext.getConnection().prepareStatement(sql.toString());
            rs = p.executeQuery();

            while (rs.next()) {
                final User u = new User();

                u.setId(rs.getInt("user_id"));
                u.setUsername(rs.getString("username"));
                u.setKarma(new KarmaStatus(u.getId(), rs.getDouble("user_karma")));
                u.setAvatar(rs.getString("user_avatar"));
                u.setAvatarEnabled(rs.getInt("user_allowavatar") == 1);
                u.setRegistrationDate(new Date(rs.getTimestamp("user_regdate").getTime()));
                u.setTotalPosts(rs.getInt("user_posts"));
                u.setFrom(rs.getString("user_from"));
                u.setEmail(rs.getString("user_email"));
                u.setRankId(rs.getInt("rank_id"));
                u.setViewEmailEnabled(rs.getInt("user_viewemail") == 1);
                u.setIcq(rs.getString("user_icq"));
                u.setAttachSignatureEnabled(rs.getInt("user_attachsig") == 1);
                u.setMsnm(rs.getString("user_msnm"));
                u.setYim(rs.getString("user_yim"));
                u.setWebSite(rs.getString("user_website"));
                u.setAim(rs.getString("user_aim"));
                u.setSignature(rs.getString("user_sig"));

                m.put(new Integer(u.getId()), u);
            }

            return m;
        } catch (final SQLException e) {
            throw new DatabaseException(e);
        } finally {
            DbUtils.close(rs, p);
        }
    }

    @Override
    public List<Topic> selectByForumByTypeByLimit(final int forumId, final int type, final int startFrom, final int count) {
        final String sql = SystemGlobals.getSql("TopicModel.selectByForumByTypeByLimit");

        PreparedStatement p = null;

        try {
            p = JForumExecutionContext.getConnection().prepareStatement(sql);
            p.setInt(1, forumId);
            p.setInt(2, type);
            p.setInt(3, startFrom);
            p.setInt(4, count);

            return this.fillTopicsData(p);
        } catch (final SQLException e) {
            throw new DatabaseException(e);
        }
    }

}
