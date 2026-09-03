package com.globaltrade.web.resource;

import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import com.globaltrade.entity.Notification;
import com.globaltrade.service.NotificationService;

import java.util.List;
import java.util.Map;

@Path("/notifications")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class NotificationResource {

    @EJB
    private NotificationService notificationService;

    @GET
    public Response getNotifications() {
        List<Notification> list = notificationService.getAllNotifications();
        List<Map<String, Object>> result = list.stream().map(n -> {
            Map<String, Object> m = new java.util.HashMap<>();
            m.put("id", n.getId());
            m.put("title", n.getTitle());
            m.put("message", n.getMessage());
            m.put("readStatus", n.isReadStatus());
            m.put("createdAt", n.getCreatedAt() != null ? n.getCreatedAt().toString() : "");
            
            // Extract category and severity
            String category = "GENERAL";
            if (n.getTitle().contains("CUSTOMS")) category = "CUSTOMS";
            else if (n.getTitle().contains("INVENTORY") || n.getTitle().contains("Stock")) category = "INVENTORY";
            else if (n.getTitle().contains("LOGISTICS") || n.getTitle().contains("Shipment") || n.getTitle().contains("Cargo")) category = "LOGISTICS";
            else if (n.getTitle().contains("VENDOR")) category = "VENDOR";

            String severity = "INFO";
            if (n.getTitle().contains("Alert") || n.getTitle().contains("Low") || n.getMessage().contains("reached threshold")) severity = "CRITICAL";
            else if (n.getTitle().contains("Required") || n.getTitle().contains("Approaching") || n.getMessage().contains("48-hr")) severity = "WARNING";
            else if (n.getTitle().contains("Advanced") || n.getTitle().contains("Completed") || n.getMessage().contains("departed")) severity = "SUCCESS";

            m.put("category", category);
            m.put("severity", severity);
            return m;
        }).toList();

        return Response.ok(result).build();
    }

    @POST
    @Path("/read/{id}")
    public Response markAsRead(@PathParam("id") Long id) {
        notificationService.markAsRead(id);
        return Response.ok(Map.of("message", "Notification marked as read")).build();
    }

    @POST
    @Path("/clear")
    public Response clearAll() {
        notificationService.clearAll();
        return Response.ok(Map.of("message", "Notifications cleared")).build();
    }
}
