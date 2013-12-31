package com.igearbook.dao;

import java.util.List;

import com.igearbook.entities.Recommendation;

public interface RecommendDao extends BaseDao<Recommendation> {

    public List<Recommendation> listByTypeByLimit(int type, int limit);

    public Recommendation getByTopicId(int id);

}
