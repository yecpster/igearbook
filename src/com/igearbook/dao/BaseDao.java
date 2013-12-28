package com.igearbook.dao;

import java.util.List;

import com.igearbook.entities.PaginationData;
import com.igearbook.entities.PaginationParams;

public interface BaseDao<T> {
    public T get(int id);

    public void delete(T obj);

    public void add(T obj);

    public void update(T obj);

    public List<T> list();

    public PaginationData<T> doPagination(PaginationParams params);
}
