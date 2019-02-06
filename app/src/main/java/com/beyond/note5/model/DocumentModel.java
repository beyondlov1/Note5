package com.beyond.note5.model;

import java.util.List;

/**
 * @author: beyond
 * @date: 2019/2/6
 */

public interface DocumentModel<T> extends Model {

    void add(T document);

    void update(T document);

    void delete(T document);

    List<T> findAll();
}
