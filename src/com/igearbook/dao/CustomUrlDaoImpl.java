package com.igearbook.dao;

import org.springframework.stereotype.Repository;

import com.igearbook.entities.CustomUrl;

@Repository
public class CustomUrlDaoImpl extends BaseDaoImpl<CustomUrl> implements CustomUrlDao {

    public CustomUrlDaoImpl() {
        super(CustomUrl.class);
    }
}
