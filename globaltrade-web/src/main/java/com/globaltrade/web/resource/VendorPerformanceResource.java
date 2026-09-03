package com.globaltrade.web.resource;

import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import com.globaltrade.entity.VendorPerformance;
import com.globaltrade.service.VendorPerformanceService;

import java.util.List;
import java.util.Map;

@Path("/vendors")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class VendorPerformanceResource {

    @EJB
    private VendorPerformanceService vendorPerformanceService;

    public record EvaluateVendorRequest(
            Long vendorId,
            Double fulfillmentScore,
            Double onTimeDeliveryRate,
            Double qualityRating
    ) {}

    @GET
    @Path("/performance")
    public Response getVendorPerformances() {
        List<VendorPerformance> list = vendorPerformanceService.getAllVendorPerformances();
        List<Map<String, Object>> result = list.stream().map(vp -> {
            Map<String, Object> m = new java.util.HashMap<>();
            m.put("id", vp.getId());
            m.put("vendorId", vp.getVendor() != null ? vp.getVendor().getId() : 0);
            m.put("vendorName", vp.getVendor() != null ? (vp.getVendor().getCompanyName() != null ? vp.getVendor().getCompanyName() : vp.getVendor().getName()) : "Global Supplier");
            m.put("email", vp.getVendor() != null ? vp.getVendor().getEmail() : "supplier@company.com");
            m.put("fulfillmentScore", vp.getFulfillmentScore());
            m.put("onTimeDeliveryRate", vp.getOnTimeDeliveryRate());
            m.put("qualityRating", vp.getQualityRating());
            m.put("evaluatedAt", vp.getEvaluatedAt() != null ? vp.getEvaluatedAt().toString() : "Recent");
            return m;
        }).toList();

        return Response.ok(result).build();
    }

    @POST
    @Path("/evaluate")
    public Response evaluateVendor(EvaluateVendorRequest req) {
        try {
            Long vId = req != null && req.vendorId() != null ? req.vendorId() : 1L;
            Double score = req != null && req.fulfillmentScore() != null ? req.fulfillmentScore() : 98.5;
            Double onTime = req != null && req.onTimeDeliveryRate() != null ? req.onTimeDeliveryRate() : 99.0;
            Double quality = req != null && req.qualityRating() != null ? req.qualityRating() : 5.0;

            VendorPerformance vp = vendorPerformanceService.evaluateVendor(vId, score, onTime, quality);

            return Response.ok(Map.of(
                    "message", "Vendor performance evaluation saved to database!",
                    "id", vp.getId(),
                    "fulfillmentScore", vp.getFulfillmentScore(),
                    "onTimeDeliveryRate", vp.getOnTimeDeliveryRate(),
                    "qualityRating", vp.getQualityRating()
            )).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", e.getMessage())).build();
        }
    }
}
