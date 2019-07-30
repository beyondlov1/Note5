package com.beyond.note5.inject;

public class BeanInjectUtils {

    private static InjectContext context = new InjectContextImpl();

    private static BeanInjector injector =  new BeanInjectorImpl(context);

    public static void setContext(InjectContext context){
        BeanInjectUtils.context = context;
    }

    public static void setInjector(BeanInjector injector){
        BeanInjectUtils.injector = injector;
    }

    public static void inject(Object o, Object... params) {
        injector.inject(o, params);
    }

    public static void inject(Object o, Class[] implementClasses, Object... params) {
        injector.inject(o, implementClasses, params);
    }

    public static <T> void registerSingletonBean(Class<T> tClass, T t) {
        context.registerSingletonBean(tClass, t);
    }

    public static void registerImplementMapping(Class interfaceClass, Class implementClass) {
        context.registerImplementMapping(interfaceClass, implementClass);
    }
}
