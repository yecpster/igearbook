package com.igearbook.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

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
        getSession().delete(obj);
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
        return getSession().createQuery("from " + type.getSimpleName()).list();
    }
}
