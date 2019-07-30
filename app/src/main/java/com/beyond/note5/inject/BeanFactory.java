package com.beyond.note5.inject;

public interface BeanFactory {

    <T> T getBean(Class<T> tClass);

    /**
     * 内部类
     * @param tClass 内部类
     * @param additionalParams 附加构造可选参数
     * @param <T>
     * @return
     */
    <T> T getBean(Class<T> tClass,  Object... additionalParams);

    <T> T getPrototypeBean(Class<T> tClass, Object... params);

}
