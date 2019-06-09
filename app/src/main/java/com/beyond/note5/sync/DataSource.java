package com.beyond.note5.sync;

import java.io.IOException;
import java.util.List;

public interface DataSource<T> {
    void add(T t);
    void delete(T t);
    void update(T t);
    T select(T t);
    List<T> selectAll() throws IOException;
    void cover(List<T> all) throws IOException;
    Class clazz();
}
