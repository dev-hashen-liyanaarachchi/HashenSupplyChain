package com.globaltrade.interceptor;

import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.InvocationContext;
import com.globaltrade.exception.CustomsException;
import jakarta.interceptor.Interceptor;

import java.util.logging.Logger;

@Interceptor
public class ComplianceInterceptor {

    private static final Logger LOGGER = Logger.getLogger(ComplianceInterceptor.class.getName());

    @AroundInvoke
    public Object checkInternationalCompliance(InvocationContext context) throws Exception {
        Object[] params = context.getParameters();
        if (params != null) {
            for (Object param : params) {
                if (param instanceof String str && str.startsWith("EMBARGO")) {
                    LOGGER.severe("[COMPLIANCE VIOLATION DETECTED] Embargoed destination or item code detected: " + str);
                    throw new CustomsException(str, "International Trade Sanctions Violation.");
                }
            }
        }
        return context.proceed();
    }
}
