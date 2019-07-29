package com.beyond.note5.inject;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

public class BeanInjectorImpl implements BeanInjector {

    private InjectContext context;

    private BeanFactoryImpl factory;

    public BeanInjectorImpl(InjectContext context) {
        this.context = context;
        this.factory = new BeanFactoryImpl(context);
    }

    @Override
    public void inject(Object o, Object... params) {
        Class<?> aClass = o.getClass();
        Field[] declaredFields = aClass.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(SingletonInject.class)) {
                injectSingleton(o, factory, field, new Class[0]);
            }

            if (field.isAnnotationPresent(PrototypeInject.class) && params.length>0) {
                injectPrototype(o, factory, field, params, new Class[0]);
            }
        }
    }

    @Override
    public void inject(Object o, Class[] implementClass, Object... params) {
        Class<?> aClass = o.getClass();
        Field[] declaredFields = aClass.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.isAnnotationPresent(SingletonInject.class)) {
                injectSingleton(o, factory, field, implementClass);
            }

            if (field.isAnnotationPresent(PrototypeInject.class) && params.length>0) {
                injectPrototype(o, factory, field, params,implementClass);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void injectSingleton(Object o, BeanFactoryImpl factory, Field field, Class[] implementClass) {
        Class type = field.getType();
        for (Class aClass : implementClass) {
            if (field.getType().isAssignableFrom(aClass)){
                type = aClass;
                break;
            }
        }
        Object bean = factory.getBean(type);
        field.setAccessible(true);
        try {
            field.set(o, bean);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private void injectPrototype(Object o, BeanFactoryImpl factory, Field field, Object[] params, Class[] implementClass) {
        PrototypeInject prototypeInject = field.getAnnotation(PrototypeInject.class);
        boolean consume = prototypeInject.paramConsume();
        Class<?> type = field.getType();
        for (Class aClass : implementClass) {
            if (type.isAssignableFrom(aClass)){
                type = aClass;
                break;
            }
        }
        Constructor constructor = chooseConstructor(type);
        Class[] parameterTypes = constructor.getParameterTypes();
        Object[] thisParams = new Object[parameterTypes.length];
        int i = 0;
        for (Class parameterType : parameterTypes) {
            boolean found = false;
            for (int j = 0; j < params.length; j++) {
                Object param = params[j];
                if (parameterType.isInstance(param)) {
                    thisParams[i] = param;
                    if (consume) {
                        params[j] = null;
                    }
                    found = true;
                    break;
                }
            }
            if (!found) {
                setSingletonParam(thisParams, i, parameterType);
            }
            i++;
        }

        Object bean = factory.getPrototypeBean(type, thisParams);
        field.setAccessible(true);
        try {
            field.set(o, bean);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private void setSingletonParam(Object[] thisParams, int i, Class parameterType) {
        thisParams[i] = factory.getBean(parameterType);
    }

    private Constructor chooseConstructor(Class<?> type) {
        type = context.getImplementClass(type);
        return context.chooseConstructor(type);
    }
}
