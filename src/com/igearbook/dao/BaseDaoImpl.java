package com.igearbook.dao;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map.Entry;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import com.igearbook.entities.PaginationData;
import com.igearbook.entities.PaginationParams;

public abstract class BaseDaoImpl<T> implements BaseDao<T> {
    private final Class<T> type;

    public Class<T> getMyType() {
        return this.type;
    }

    public BaseDaoImpl(final Class<T> type) {
        this.type = type;
    }

    protected SessionFactory sessionFactory;

    public void setSessionFactory(final SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    protected Session getSession() {
        return sessionFactory.getCurrentSession();
    }

    @Override
    @SuppressWarnings("unchecked")
    public T get(final int id) {
        return (T) getSession().get(type, id);
    }

    @Override
    public void delete(final T obj) {
        final Session session = getSession();
        session.delete(obj);
        session.flush();
    }

    @Override
    public void add(final T obj) {
        final Session session = getSession();
        session.save(obj);
        session.flush();
    }

    @Override
    public void update(final T obj) {
        final Session session = getSession();
        session.update(obj);
        session.flush();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<T> list() {
        return getSession().createCriteria(type).list();
    }

    @Override
    public PaginationData<T> doPagination(final PaginationParams params) {
        Criteria criteria = getSession().createCriteria(type);
        for (final Entry<String, Object> entry : params.getQueryParams().entrySet()) {
            criteria = criteria.add(Restrictions.eq(entry.getKey(), entry.getValue()));
        }

        final PaginationData<T> paginationData = new PaginationData<T>();
        final BigDecimal recordsPerPage = BigDecimal.valueOf(params.getRecordsPerPage());
        final BigDecimal totalRecords = getTotalRecords(params);
        final int totalPages = totalRecords.divide(recordsPerPage, RoundingMode.CEILING).intValue();
        final int currentPage = BigDecimal.valueOf(params.getStart() + 1).divide(recordsPerPage, RoundingMode.CEILING).intValue();
        paginationData.setTotalRecords(totalRecords.intValue());
        paginationData.setRecordsPerPage(recordsPerPage.intValue());
        paginationData.setCurrentPage(currentPage);
        paginationData.setTotalPages(totalPages);

        criteria.setFirstResult(params.getStart());
        criteria.setMaxResults(params.getRecordsPerPage());
        @SuppressWarnings("unchecked")
        final List<T> list = criteria.list();
        paginationData.setList(list);
        return paginationData;
    }

    private BigDecimal getTotalRecords(final PaginationParams params) {
        final Criteria criteria = getSession().createCriteria(type);
        final Long count = (Long) criteria.setProjection(Projections.rowCount()).uniqueResult();
        return BigDecimal.valueOf(count);
    }

}
