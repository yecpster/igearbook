package com.igearbook.dao;

import java.util.List;
import java.util.Map;

import net.jforum.entities.Forum;
import net.jforum.entities.Post;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import com.google.common.collect.Maps;
import com.igearbook.entities.PaginationParams;

@Repository
public class ForumDaoImpl extends BaseDaoImpl<Forum> implements ForumDao {

    public ForumDaoImpl() {
        super(Forum.class);
    }

    @Override
    public Forum get(final int id) {
        final Forum forum = (Forum) getSession().get(Forum.class, id);
        if(forum!=null){
            forum.setTotalPosts(getTotalPosts(forum));
        }
        return forum;
    }

    private int getTotalPosts(final Forum forum) {
        final PaginationParams params = new PaginationParams();
        final Map<String, Object> queryParams = Maps.newHashMap();
        queryParams.put("forumId", forum.getId());
        params.setQueryParams(queryParams);
        return this.getTotalRecords(Post.class, params).intValue();
    }

    @Override
    public List<Forum> listHostTeam(final int limit) {
        final Criteria criteria = getSession().createCriteria(Forum.class);
        criteria.add(Restrictions.eq("type", Forum.TYPE_TEAM));
        criteria.addOrder(Order.desc("lastPostId"));
        @SuppressWarnings("unchecked")
        final List<Forum> list = criteria.setMaxResults(limit).list();
        return list;
    }

    @Override
    public List<Forum> listRecommendTeam(final int limit) {
        final Criteria criteria = getSession().createCriteria(Forum.class);
        criteria.add(Restrictions.eq("type", Forum.TYPE_TEAM));
        criteria.addOrder(Order.desc("totalTopics"));
        @SuppressWarnings("unchecked")
        final List<Forum> list = criteria.setMaxResults(limit).list();
        return list;
    }
}
