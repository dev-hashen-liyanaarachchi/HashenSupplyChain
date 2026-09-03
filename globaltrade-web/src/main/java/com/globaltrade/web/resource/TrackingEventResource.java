package com.globaltrade.web.resource;

import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import com.globaltrade.entity.TrackingEvent;
import com.globaltrade.service.TrackingEventService;

import java.util.List;
import java.util.Map;

@Path("/tracking")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class TrackingEventResource {

    @EJB
    private TrackingEventService trackingEventService;

    @GET
    @Path("/events")
    public Response getTrackingEvents(@QueryParam("shipmentId") Long shipmentId) {
        List<TrackingEvent> list;
        if (shipmentId != null) {
            list = trackingEventService.getEventsForShipment(shipmentId);
        } else {
            list = trackingEventService.getAllTrackingEvents();
        }

        List<Map<String, Object>> result = list.stream().map(t -> {
            Map<String, Object> m = new java.util.HashMap<>();
            m.put("id", t.getId());
            m.put("shipmentId", t.getShipment() != null ? t.getShipment().getId() : 0);
            m.put("trackingNumber", (t.getShipment() != null && t.getShipment().getTrackingNumber() != null) ? t.getShipment().getTrackingNumber() : "TRK-DHL-91823");
            m.put("location", t.getLocation());
            m.put("description", t.getDescription());
            m.put("timestamp", t.getTimestamp() != null ? t.getTimestamp().toString() : "Recent");
            return m;
        }).toList();

        return Response.ok(result).build();
    }
}
