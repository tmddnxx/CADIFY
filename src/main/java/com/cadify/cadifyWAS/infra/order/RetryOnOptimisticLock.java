package com.cadify.cadifyWAS.infra.order;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
public @interface RetryOnOptimisticLock {
    int maxRetries() default 3;
    long delay() default 200; // ms 단위
}
