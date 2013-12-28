package com.igearbook.dao;

import java.util.List;

import com.igearbook.entities.PaginationData;
import com.igearbook.entities.PaginationParams;

public interface CommonDao {

    public <T> T get(Class<T> theClass, int id);

    public void delete(Object obj);

    public void add(Object obj);

    public void update(Object obj);

    public <T> List<T> list(Class<T> theClass);

    public <T> PaginationData<T> doPagination(Class<T> theClass, PaginationParams params);
}
