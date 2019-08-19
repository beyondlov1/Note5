package com.beyond.note5.sync.utils;

/**
 * @author: beyond
 * @date: 2019/8/19
 */

public interface Serializer<S,T> {
    S encode(T t);
    T decode(S s);
}
