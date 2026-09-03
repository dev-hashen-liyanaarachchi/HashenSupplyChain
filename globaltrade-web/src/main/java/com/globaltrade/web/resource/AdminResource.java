package com.globaltrade.web.resource;

import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import com.globaltrade.service.InventoryService;
import com.globaltrade.service.CustomsService;
import com.globaltrade.service.WarehouseService;
import com.globaltrade.entity.CustomsDocument;

import java.util.List;
import java.util.Map;

@Path("/admin")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AdminResource {

    @EJB
    private InventoryService inventoryService;

    @EJB
    private CustomsService customsService;

    @EJB
    private WarehouseService warehouseService;

    public record OrderStatusUpdate(Long orderId, String status) {
    }

    public record ShipmentStatusUpdate(Long shipmentId, String status) {
    }

    public record CustomsReviewRequest(Long documentId, String officerName, Boolean approve, String notes) {
    }

    @GET
    @Path("/dashboard-stats")
    public Response getDashboardStats() {
        List<Map<String, Object>> orders = inventoryService.getOrdersWithShipments();
        List<Map<String, Object>> shipments = inventoryService.getAllShipments();
        List<CustomsDocument> customsDocs = customsService.getAllCustomsDocuments();

        double totalRevenue = orders.stream()
                .mapToDouble(o -> o.get("totalAmount") != null ? ((Number) o.get("totalAmount")).doubleValue() : 0.0)
                .sum();

        long pendingPacking = orders.stream()
                .filter(o -> "PROCESSING".equalsIgnoreCase((String) o.get("status")) || "PENDING".equalsIgnoreCase((String) o.get("status")) || "PICKING_PACKING".equalsIgnoreCase((String) o.get("status")))
                .count();

        long inCustoms = customsDocs.stream()
                .filter(c -> c.getStatus() != null && (c.getStatus().name().contains("SUBMITTED") || c.getStatus().name().contains("HOLD") || c.getStatus().name().contains("PENDING")))
                .count();

        Map<String, Object> stats = Map.of(
                "totalOrders", orders.size(),
                "totalRevenue", Math.round(totalRevenue * 100.0) / 100.0,
                "totalShipments", shipments.size(),
                "pendingPackingCount", pendingPacking,
                "inCustomsCount", inCustoms
        );

        return Response.ok(stats).build();
    }

    @GET
    @Path("/orders")
    public Response getOrders() {
        List<Map<String, Object>> orders = inventoryService.getOrdersWithShipments();
        return Response.ok(orders).build();
    }

    @POST
    @Path("/orders/status")
    public Response updateOrderStatus(OrderStatusUpdate req) {
        if (req == null || req.orderId() == null || req.status() == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", "Order ID and status required")).build();
        }
        com.globaltrade.entity.Order updated = inventoryService.updateOrderStatus(req.orderId(), req.status());
        if (updated != null) {
            return Response.ok(Map.of(
                    "message", "Order status updated successfully",
                    "orderId", updated.getId(),
                    "status", updated.getStatus().name()
            )).build();
        }
        return Response.status(Response.Status.NOT_FOUND).entity(Map.of("error", "Order not found")).build();
    }

    @GET
    @Path("/shipments")
    public Response getShipments() {
        List<Map<String, Object>> shipments = inventoryService.getAllShipments();
        return Response.ok(shipments).build();
    }

    @POST
    @Path("/shipments/status")
    public Response updateShipmentStatus(ShipmentStatusUpdate req) {
        if (req == null || req.shipmentId() == null || req.status() == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", "Shipment ID and status required")).build();
        }
        com.globaltrade.entity.Shipment updated = inventoryService.updateShipmentStatus(req.shipmentId(), req.status());
        if (updated != null) {
            return Response.ok(Map.of(
                    "message", "Shipment status updated successfully",
                    "shipmentId", updated.getId(),
                    "status", updated.getStatus().name()
            )).build();
        }
        return Response.status(Response.Status.NOT_FOUND).entity(Map.of("error", "Shipment not found")).build();
    }

    @GET
    @Path("/customs")
    public Response getCustomsDocuments() {
        List<CustomsDocument> list = customsService.getAllCustomsDocuments();
        List<Map<String, Object>> result = list.stream().map(d -> {
            Map<String, Object> m = new java.util.HashMap<>();
            m.put("id", d.getId());
            m.put("shipmentId", d.getShipment() != null ? d.getShipment().getId() : 0);
            m.put("trackingNumber", (d.getShipment() != null && d.getShipment().getTrackingNumber() != null) ? d.getShipment().getTrackingNumber() : "TRK-GEN-100");
            m.put("originWarehouse", (d.getShipment() != null && d.getShipment().getOriginWarehouse() != null) ? d.getShipment().getOriginWarehouse().getName() : "Logistics Hub");
            m.put("documentType", d.getDocumentType() != null ? d.getDocumentType() : "COMMERCIAL_INVOICE");
            m.put("hsCode", d.getHsCode() != null ? d.getHsCode() : "9018.90");
            m.put("status", d.getStatus() != null ? d.getStatus().name() : "SUBMITTED");
            m.put("inspectedBy", d.getInspectedBy() != null ? d.getInspectedBy() : "Pending Inspection");
            m.put("declaredValue", d.getDeclaredValue() != null ? d.getDeclaredValue() : 4890.00);
            m.put("dutyFee", d.getDutyFee() != null ? d.getDutyFee() : 244.50);
            m.put("originCountry", d.getOriginCountry() != null ? d.getOriginCountry() : "US");
            m.put("destinationCountry", d.getDestinationCountry() != null ? d.getDestinationCountry() : "LK");
            m.put("exporterName", d.getExporterName() != null ? d.getExporterName() : "USA New York Air Cargo Center");
            m.put("importerName", d.getImporterName() != null ? d.getImporterName() : "Consignee Client");
            m.put("packingListItems", d.getPackingListItems() != null ? d.getPackingListItems() : "1x Medical Cargo Container");
            m.put("clearanceDeadline", d.getClearanceDeadline() != null ? d.getClearanceDeadline().toString() : "48 Hours Window");
            return m;
        }).toList();
        return Response.ok(result).build();
    }

    @POST
    @Path("/customs/review")
    public Response reviewCustomsDocument(CustomsReviewRequest req) {
        if (req == null || req.documentId() == null || req.approve() == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", "Document ID and approve decision required")).build();
        }
        try {
            String officer = (req.officerName() != null && !req.officerName().isBlank()) ? req.officerName() : "Chief Officer #8902";
            CustomsDocument doc = customsService.reviewCustomsDocument(req.documentId(), officer, req.approve(), req.notes());
            return Response.ok(Map.of(
                    "message", "Customs clearance status updated",
                    "documentId", doc.getId(),
                    "status", doc.getStatus().name(),
                    "inspectedBy", doc.getInspectedBy()
            )).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", e.getMessage())).build();
        }
    }
}
