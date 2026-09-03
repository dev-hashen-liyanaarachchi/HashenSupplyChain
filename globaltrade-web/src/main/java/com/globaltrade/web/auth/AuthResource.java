package com.globaltrade.web.auth;

import jakarta.ejb.EJB;
import jakarta.ejb.EJBException;
import jakarta.inject.Inject;
import jakarta.security.enterprise.credential.UsernamePasswordCredential;
import jakarta.security.enterprise.identitystore.CredentialValidationResult;
import jakarta.security.enterprise.identitystore.IdentityStoreHandler;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import com.globaltrade.entity.RefreshToken;
import com.globaltrade.entity.User;
import com.globaltrade.exception.InvalidCredentialsException;
import com.globaltrade.exception.UserAlreadyExistsException;
import com.globaltrade.security.LoginService;
import com.globaltrade.security.RefreshTokenService;
import com.globaltrade.web.security.JwtUtil;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Path("/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthResource {

    @Inject
    private IdentityStoreHandler identityStoreHandler;

    @EJB
    private LoginService loginService;

    @EJB
    private RefreshTokenService refreshTokenService;

    @EJB
    private com.globaltrade.ejb.interfaces.VendorService vendorService;

    public record LoginRequest(String username, String password) {
    }

    public record RegisterRequest(String username, String email, String password, String role, String fullName, String phone, String country) {
    }

    public record VendorOnboardRequest(
            String companyName,
            String taxId,
            String email,
            String phone,
            String country,
            String streetAddress,
            String businessCategory,
            String username,
            String password
    ) {
    }

    public record RefreshRequest(String refreshToken) {
    }

    @Path("/login")
    @POST
    public Response login(LoginRequest request) {
        if (request == null || request.username() == null || request.password() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Missing username and password"))
                    .build();
        }

        UsernamePasswordCredential credential = new UsernamePasswordCredential(request.username(), request.password());
        CredentialValidationResult result = identityStoreHandler.validate(credential);

        if (result.getStatus() == CredentialValidationResult.Status.VALID) {
            String username = result.getCallerPrincipal().getName();
            Set<String> roles = result.getCallerGroups();

            String token = JwtUtil.generateToken(username, roles);
            RefreshToken refreshToken = refreshTokenService.create(username);

            return Response.status(Response.Status.OK)
                    .entity(Map.of(
                            "accessToken", token,
                            "refreshToken", refreshToken.getToken(),
                            "username", username,
                            "roles", roles
                    ))
                    .build();
        } else {
            throw new InvalidCredentialsException("Invalid username or password");
        }
    }

    @Path("/register")
    @POST
    public Response register(RegisterRequest request) {
        if (request == null || request.username() == null || request.email() == null || request.password() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Missing required registration parameters (username, email, password)"))
                    .build();
        }

        try {
            User user = loginService.registerUser(request.username(), request.email(), request.password(), request.role());
            Set<String> roles = Set.of(user.getRole().getName());
            String token = JwtUtil.generateToken(user.getUsername(), roles);
            RefreshToken refreshToken = refreshTokenService.create(user.getUsername());

            return Response.status(Response.Status.CREATED)
                    .entity(Map.of(
                            "message", "User registered successfully",
                            "username", user.getUsername(),
                            "email", user.getEmail(),
                            "fullName", request.fullName() != null ? request.fullName() : "",
                            "accessToken", token,
                            "refreshToken", refreshToken.getToken(),
                            "roles", roles
                    ))
                    .build();
        } catch (UserAlreadyExistsException | IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        } catch (EJBException e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", cause.getMessage() != null ? cause.getMessage() : "Registration error"))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Registration error: " + e.getMessage()))
                    .build();
        }
    }

    @Path("/vendor-onboard")
    @POST
    public Response vendorOnboard(VendorOnboardRequest request) {
        if (request == null || request.companyName() == null || request.taxId() == null || request.email() == null || request.username() == null || request.password() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Missing required vendor onboarding fields (Company Name, Tax ID, Email, Username, Password)"))
                    .build();
        }

        try {
            User user = loginService.registerUser(request.username(), request.email(), request.password(), "VENDOR");
            
            com.globaltrade.dto.VendorDTO dto = new com.globaltrade.dto.VendorDTO();
            dto.setCompanyName(request.companyName());
            dto.setTaxIdentificationNumber(request.taxId());
            dto.setEmail(request.email());
            dto.setPhone(request.phone());
            dto.setCountry(request.country());
            dto.setStreetAddress(request.streetAddress());
            dto.setBusinessCategory(request.businessCategory());
            
            if (vendorService != null) {
                vendorService.registerVendor(dto);
            }

            Set<String> roles = Set.of("VENDOR");
            String token = JwtUtil.generateToken(user.getUsername(), roles);
            RefreshToken refreshToken = refreshTokenService.create(user.getUsername());

            return Response.status(Response.Status.CREATED)
                    .entity(Map.of(
                            "message", "Corporate Vendor Onboarded successfully",
                            "companyName", request.companyName(),
                            "taxId", request.taxId(),
                            "username", user.getUsername(),
                            "email", user.getEmail(),
                            "accessToken", token,
                            "refreshToken", refreshToken.getToken(),
                            "roles", roles
                    ))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Vendor Onboarding Failed: " + e.getMessage()))
                    .build();
        }
    }

    @POST
    @Path("/refresh")
    public Response refresh(RefreshRequest request) {
        if (request == null || request.refreshToken() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Missing refresh token"))
                    .build();
        }

        Optional<RefreshToken> tokenOptional = refreshTokenService.findValid(request.refreshToken());
        if (tokenOptional.isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("error", "Invalid or expired refresh token"))
                    .build();
        }

        RefreshToken oldToken = tokenOptional.get();
        String username = oldToken.getUsername();

        refreshTokenService.deleteToken(oldToken.getToken());
        RefreshToken refreshToken = refreshTokenService.create(username);

        Set<String> roles = loginService.getRoles(username);
        String token = JwtUtil.generateToken(username, roles);

        return Response.status(Response.Status.OK)
                .entity(Map.of(
                        "accessToken", token,
                        "refreshToken", refreshToken.getToken(),
                        "username", username,
                        "roles", roles
                ))
                .build();
    }

    @POST
    @Path("/logout")
    public Response logout(RefreshRequest request) {
        if (request != null && request.refreshToken() != null) {
            refreshTokenService.deleteToken(request.refreshToken());
        }
        return Response.status(Response.Status.OK)
                .entity(Map.of("message", "Logged Out"))
                .build();
    }
}
