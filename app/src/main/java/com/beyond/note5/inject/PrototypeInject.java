package com.beyond.note5.inject;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Deprecated
public @interface PrototypeInject {
    boolean paramConsume() default false;
}
