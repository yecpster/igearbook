package com.igearbook.dao;

import com.igearbook.entities.Recommendation;

public interface RecommendDao extends BaseDao<Recommendation> {

    public Recommendation getByTopicId(int id);

}
