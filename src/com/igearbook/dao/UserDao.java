package com.igearbook.dao;

import net.jforum.entities.User;

import com.igearbook.entities.PaginationData;
import com.igearbook.entities.PaginationParams;

public interface UserDao extends BaseDao<User> {

    public PaginationData<User> listByGroup(PaginationParams params);

}
