package com.igearbook.dao;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import com.igearbook.entities.UserApi;

@Repository
public class UserApiDaoImpl extends BaseDaoImpl<UserApi> implements UserApiDao {

    public UserApiDaoImpl() {
        super(UserApi.class);
    }

    @Override
    public UserApi getByOpenId(final String openId) {
        final Criteria criteria = getSession().createCriteria(UserApi.class);
        criteria.add(Restrictions.eq("openId", openId));

        return (UserApi) criteria.uniqueResult();
    }

}
