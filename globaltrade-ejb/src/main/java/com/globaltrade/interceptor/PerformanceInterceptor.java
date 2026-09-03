package com.globaltrade.interceptor;

import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.InvocationContext;
import jakarta.interceptor.Interceptor;
import java.util.logging.Logger;

@Interceptor
public class PerformanceInterceptor {

    private static final Logger LOGGER = Logger.getLogger(PerformanceInterceptor.class.getName());
    private static final long SLA_THRESHOLD_MS = 500;

    @AroundInvoke
    public Object measurePerformance(InvocationContext context) throws Exception {
        long startTime = System.currentTimeMillis();
        try {
            return context.proceed();
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            String methodName = context.getMethod().getDeclaringClass().getSimpleName() + "." + context.getMethod().getName();

            if (duration > SLA_THRESHOLD_MS) {
                LOGGER.warning("[SLA PERFORMANCE WARNING] Method " + methodName + " executed in " + duration + " ms (Exceeded " + SLA_THRESHOLD_MS + " ms SLA)");
            } else {
                LOGGER.info("[PERFORMANCE METRIC] Method " + methodName + " executed in " + duration + " ms.");
            }
        }
    }
}
