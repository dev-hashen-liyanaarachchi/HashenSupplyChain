package com.globaltrade.web.security;

import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.security.enterprise.AuthenticationStatus;
import jakarta.security.enterprise.authentication.mechanism.http.HttpAuthenticationMechanism;
import jakarta.security.enterprise.authentication.mechanism.http.HttpMessageContext;
import jakarta.security.enterprise.identitystore.CredentialValidationResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Set;

@ApplicationScoped
public class JwtAuthMechanism implements HttpAuthenticationMechanism {

    @Inject
    private JwtUtil jwtUtil;

    @Override
    public AuthenticationStatus validateRequest(HttpServletRequest request, HttpServletResponse response, HttpMessageContext context) {
        String path = request.getRequestURI();

        if (path.contains("/api/auth/login") || path.contains("/api/auth/refresh")) {
            return context.doNothing();
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                DecodedJWT jwt = jwtUtil.validateToken(token);
                String username = jwt.getSubject();
                String role = jwt.getClaim("role").asString();

                return context.notifyContainerAboutLogin(new CredentialValidationResult(username, Set.of(role)));
            } catch (Exception e) {
                return context.responseUnauthorized();
            }
        }

        return context.doNothing();
    }
}
