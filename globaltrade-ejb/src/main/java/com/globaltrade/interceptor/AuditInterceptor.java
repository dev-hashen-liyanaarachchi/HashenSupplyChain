package com.globaltrade.interceptor;

import jakarta.annotation.Resource;
import jakarta.ejb.SessionContext;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.InvocationContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import com.globaltrade.entity.AuditLog;
import jakarta.interceptor.Interceptor;

import java.util.logging.Logger;

@Interceptor
public class AuditInterceptor {

    private static final Logger LOGGER = Logger.getLogger(AuditInterceptor.class.getName());

    @PersistenceContext(unitName = "GlobalTradePU")
    private EntityManager em;

    @Resource
    private SessionContext sessionContext;

    @AroundInvoke
    public Object logInvocation(InvocationContext context) throws Exception {
        String methodName = context.getMethod().getDeclaringClass().getSimpleName() + "." + context.getMethod().getName();
        String caller = (sessionContext != null && sessionContext.getCallerPrincipal() != null)
                ? sessionContext.getCallerPrincipal().getName() : "ANONYMOUS";

        long startTime = System.currentTimeMillis();
        LOGGER.info("[AUDIT INTERCEPTOR START] Principal: " + caller + " -> Target: " + methodName);

        try {
            Object result = context.proceed();
            long duration = System.currentTimeMillis() - startTime;
            LOGGER.info("[AUDIT INTERCEPTOR SUCCESS] Target: " + methodName + " Duration: " + duration + " ms");

            if (em != null) {
                AuditLog log = new AuditLog(caller, "LOGISTICS_USER", "INVOKE_SUCCESS", methodName, "127.0.0.1", duration);
                em.persist(log);
            }
            return result;
        } catch (Exception ex) {
            long duration = System.currentTimeMillis() - startTime;
            LOGGER.severe("[AUDIT INTERCEPTOR ERROR] Target: " + methodName + " Failed: " + ex.getMessage());

            if (em != null) {
                AuditLog log = new AuditLog(caller, "LOGISTICS_USER", "INVOKE_FAILED: " + ex.getClass().getSimpleName(), methodName, "127.0.0.1", duration);
                em.persist(log);
            }
            throw ex;
        }
    }
}
