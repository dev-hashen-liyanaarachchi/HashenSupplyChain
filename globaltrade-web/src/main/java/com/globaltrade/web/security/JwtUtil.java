package com.globaltrade.web.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

@ApplicationScoped
public class JwtUtil {

    private static final String SECRET = "GlobalTradeLogisticsSuperSecretJWTKey2026";
    private static final long EXPIRATION_TIME = 86400000; // 24 hours

    public static String generateToken(String username, String role) {
        return JWT.create()
                .withSubject(username)
                .withClaim("role", role)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .sign(Algorithm.HMAC256(SECRET));
    }

    public static String generateToken(String username, Collection<String> roles) {
        String roleStr = (roles != null && !roles.isEmpty()) ? roles.iterator().next() : "CUSTOMER";
        return JWT.create()
                .withSubject(username)
                .withClaim("role", roleStr)
                .withClaim("roles", roles != null ? List.copyOf(roles) : List.of())
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .sign(Algorithm.HMAC256(SECRET));
    }

    public static DecodedJWT validateToken(String token) {
        return JWT.require(Algorithm.HMAC256(SECRET))
                .build()
                .verify(token);
    }

    public static String getUsername(String token) {
        return validateToken(token).getSubject();
    }

    public static String getRole(String token) {
        return validateToken(token).getClaim("role").asString();
    }
}
