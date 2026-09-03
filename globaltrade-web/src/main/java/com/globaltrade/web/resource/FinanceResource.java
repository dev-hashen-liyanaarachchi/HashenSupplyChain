package com.globaltrade.web.resource;

import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import com.globaltrade.entity.CustomsDocument;
import com.globaltrade.service.FinanceService;

import java.util.List;
import java.util.Map;

@Path("/finance")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class FinanceResource {

    @EJB
    private FinanceService financeService;

    public record SettlementRequest(Long documentId, String carrierName, String financeOfficer) {}

    @GET
    @Path("/ledger")
    public Response getFinanceLedger() {
        List<CustomsDocument> list = financeService.getClearedCustomsForSettlement();
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
            m.put("freightCharge", d.getFreightCharge() != null ? d.getFreightCharge() : 180.00);
            m.put("originCountry", d.getOriginCountry() != null ? d.getOriginCountry() : "US");
            m.put("destinationCountry", d.getDestinationCountry() != null ? d.getDestinationCountry() : "LK");
            m.put("exporterName", d.getExporterName() != null ? d.getExporterName() : "USA New York Air Cargo Hub");
            m.put("importerName", d.getImporterName() != null ? d.getImporterName() : "Consignee Client");
            m.put("packingListItems", d.getPackingListItems() != null ? d.getPackingListItems() : "1x Medical Cargo Container");
            m.put("settlementStatus", d.getSettlementStatus() != null ? d.getSettlementStatus() : "PENDING_DUTY_SETTLEMENT");
            m.put("assignedCarrier", d.getAssignedCarrier() != null ? d.getAssignedCarrier() : "Pending Carrier Handover");
            return m;
        }).toList();

        return Response.ok(result).build();
    }

    @GET
    @Path("/payments")
    public Response getAllPayments() {
        List<com.globaltrade.entity.Payment> list = financeService.getAllPayments();
        List<Map<String, Object>> result = list.stream().map(p -> {
            Map<String, Object> m = new java.util.HashMap<>();
            m.put("id", p.getId());
            m.put("orderId", p.getOrder() != null ? p.getOrder().getId() : 0);
            m.put("orderNumber", p.getOrder() != null ? p.getOrder().getOrderNumber() : "ORD-90182");
            m.put("customerName", (p.getOrder() != null && p.getOrder().getCustomer() != null) ? (p.getOrder().getCustomer().getFirstName() + " " + p.getOrder().getCustomer().getLastName()) : "Customer Client");
            m.put("transactionReference", p.getTransactionReference());
            m.put("paymentMethod", p.getPaymentMethod() != null ? p.getPaymentMethod().name() : "CREDIT_CARD");
            m.put("paymentStatus", p.getPaymentStatus() != null ? p.getPaymentStatus().name() : "COMPLETED");
            m.put("amount", p.getAmount() != null ? p.getAmount() : 4890.00);
            m.put("timestamp", p.getTimestamp() != null ? p.getTimestamp().toString() : "Recent");
            return m;
        }).toList();

        return Response.ok(result).build();
    }

    @POST
    @Path("/settle")
    public Response settleDutyAndHandover(SettlementRequest req) {
        if (req == null || req.documentId() == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", "Document ID is required")).build();
        }
        try {
            String carrier = req.carrierName() != null ? req.carrierName() : "DHL Express International Air Fleet";
            String officer = req.financeOfficer() != null ? req.financeOfficer() : "Chief Financial Officer Perera (#409)";
            CustomsDocument doc = financeService.settleDutyAndHandoverToCarrier(req.documentId(), carrier, officer);

            return Response.ok(Map.of(
                    "message", "Import duty tax settled successfully! Cargo handed over to carrier.",
                    "documentId", doc.getId(),
                    "settlementStatus", doc.getSettlementStatus(),
                    "assignedCarrier", doc.getAssignedCarrier()
            )).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", e.getMessage())).build();
        }
    }
}
