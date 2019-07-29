package com.beyond.note5.inject;

public interface BeanInjector {
    void inject(Object o, Object... params);

    void inject(Object o, Class[] implementClass, Object... params);
}
