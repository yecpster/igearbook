package com.igearbook.dao;

import org.springframework.stereotype.Repository;

import com.igearbook.entities.Recommendation;

@Repository
public class RecommendDaoImpl extends BaseDaoImpl<Recommendation> implements RecommendDao {

    public RecommendDaoImpl() {
        super(Recommendation.class);
    }

}
