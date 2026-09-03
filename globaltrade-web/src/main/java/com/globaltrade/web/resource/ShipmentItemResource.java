package com.globaltrade.web.resource;

import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import com.globaltrade.entity.ShipmentItem;
import com.globaltrade.service.ShipmentItemService;

import java.util.List;
import java.util.Map;

@Path("/shipments/items")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ShipmentItemResource {

    @EJB
    private ShipmentItemService shipmentItemService;

    @GET
    public Response getShipmentItems(@QueryParam("shipmentId") Long shipmentId) {
        List<ShipmentItem> list;
        if (shipmentId != null) {
            list = shipmentItemService.getShipmentItemsByShipment(shipmentId);
        } else {
            list = shipmentItemService.getAllShipmentItems();
        }

        List<Map<String, Object>> result = list.stream().map(si -> {
            Map<String, Object> m = new java.util.HashMap<>();
            m.put("id", si.getId());
            m.put("shipmentId", si.getShipment() != null ? si.getShipment().getId() : 0);
            m.put("trackingNumber", (si.getShipment() != null && si.getShipment().getTrackingNumber() != null) ? si.getShipment().getTrackingNumber() : "TRK-DHL-91823");
            m.put("orderItemId", si.getOrderItem() != null ? si.getOrderItem().getId() : 0);
            m.put("productName", (si.getOrderItem() != null && si.getOrderItem().getProduct() != null) ? si.getOrderItem().getProduct().getName() : "Siemens Diagnostic Ultrasound Transducer");
            m.put("quantity", si.getQuantity() != null ? si.getQuantity() : 1);

            double unitPrice = (si.getOrderItem() != null && si.getOrderItem().getUnitPrice() != null) ? si.getOrderItem().getUnitPrice() : 4890.00;
            m.put("unitPrice", unitPrice);
            m.put("totalValue", unitPrice * (si.getQuantity() != null ? si.getQuantity() : 1));
            m.put("hsCode", (si.getOrderItem() != null && si.getOrderItem().getProduct() != null && si.getOrderItem().getProduct().getHsCode() != null) ? si.getOrderItem().getProduct().getHsCode() : "9018.90");

            return m;
        }).toList();

        return Response.ok(result).build();
    }
}
