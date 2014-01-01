package com.igearbook.dao;

import java.util.List;

import net.jforum.entities.Topic;

public interface TopicDao extends BaseDao<Topic> {

    public List<Topic> listRecentTopics(int forumType, int limit);
}
