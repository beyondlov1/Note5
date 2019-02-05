package com.beyond.note5.view.convert;

/**
 * Created by beyond on 2019/2/3.
 */

public interface Converter<T,S> {
    S convert(T t);
}
