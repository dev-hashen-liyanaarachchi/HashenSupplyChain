package com.globaltrade.web.resource;

import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import com.globaltrade.entity.Carrier;
import com.globaltrade.entity.Shipment;
import com.globaltrade.enums.ShipmentType;
import com.globaltrade.service.CarrierService;

import java.util.List;
import java.util.Map;

@Path("/carriers")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CarrierResource {

    @EJB
    private CarrierService carrierService;

    public record CreateCarrierRequest(
            String name,
            String carrierType,
            String countryCode,
            String contactPhone,
            String contactEmail,
            String fleetSize
    ) {}

    public record HandoverRequest(
            Long shipmentId,
            Long carrierId,
            String driverName,
            String vehicleNo
    ) {}

    @GET
    public Response getAllCarriers() {
        List<Carrier> list = carrierService.getAllCarriers();
        List<Map<String, Object>> result = list.stream().map(c -> {
            Map<String, Object> m = new java.util.HashMap<>();
            m.put("id", c.getId());
            m.put("name", c.getName());
            m.put("carrierType", c.getCarrierType() != null ? c.getCarrierType().name() : "AIR_FREIGHT");
            m.put("countryCode", c.getCountryCode() != null ? c.getCountryCode() : "LK");
            m.put("contactPhone", c.getContactPhone() != null ? c.getContactPhone() : "+94 11 234 5678");
            m.put("contactEmail", c.getContactEmail() != null ? c.getContactEmail() : "dispatch@lankalogistics.lk");
            m.put("fleetSize", c.getFleetSize() != null ? c.getFleetSize() : "45 Vans & Air Fleet");
            m.put("operatingStatus", c.getOperatingStatus() != null ? c.getOperatingStatus() : "ACTIVE");
            return m;
        }).toList();

        return Response.ok(result).build();
    }

    @POST
    public Response addCarrier(CreateCarrierRequest req) {
        if (req == null || req.name() == null || req.name().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", "Carrier name is required")).build();
        }

        try {
            ShipmentType type = ShipmentType.EXPRESS_COURIER;
            if (req.carrierType() != null) {
                try {
                    type = ShipmentType.valueOf(req.carrierType().toUpperCase());
                } catch (Exception ignored) {}
            }

            Carrier carrier = new Carrier(req.name(), type, req.countryCode() != null ? req.countryCode() : "LK");
            if (req.contactPhone() != null) carrier.setContactPhone(req.contactPhone());
            if (req.contactEmail() != null) carrier.setContactEmail(req.contactEmail());
            if (req.fleetSize() != null) carrier.setFleetSize(req.fleetSize());

            Carrier created = carrierService.addCarrier(carrier);
            return Response.status(Response.Status.CREATED).entity(created).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", e.getMessage())).build();
        }
    }

    @POST
    @Path("/handover")
    public Response handoverCargo(HandoverRequest req) {
        if (req == null || req.shipmentId() == null || req.carrierId() == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", "Shipment ID and Carrier ID are required")).build();
        }

        try {
            Shipment s = carrierService.handoverCargoToDestinationCarrier(
                    req.shipmentId(),
                    req.carrierId(),
                    req.driverName(),
                    req.vehicleNo()
            );

            return Response.ok(Map.of(
                    "message", "Cargo successfully handed over to destination carrier!",
                    "shipmentId", s.getId(),
                    "trackingNumber", s.getTrackingNumber(),
                    "carrierName", s.getCarrier() != null ? s.getCarrier().getName() : "Local Carrier Fleet",
                    "status", s.getStatus().name()
            )).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", e.getMessage())).build();
        }
    }
}
