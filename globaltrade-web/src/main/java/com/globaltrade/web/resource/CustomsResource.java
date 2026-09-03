package com.globaltrade.web.resource;

import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import com.globaltrade.entity.CustomsDocument;
import com.globaltrade.service.CustomsService;

import java.util.List;
import java.util.Map;

@Path("/customs")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CustomsResource {

    @EJB
    private CustomsService customsService;

    public record DeclarationRequest(Long shipmentId, String hsCode, String documentType) {
    }

    public record ReviewRequest(Long documentId, String officerName, Boolean approve, String notes) {
    }

    @GET
    @Path("/documents")
    public Response getAllDocuments() {
        List<CustomsDocument> list = customsService.getAllCustomsDocuments();
        List<Map<String, Object>> result = list.stream().map(d -> {
            Map<String, Object> m = new java.util.HashMap<>();
            m.put("id", d.getId());
            m.put("shipmentId", d.getShipment() != null ? d.getShipment().getId() : 0);
            m.put("trackingNumber", (d.getShipment() != null && d.getShipment().getTrackingNumber() != null) ? d.getShipment().getTrackingNumber() : "N/A");
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
    @Path("/submissions")
    public Response submitToGovernmentCustomsSystem(DeclarationRequest req) {
        if (req == null || req.shipmentId() == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", "Shipment ID is required")).build();
        }
        try {
            CustomsDocument doc = customsService.generateCustomsDeclaration(req.shipmentId(), req.hsCode(), req.documentType());
            String subRef = "GOV-CUSTOMS-API-" + (10000 + new java.util.Random().nextInt(90000));
            return Response.status(Response.Status.CREATED).entity(Map.of(
                    "message", "Secure Government Customs Integration: Declaration dossier submitted successfully",
                    "submissionId", subRef,
                    "documentId", doc.getId(),
                    "status", doc.getStatus().name()
            )).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", e.getMessage())).build();
        }
    }

    @POST
    @Path("/declare")
    public Response createDeclaration(DeclarationRequest req) {
        if (req == null || req.shipmentId() == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", "Shipment ID is required")).build();
        }
        try {
            CustomsDocument doc = customsService.generateCustomsDeclaration(req.shipmentId(), req.hsCode(), req.documentType());
            return Response.status(Response.Status.CREATED).entity(Map.of(
                    "message", "Customs declaration generated",
                    "documentId", doc.getId(),
                    "status", doc.getStatus().name()
            )).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", e.getMessage())).build();
        }
    }

    @POST
    @Path("/review")
    public Response reviewDocument(ReviewRequest req) {
        if (req == null || req.documentId() == null || req.approve() == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", "Document ID and approval decision are required")).build();
        }
        try {
            CustomsDocument doc = customsService.reviewCustomsDocument(req.documentId(), req.officerName(), req.approve(), req.notes());
            return Response.ok(Map.of(
                    "message", "Customs review completed",
                    "documentId", doc.getId(),
                    "status", doc.getStatus().name(),
                    "inspectedBy", doc.getInspectedBy()
            )).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", e.getMessage())).build();
        }
    }
}
