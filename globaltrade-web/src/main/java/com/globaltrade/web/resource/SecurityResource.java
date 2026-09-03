package com.globaltrade.web.resource;

import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import com.globaltrade.entity.Role;
import com.globaltrade.service.SecurityService;

import java.util.List;
import java.util.Map;

@Path("/security")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class SecurityResource {

    @EJB
    private SecurityService securityService;

    public record AssignPermissionsRequest(
            Long roleId,
            List<String> permissionNames
    ) {}

    @GET
    @Path("/permissions")
    public Response getPermissions() {
        List<Map<String, String>> list = securityService.getAllPermissions();
        return Response.ok(list).build();
    }

    @GET
    @Path("/roles")
    public Response getRoles() {
        List<Role> list = securityService.getAllRoles();
        List<Map<String, Object>> result = list.stream().map(r -> {
            Map<String, Object> m = new java.util.HashMap<>();
            m.put("id", r.getId());
            m.put("name", r.getName());
            List<String> permNames = r.getPermissions() != null ? r.getPermissions().stream().toList() : List.of();
            m.put("permissions", permNames);
            m.put("permissionCount", permNames.size());
            return m;
        }).toList();

        return Response.ok(result).build();
    }

    @POST
    @Path("/roles/assign")
    @jakarta.annotation.security.RolesAllowed({"ADMIN"})
    public Response assignPermissions(AssignPermissionsRequest req) {
        if (req == null || req.roleId() == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", "Role ID is required")).build();
        }

        try {
            Role role = securityService.assignPermissionsToRole(req.roleId(), req.permissionNames());
            List<String> permNames = role.getPermissions() != null ? role.getPermissions().stream().toList() : List.of();

            return Response.ok(Map.of(
                    "message", "Role permissions updated successfully in role_permissions database table!",
                    "roleId", role.getId(),
                    "roleName", role.getName(),
                    "assignedPermissions", permNames
            )).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", e.getMessage())).build();
        }
    }
}
