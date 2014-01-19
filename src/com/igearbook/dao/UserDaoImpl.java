package com.igearbook.dao;

import net.jforum.entities.User;

import org.springframework.stereotype.Repository;

import com.igearbook.entities.PaginationData;
import com.igearbook.entities.PaginationParams;

@Repository
public class UserDaoImpl extends BaseDaoImpl<User> implements UserDao {

    public UserDaoImpl() {
        super(User.class);
    }

    @Override
    public PaginationData<User> listByGroup(final PaginationParams params) {
        final String hql = "select distinct u from User u inner join u.groupsList ug where ug.id = :groupId";
        return this.doPagination(hql, params);
    }

    @Override
    public boolean isIpRegistered(final String ip) {
        final String hql = "select count(*) from User where registerIp = :ip";
        final Long count = (Long) getSession().createQuery(hql).setString("ip", ip).uniqueResult();
        return count > 0;
    }

}
