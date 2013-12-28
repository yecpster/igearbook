package com.igearbook.dao;

import java.util.List;

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
        @SuppressWarnings("unchecked")
        final List<Recommendation> list = getSession().createCriteria(Recommendation.class).add(Restrictions.eq("topicId", id)).list();
        return list.size() > 0 ? list.get(0) : null;
    }

}
