package com.globaltrade.web.resource;

import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import com.globaltrade.entity.Product;
import com.globaltrade.entity.Inventory;
import com.globaltrade.service.InventoryService;

import java.util.*;

@Path("/storefront")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CustomerStorefrontResource {

    @EJB
    private InventoryService inventoryService;

    public record CartItemRequest(Long productId, Integer qty) {
    }

    public record CheckoutRequest(
            String customerName,
            String phone,
            String email,
            String street,
            String city,
            String postalCode,
            String destinationCountryCode,
            String originCountryCode,
            Long warehouseId,
            String paymentMethod,
            List<CartItemRequest> items
    ) {
    }

    @GET
    @Path("/products")
    public Response getStorefrontProducts() {
        List<Product> products = inventoryService.getAllProducts();
        List<Inventory> inventories = inventoryService.getAllInventories();

        List<Map<String, Object>> result = products.stream().map(p -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", p.getId());
            m.put("sku", p.getSku());
            m.put("name", p.getName());
            m.put("price", p.getPrice());
            m.put("weightKg", p.getWeightKg() != null ? p.getWeightKg() : 1.0);
            m.put("hsCode", p.getHsCode());
            m.put("description", p.getDescription());
            m.put("categoryName", p.getCategory() != null ? p.getCategory().getName() : "General Trade");
            m.put("brandName", p.getBrand() != null ? p.getBrand().getName() : "Global Brand");

            int totalStock = inventories.stream()
                    .filter(i -> i.getProduct() != null && i.getProduct().getId().equals(p.getId()))
                    .mapToInt(Inventory::getAvailableQty)
                    .sum();
            int finalStock = totalStock > 0 ? totalStock : 150;
            m.put("availableStock", finalStock);
            m.put("stockQty", finalStock);

            List<Map<String, Object>> whList = inventories.stream()
                    .filter(i -> i.getProduct() != null && i.getProduct().getId().equals(p.getId()) && i.getWarehouse() != null)
                    .map(i -> {
                        Map<String, Object> wm = new HashMap<>();
                        wm.put("warehouseId", i.getWarehouse().getId());
                        wm.put("warehouseName", i.getWarehouse().getName());
                        String ccode = (i.getWarehouse().getAddress() != null && i.getWarehouse().getAddress().getCountry() != null)
                                ? i.getWarehouse().getAddress().getCountry().getCode() : "DE";
                        wm.put("countryCode", ccode);
                        wm.put("availableQty", i.getAvailableQty());
                        return wm;
                    }).toList();

            m.put("warehouses", whList);

            return m;
        }).toList();

        return Response.ok(result).build();
    }

    public record QuoteRequest(
            String originCountryCode,
            String destinationCountryCode,
            Double totalWeightKg
    ) {
    }

    @POST
    @Path("/quote")
    public Response getShippingQuote(QuoteRequest req) {
        if (req == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", "Quote request body required")).build();
        }
        com.globaltrade.service.ShippingRateService.ShippingQuote quote = inventoryService.getShippingQuote(
                req.originCountryCode(),
                req.destinationCountryCode(),
                req.totalWeightKg() != null ? req.totalWeightKg() : 1.0
        );
        return Response.ok(quote).build();
    }

    @POST
    @Path("/checkout")
    public Response checkout(CheckoutRequest req) {
        if (req == null || req.customerName() == null || req.customerName().isBlank() || req.items() == null || req.items().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Customer name and cart items are required"))
                    .build();
        }

        try {
            List<Map<String, Object>> itemsList = new ArrayList<>();
            for (CartItemRequest itemReq : req.items()) {
                itemsList.add(Map.of(
                        "productId", itemReq.productId(),
                        "qty", itemReq.qty() != null ? itemReq.qty() : 1
                ));
            }

            Map<String, Object> orderResult = inventoryService.placeStorefrontOrder(
                    req.customerName(),
                    req.phone(),
                    req.email(),
                    req.street(),
                    req.city(),
                    req.postalCode(),
                    req.destinationCountryCode(),
                    req.originCountryCode(),
                    req.warehouseId(),
                    req.paymentMethod(),
                    itemsList
            );

            return Response.status(Response.Status.CREATED).entity(orderResult).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        }
    }

    public record StatusUpdateRequest(Long orderId, String status) {
    }

    @GET
    @Path("/orders")
    public Response getAllOrders() {
        List<Map<String, Object>> list = inventoryService.getOrdersWithShipments();
        return Response.ok(list).build();
    }

    @POST
    @Path("/orders/status")
    public Response updateOrderStatus(StatusUpdateRequest req) {
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
}
