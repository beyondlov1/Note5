package com.beyond.note5.model;

import com.beyond.note5.bean.Document;

import java.util.Date;
import java.util.List;

/**
 * @author: beyond
 * @date: 2019/2/6
 */

public interface DocumentModel<T extends Document> extends Model<T> {

    void add(T document);

    void update(T document);

    void deleteLogic(T document);

    void delete(T document);

    List<T> findAll();

    List<T> findAllInAll();

    T findById(String id);

    List<T> findByModifiedDate(Date date);
}
