package com.igearbook.dao;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import com.igearbook.entities.Recommendation;

@Repository
public class RecommendDaoImpl extends BaseDaoImpl<Recommendation> implements RecommendDao {

    public RecommendDaoImpl() {
        super(Recommendation.class);
    }

    @Override
    public Recommendation getByTopicId(final int id) {
        return (Recommendation) getSession().createCriteria(Recommendation.class).add(Restrictions.eq("topicId", id)).uniqueResult();
    }

    @Override
    public List<Recommendation> listByTypeByLimit(final int type, final int limit) {
        final Criteria criteria = getSession().createCriteria(Recommendation.class);
        criteria.add(Restrictions.eq("type", type));
        criteria.addOrder(Order.desc("id"));
        criteria.setMaxResults(limit);
        @SuppressWarnings("unchecked")
        final List<Recommendation> list = criteria.list();
        return list;
    }

}
