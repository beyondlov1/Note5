package com.beyond.note5.utils.converter;

/**
 * @author: beyond
 * @date: 2019/2/3
 */

public interface Converter<T,S> {
    S convert(T t);
}
