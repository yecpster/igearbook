package com.igearbook.dao;

import java.util.List;

import com.igearbook.entities.PaginationData;
import com.igearbook.entities.PaginationParams;

public class CommonDaoImpl extends BaseDaoImpl<Object> implements CommonDao {
    public CommonDaoImpl() {
        super(Object.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(final Class<T> theClass, final int id) {
        return (T) getSession().get(theClass, id);
    }

    @Override
    public Object get(final int id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Object> list() {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> List<T> list(final Class<T> theClass) {
        return getSession().createCriteria(theClass).list();
    }

    @Override
    public <T> PaginationData<T> doPagination(final Class<T> theClass, final PaginationParams params) {
        return super.doPagination(theClass, params);
    }

}
