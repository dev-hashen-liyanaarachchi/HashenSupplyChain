package com.globaltrade.interceptor;

import jakarta.annotation.Resource;
import jakarta.ejb.SessionContext;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.InvocationContext;
import jakarta.interceptor.Interceptor;

import java.util.logging.Logger;

@Interceptor
public class SecurityInterceptor {

    private static final Logger LOGGER = Logger.getLogger(SecurityInterceptor.class.getName());

    @Resource
    private SessionContext sessionContext;

    @AroundInvoke
    public Object checkSecurityContext(InvocationContext context) throws Exception {
        String methodName = context.getMethod().getName();
        String principalName = (sessionContext != null && sessionContext.getCallerPrincipal() != null)
                ? sessionContext.getCallerPrincipal().getName() : "UNAUTHENTICATED";

        LOGGER.info("[SECURITY INTERCEPTOR] Verifying security context for Principal: " + principalName + " on Method: " + methodName);
        return context.proceed();
    }
}
