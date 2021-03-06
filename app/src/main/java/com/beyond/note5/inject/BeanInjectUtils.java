package com.beyond.note5.inject;

public class BeanInjectUtils {

    private static class InjectHolder{
        private final static InjectContext CONTEXT = new InjectContextImpl();
        private final static BeanInjector INJECTOR = new BeanInjectorImpl(CONTEXT);
    }

    public static void inject(Object o, Object... params) {
        InjectHolder.INJECTOR.inject(o, params);
    }

    public static void inject(Object o, Class[] implementClasses, Object... params) {
        InjectHolder.INJECTOR.inject(o, implementClasses, params);
    }

    public static <T> void registerSingletonBean(Class<T> tClass, T t) {
        InjectHolder.CONTEXT.registerSingletonBean(tClass, t);
    }

    public static void registerImplementMapping(Class interfaceClass, Class implementClass) {
        InjectHolder.CONTEXT.registerImplementMapping(interfaceClass, implementClass);
    }
}
