package com.igearbook.dao;

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

}
