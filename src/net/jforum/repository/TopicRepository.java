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
 * This file creation date: 05/04/2004 - 20:11:44
 * The JForum Project
 * http://www.jforum.net
 */
package net.jforum.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.jforum.cache.CacheEngine;
import net.jforum.cache.Cacheable;
import net.jforum.dao.DataAccessDriver;
import net.jforum.dao.TopicDAO;
import net.jforum.entities.Topic;
import net.jforum.entities.TopicTypeComparator;
import net.jforum.util.preferences.ConfigKeys;
import net.jforum.util.preferences.SystemGlobals;

import com.google.common.collect.Lists;

/**
 * Repository for the last n topics for each forum.
 * 
 * @author Rafael Steil
 * @author James Yong
 * @version $Id: TopicRepository.java,v 1.33 2007/09/05 04:00:27 rafaelsteil Exp $
 */
public class TopicRepository implements Cacheable {
    private static int maxItems = SystemGlobals.getIntValue(ConfigKeys.TOPICS_PER_PAGE);

    private static final String FQN = "topics";
    private static final String RECENT = "recent";
    private static final String RECENT_REPLIED = "recentReplied";
    private static final String HOTTEST = "hottest";
    private static final String FQN_FORUM = FQN + "/byforum";
    private static final String RELATION = "relation";
    private static final String FQN_LOADED = FQN + "/loaded";
    private static final Comparator TYPE_COMPARATOR = new TopicTypeComparator();

    private static CacheEngine cache;

    /**
     * @see net.jforum.cache.Cacheable#setCacheEngine(net.jforum.cache.CacheEngine)
     */
    @Override
    public void setCacheEngine(final CacheEngine engine) {
        cache = engine;
    }

    public static boolean isLoaded(final int forumId) {
        return "1".equals(cache.get(FQN_LOADED, Integer.toString(forumId)));
    }

    /**
     * Add topic to the FIFO stack
     * 
     * @param topic
     *            The topic to add to stack
     */
    public synchronized static void pushTopic(final Topic topic) {
        if (SystemGlobals.getBoolValue(ConfigKeys.TOPIC_CACHE_ENABLED)) {
            final boolean hasReply = topic.getTotalReplies() > 0;
            final String limitKey = hasReply ? ConfigKeys.HOTTEST_TOPICS : ConfigKeys.RECENT_TOPICS;
            final int limit = SystemGlobals.getIntValue(limitKey);

            final String recentType = hasReply ? RECENT_REPLIED : RECENT;
            final List<Topic> topics = hasReply ? getRecentRepliedTopics() : getRecentTopics();
            final LinkedList<Topic> topicsLinkedList = Lists.newLinkedList(topics);
            topicsLinkedList.remove(topic);
            topicsLinkedList.addFirst(topic);

            while (topicsLinkedList.size() > limit) {
                topicsLinkedList.removeLast();
            }

            cache.add(FQN, recentType, topicsLinkedList);
        }
    }

    /**
     * Get all cached recent topics.
     * 
     */
    public static List<Topic> getRecentTopics() {
        List l = (List) cache.get(FQN, RECENT);

        if (l == null || l.size() == 0 || !SystemGlobals.getBoolValue(ConfigKeys.TOPIC_CACHE_ENABLED)) {
            l = loadMostRecentTopics();
        }

        return new ArrayList<Topic>(l);
    }

    /**
     * Get all cached recent replied topics.
     * 
     */
    public static List<Topic> getRecentRepliedTopics() {
        @SuppressWarnings("unchecked")
        List<Topic> l = (List<Topic>) cache.get(FQN, RECENT_REPLIED);

        if (l == null || l.size() == 0 || !SystemGlobals.getBoolValue(ConfigKeys.TOPIC_CACHE_ENABLED)) {
            l = loadMostRecentRepliedTopics();
        }

        return new ArrayList<Topic>(l);
    }

    /**
     * Get all cached hottest topics.
     * 
     */
    public static List getHottestTopics() {
        List l = (List) cache.get(FQN, HOTTEST);

        if (l == null || l.size() == 0 || !SystemGlobals.getBoolValue(ConfigKeys.TOPIC_CACHE_ENABLED)) {
            l = loadHottestTopics();
        }

        return new ArrayList(l);
    }

    /**
     * Add recent topics to the cache
     */
    public synchronized static LinkedList<Topic> loadMostRecentTopics() {
        final TopicDAO tm = DataAccessDriver.getInstance().newTopicDAO();
        final int limit = SystemGlobals.getIntValue(ConfigKeys.RECENT_TOPICS);

        final LinkedList<Topic> l = new LinkedList<Topic>(tm.selectRecentTopics(limit));
        cache.add(FQN, RECENT, l);

        return l;
    }

    /**
     * Add recent replied topics to the cache
     */
    public synchronized static LinkedList<Topic> loadMostRecentRepliedTopics() {
        final TopicDAO tm = DataAccessDriver.getInstance().newTopicDAO();
        final int limit = SystemGlobals.getIntValue(ConfigKeys.HOTTEST_TOPICS);

        final LinkedList<Topic> l = new LinkedList<Topic>(tm.selectRecentReplied(limit));
        cache.add(FQN, RECENT_REPLIED, l);

        return l;
    }

    /**
     * Add hottest topics to the cache
     */
    public synchronized static List loadHottestTopics() {
        final TopicDAO tm = DataAccessDriver.getInstance().newTopicDAO();
        final int limit = SystemGlobals.getIntValue(ConfigKeys.HOTTEST_TOPICS);

        final List l = tm.selectHottestTopics(limit);
        cache.add(FQN, HOTTEST, new LinkedList(l));

        return l;
    }

