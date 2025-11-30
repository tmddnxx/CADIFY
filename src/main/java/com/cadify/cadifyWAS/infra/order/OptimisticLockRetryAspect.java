package com.cadify.cadifyWAS.infra.order;

import jakarta.persistence.OptimisticLockException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class OptimisticLockRetryAspect {

    @Around("@annotation(retryAnnotation)")
    public Object retry(ProceedingJoinPoint pjp, RetryOnOptimisticLock retryAnnotation) throws Throwable {
        int maxRetries = retryAnnotation.maxRetries();
        long delay = retryAnnotation.delay();

        int attempt = 0;
        while (true) {
            try {
                return pjp.proceed();
            } catch (ObjectOptimisticLockingFailureException | OptimisticLockException e) {
                attempt++;
                if (attempt >= maxRetries) {
                    throw e; // 재시도 초과 시 예외 그대로 던짐
                }
                Thread.sleep(delay); // 지연 후 재시도
            }
        }
    }
}
