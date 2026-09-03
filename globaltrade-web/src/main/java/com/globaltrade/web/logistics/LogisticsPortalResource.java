package com.globaltrade.web.logistics;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import com.globaltrade.ejb.interfaces.ShipmentService;
import com.globaltrade.ejb.interfaces.TrackingService;
import com.globaltrade.dto.ShipmentDTO;
import com.globaltrade.entity.Shipment;
import com.globaltrade.enums.ShipmentStatus;
import com.globaltrade.exception.CustomsException;
import com.globaltrade.exception.ShipmentException;
import java.util.List;

@Path("/logistics")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class LogisticsPortalResource {

    @EJB
    private ShipmentService shipmentService;

    @EJB
    private TrackingService trackingService;

    @POST
    @Path("/shipments")
    @RolesAllowed({"LOGISTICS_COORDINATOR", "SYSTEM_ADMIN"})
    public Response dispatchShipment(ShipmentDTO dto) throws ShipmentException, CustomsException {
        Shipment shipment = shipmentService.createShipment(dto);
        return Response.status(Response.Status.CREATED).entity(shipment).build();
    }

    @POST
    @Path("/shipments/batch-update")
    @RolesAllowed({"LOGISTICS_COORDINATOR", "SYSTEM_ADMIN"})
    public Response batchUpdateShipmentStatus(@QueryParam("status") ShipmentStatus status, List<Long> shipmentIds) {
        int updated = shipmentService.processBatchShipmentUpdate(shipmentIds, status);
        return Response.ok("Successfully updated " + updated + " shipments to status " + status).build();
    }

    @GET
    @Path("/tracking/{trackingNumber}")
    public Response getTrackingHistory(@PathParam("trackingNumber") String trackingNumber) {
        return Response.ok(trackingService.getTrackingHistory(trackingNumber)).build();
    }
}
