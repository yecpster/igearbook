package com.igearbook.dao;

import java.util.List;

import net.jforum.entities.Topic;

import org.hibernate.Query;
import org.springframework.stereotype.Repository;

@Repository
public class TopicDaoImpl extends BaseDaoImpl<Topic> implements TopicDao {

    public TopicDaoImpl() {
        super(Topic.class);
    }

    @Override
    public List<Topic> listRecentTopics(final int forumType, final int limit) {
        final String hql = "select t from Topic t, Forum f where t.type!=:topicType and t.forumId=f.id and f.type=:forumType order by t.id desc";
        final Query query = getSession().createQuery(hql);
        query.setParameter("topicType", Topic.TYPE_ANNOUNCE);
        query.setParameter("forumType", forumType);
        query.setMaxResults(limit);
        @SuppressWarnings("unchecked")
        final List<Topic> list = query.list();
        return list;
    }
}
