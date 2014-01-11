package com.igearbook.dao;

import java.util.List;

import com.igearbook.entities.PaginationData;
import com.igearbook.entities.PaginationParams;
import com.igearbook.entities.Recommendation;

public interface RecommendDao extends BaseDao<Recommendation> {

    public List<Recommendation> listByTypeByLimit(int type, int limit);

    public PaginationData<Recommendation> doPaginationByType(final int type, final PaginationParams params);

    public Recommendation getByTopicId(int id);

}
