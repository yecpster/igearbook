package com.igearbook.dao;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import com.igearbook.constant.UrlType;
import com.igearbook.entities.CustomUrl;

@Repository
public class CustomUrlDaoImpl extends BaseDaoImpl<CustomUrl> implements CustomUrlDao {

    public CustomUrlDaoImpl() {
        super(CustomUrl.class);
    }

    @Override
    public CustomUrl getByTypeModuleId(final UrlType type, final int moduleId) {
        final Criteria criteria=this.getSession().createCriteria(CustomUrl.class);
        criteria.add(Restrictions.eq("type", type));
        criteria.add(Restrictions.eq("moduleId", moduleId));
        return (CustomUrl) criteria.uniqueResult();
    }

    @Override
    public CustomUrl getByUrl(final String url) {
        final Criteria criteria=this.getSession().createCriteria(CustomUrl.class);
        criteria.add(Restrictions.eq("url", url));
        return (CustomUrl) criteria.uniqueResult();
    }
}
