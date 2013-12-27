package com.igearbook.dao;

import java.util.List;

public interface BaseDao<T> {
        public T get(int id);
        
        public void delete(T obj);

        public void add(T obj);

        public void update(T obj);

        public List<T> list();

}
