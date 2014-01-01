package com.igearbook.dao;

import net.jforum.entities.Post;

import org.springframework.stereotype.Repository;

@Repository
public class PostDaoImpl extends BaseDaoImpl<Post> implements PostDao {

    public PostDaoImpl() {
        super(Post.class);
    }

}
