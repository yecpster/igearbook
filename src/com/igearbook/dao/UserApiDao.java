package com.igearbook.dao;

import com.igearbook.entities.UserApi;

public interface UserApiDao extends BaseDao<UserApi> {

    public UserApi getByOpenId(String openId);
}
