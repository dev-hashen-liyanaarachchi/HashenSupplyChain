package com.globaltrade.web.test;

import com.globaltrade.web.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GlobalTradeJWTUnitTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    public void setUp() {
        jwtUtil = new JwtUtil();
    }

    @Test
    @DisplayName("Test 1: JWT Token Generation and Signature Verification")
    public void testJwtGenerationAndVerification() {
        String token = jwtUtil.generateToken("globalvendor", "VENDOR_REP");
        assertNotNull(token, "JWT Token should not be null.");

        String username = jwtUtil.getUsername(token);
        String role = jwtUtil.getRole(token);

        assertEquals("globalvendor", username);
        assertEquals("VENDOR_REP", role);
    }
}
