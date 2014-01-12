package com.igearbook.dao;

import net.jforum.entities.User;

import com.igearbook.constant.UserAPISource;
import com.igearbook.entities.UserApi;

public interface UserApiDao extends BaseDao<UserApi> {

    public UserApi getByOpenId(String openId);

    public UserApi getByUserAndUserAPISource(User user, UserAPISource source);
}