    /**
     * Add topics to the cache
     * 
     * @param forumId
     *            The forum id to which the topics are related
     * @param topics
     *            The topics to add
     */
    public static void addAll(final int forumId, final List topics) {
        if (SystemGlobals.getBoolValue(ConfigKeys.TOPIC_CACHE_ENABLED)) {
            synchronized (FQN_FORUM) {
                cache.add(FQN_FORUM, Integer.toString(forumId), new LinkedList(topics));

                Map m = (Map) cache.get(FQN, RELATION);

                if (m == null) {
                    m = new HashMap();
                }

                final Integer fId = new Integer(forumId);

                for (final Iterator iter = topics.iterator(); iter.hasNext();) {
                    final Topic t = (Topic) iter.next();

                    m.put(new Integer(t.getId()), fId);
                }

                cache.add(FQN, RELATION, m);
                cache.add(FQN_LOADED, Integer.toString(forumId), "1");
            }
        }
    }

    /**
     * Clears the cache
     * 
     * @param forumId
     *            The forum id to clear the cache
     */
    public static void clearCache(final int forumId) {
        synchronized (FQN_FORUM) {
            cache.add(FQN_FORUM, Integer.toString(forumId), new LinkedList());
            cache.remove(FQN, RELATION);
        }
    }

    /**
     * Adds a new topic to the cache
     * 
     * @param topic
     *            The topic to add
     */
    public static void addTopic(final Topic topic) {
        if (!SystemGlobals.getBoolValue(ConfigKeys.TOPIC_CACHE_ENABLED)) {
            return;
        }

        synchronized (FQN_FORUM) {
            final String forumId = Integer.toString(topic.getForumId());
            LinkedList list = (LinkedList) cache.get(FQN_FORUM, forumId);

            if (list == null) {
                list = new LinkedList();
                list.add(topic);
            } else {
                final boolean contains = list.contains(topic);

                // If the cache is full, remove the eldest element
                if (!contains && list.size() + 1 > maxItems) {
                    list.removeLast();
                } else if (contains) {
                    list.remove(topic);
                }

                list.add(topic);

                Collections.sort(list, TYPE_COMPARATOR);
            }

            cache.add(FQN_FORUM, forumId, list);

            Map m = (Map) cache.get(FQN, RELATION);

            if (m == null) {
                m = new HashMap();
            }

            m.put(new Integer(topic.getId()), new Integer(forumId));

            cache.add(FQN, RELATION, m);
        }
    }

    /**
     * Updates a cached topic
     * 
     * @param topic
     *            The topic to update
     */
    public static void updateTopic(final Topic topic) {
        if (SystemGlobals.getBoolValue(ConfigKeys.TOPIC_CACHE_ENABLED)) {
            synchronized (FQN_FORUM) {
                final String forumId = Integer.toString(topic.getForumId());
                final List l = (List) cache.get(FQN_FORUM, forumId);

                if (l != null) {
                    final int index = l.indexOf(topic);

                    if (index > -1) {
                        l.set(index, topic);
                        cache.add(FQN_FORUM, forumId, l);
                    }
                }
            }
        }
    }

    /**
     * Gets a cached topic.
     * 
     * @param t
     *            The topic to try to get from the cache. The instance passed as argument should have ae least the topicId and forumId set
     * @return The topic instance, if found, or <code>null</code> otherwise.
     */
    public static Topic getTopic(final Topic t) {
        if (!SystemGlobals.getBoolValue(ConfigKeys.TOPIC_CACHE_ENABLED)) {
            return null;
        }

        if (t.getForumId() == 0) {
            final Map m = (Map) cache.get(FQN, RELATION);

            if (m != null) {
                final Integer forumId = (Integer) m.get(new Integer(t.getId()));

                if (forumId != null) {
                    t.setForumId(forumId.intValue());
                }
            }

            if (t.getForumId() == 0) {
                return null;
            }
        }

        final List l = (List) cache.get(FQN_FORUM, Integer.toString(t.getForumId()));

        int index = -1;

        if (l != null) {
            index = l.indexOf(t);
        }

        return (index == -1 ? null : (Topic) l.get(index));
    }

    /**
     * Checks if a topic is cached
     * 
     * @param topic
     *            The topic to verify
     * @return <code>true</code> if the topic is cached, or <code>false</code> if not.
     */
    public static boolean isTopicCached(final Topic topic) {
        if (!SystemGlobals.getBoolValue(ConfigKeys.TOPIC_CACHE_ENABLED)) {
            return false;
        }

        final String forumId = Integer.toString(topic.getForumId());
        final List list = (List) cache.get(FQN_FORUM, forumId);

        return list == null ? false : list.contains(topic);
    }

    /**
     * Get all cached topics related to a forum.
     * 
     * @param forumid
     *            The forum id
     * @return <code>ArrayList</code> with the topics.
     */
    public static List<Topic> getTopics(final int forumid) {
        if (SystemGlobals.getBoolValue(ConfigKeys.TOPIC_CACHE_ENABLED)) {
            synchronized (FQN_FORUM) {
                final List<Topic> returnList = (List) cache.get(FQN_FORUM, Integer.toString(forumid));

                if (returnList == null) {
                    return new ArrayList();
                }

                return new ArrayList(returnList);
            }
        }

        return new ArrayList();
    }
}
