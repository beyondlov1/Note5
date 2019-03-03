package com.beyond.note5.model;

import java.util.List;

/**
 * @author: beyond
 * @date: 2019/2/6
 */

public interface Model<T> {
    void add(T t);

    void update(T t);

    void delete(T t);

    List<T> findAll();
}
