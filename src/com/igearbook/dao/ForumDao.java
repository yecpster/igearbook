package com.igearbook.dao;

import java.util.List;

import net.jforum.entities.Forum;

public interface ForumDao extends BaseDao<Forum> {

    public List<Forum> listHostTeam(int limit);
    
    public List<Forum> listRecommendTeam(int limit);
}
