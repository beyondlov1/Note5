package com.beyond.note5.inject;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class BeanFactoryImpl implements BeanFactory {
    private final InjectContext context;

    public BeanFactoryImpl(InjectContext context) {
        this.context = context;
    }

    @Override
    public <T> T getPrototypeBean(Class<T> tClass, Object... params) {
        return tClass.cast(getPrototypeObjectBean(tClass, params));
    }

    private Object getPrototypeObjectBean(Class c, Object... params) {
        return createPrototypeBean(c, params);
    }

    @SuppressWarnings("unchecked")
    private Object createPrototypeBean(Class tClass, Object... params) {
        if (tClass.isInterface()) {
            tClass = context.getImplementClass(tClass);
        }
        try {
            Constructor chosenConstructor = chooseConstructor(tClass);
            return chosenConstructor.newInstance(params);
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }


    @Override
    public <T> T getBean(Class<T> tClass) {
        return tClass.cast(getObjectBean(tClass));
    }

    @Override
    public <T> T getBean(Class<T> tClass, Object... additionalParams) {
        return tClass.cast(getObjectBean(tClass, additionalParams));
    }

    @SuppressWarnings("unchecked")
    private Object getObjectBean(Class tClass, Object... additionalParams) {
        Object o = context.getSingletonBean(tClass);
        if (o == null) {
            o = createBean(tClass, additionalParams);
            if (o != null) {
                context.registerSingletonBean(tClass, o);
            }
        }
        return o;
    }

    private Object createBean(Class tClass, Object[] additionalParams) {
        if (tClass.isInterface()) {
            tClass = context.getImplementClass(tClass);
        }
        if (tClass == null) {
            throw new RuntimeException("class not found");
        }

        Constructor chosenConstructor = chooseConstructor(tClass);
        int parameterCount = chosenConstructor.getParameterTypes().length;
        Object[] parameters = new Object[chosenConstructor.getParameterTypes().length];
        Class[] parameterTypes = chosenConstructor.getParameterTypes();
        for (int i = 0; i < parameterCount; i++) {
            for (Object additionalParam : additionalParams) {
                if (parameterTypes[i].isInstance(additionalParam)) {
                    parameters[i] = additionalParam;
                }
            }
        }

        for (int i = 0; i < parameterCount; i++) {
            if (parameters[i] == null) {
                parameters[i] = getObjectBean(parameterTypes[i]);
            }
        }

        try {
            chosenConstructor.setAccessible(true);
            return chosenConstructor.newInstance(parameters);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Constructor chooseConstructor(Class tClass) {
        return context.chooseConstructor(tClass);
    }

}
