package com.beyond.note5.inject;

public interface BeanFactory {

    <T> T getBean(Class<T> tClass);

    <T> T getPrototypeBean(Class<T> tClass, Object... params);

}
