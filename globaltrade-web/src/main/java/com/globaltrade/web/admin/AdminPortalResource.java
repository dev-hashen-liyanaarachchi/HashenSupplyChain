package com.globaltrade.web.admin;

import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import com.globaltrade.service.AdminManagementService;
import java.util.*;

@Path("/admin")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AdminPortalResource {

    @EJB
    private AdminManagementService adminManagementService;

    public record AdminUserRequest(String username, String password, String email, Long roleId) {}

    @GET
    @Path("/status")
    public Response getSystemStatus() {
        return Response.ok(Map.of("status", "ACTIVE", "message", "GlobalTrade Logistics Corporation Platform Active.")).build();
    }

    @GET
    @Path("/users")
    public Response getAllAdmins() {
        List<Map<String, Object>> list = adminManagementService.getAllAdminUsers();
        return Response.ok(list).build();
    }

    @POST
    @Path("/users")
    public Response createAdmin(AdminUserRequest req) {
        if (req == null || req.username() == null || req.username().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Username and email are required"))
                    .build();
        }

        try {
            Map<String, Object> created = adminManagementService.createAdminUser(
                    req.username(),
                    req.password(),
                    req.email(),
                    req.roleId()
            );
            return Response.status(Response.Status.CREATED).entity(created).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        }
    }
}
