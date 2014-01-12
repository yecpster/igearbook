package com.igearbook.dao;

import net.jforum.entities.User;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import com.igearbook.constant.UserAPISource;
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

    @Override
    public UserApi getByUserAndUserAPISource(final User user, final UserAPISource source) {
        final Criteria criteria = getSession().createCriteria(UserApi.class);
        criteria.add(Restrictions.eq("user", user));
        criteria.add(Restrictions.eq("source", source));

        return (UserApi) criteria.uniqueResult();
    }

}
