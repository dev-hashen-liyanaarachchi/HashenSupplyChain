package com.globaltrade.web.resource;

import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import com.globaltrade.entity.*;
import com.globaltrade.service.InventoryService;

import java.util.List;
import java.util.Map;

@Path("/inventory")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class InventoryResource {

    @EJB
    private InventoryService inventoryService;

    public record CreateProductRequest(
            String sku,
            String name,
            Double price,
            Double weightKg,
            String hsCode,
            String description,
            Long categoryId,
            Long brandId
    ) {
    }

    public record AddInventoryRequest(
            Long warehouseId,
            Long productId,
            Long vendorId,
            Double unitPrice,
            Integer availableQty,
            Integer reorderThreshold
    ) {
    }

    public record SimpleNamedRequest(String name) {
    }

    @GET
    @Path("/hscodes")
    public Response getAllHsCodes() {
        List<com.globaltrade.enums.HsCodeCategory> list = inventoryService.getAllHsCodes();
        List<Map<String, Object>> result = list.stream().map(h -> Map.<String, Object>of(
                "id", (long) (h.ordinal() + 1),
                "code", h.getCode(),
                "descriptionType", h.getDescription(),
                "dutyRate", h.getDutyRate()
        )).toList();
        return Response.ok(result).build();
    }

    @GET
    @Path("/vendors")
    public Response getAllVendors() {
        List<Vendor> list = inventoryService.getAllVendors();
        List<Map<String, Object>> result = list.stream().map(v -> Map.<String, Object>of(
                "id", v.getId(),
                "companyName", v.getCompanyName(),
                "taxId", v.getTaxIdentificationNumber()
        )).toList();
        return Response.ok(result).build();
    }

    @GET
    @Path("/categories")
    public Response getAllCategories() {
        List<Category> list = inventoryService.getAllCategories();
        List<Map<String, Object>> result = list.stream().map(c -> Map.<String, Object>of(
                "id", c.getId(),
                "name", c.getName()
        )).toList();
        return Response.ok(result).build();
    }

    @POST
    @Path("/categories")
    public Response createCategory(SimpleNamedRequest req) {
        if (req == null || req.name() == null || req.name().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", "Category name is required")).build();
        }
        Category cat = inventoryService.createCategory(req.name());
        return Response.status(Response.Status.CREATED).entity(Map.of("id", cat.getId(), "name", cat.getName())).build();
    }

    @GET
    @Path("/brands")
    public Response getAllBrands() {
        List<Brand> list = inventoryService.getAllBrands();
        List<Map<String, Object>> result = list.stream().map(b -> Map.<String, Object>of(
                "id", b.getId(),
                "name", b.getName()
        )).toList();
        return Response.ok(result).build();
    }

    @POST
    @Path("/brands")
    public Response createBrand(SimpleNamedRequest req) {
        if (req == null || req.name() == null || req.name().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", "Brand name is required")).build();
        }
        Brand brand = inventoryService.createBrand(req.name());
        return Response.status(Response.Status.CREATED).entity(Map.of("id", brand.getId(), "name", brand.getName())).build();
    }

    @GET
    @Path("/products")
    public Response getAllProducts() {
        List<Product> products = inventoryService.getAllProducts();
        List<Map<String, Object>> result = products.stream().map(p -> {
            java.util.Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", p.getId());
            map.put("sku", p.getSku());
            map.put("name", p.getName());
            map.put("price", p.getPrice());
            map.put("weightKg", p.getWeightKg() != null ? p.getWeightKg() : 1.0);
            map.put("hsCode", p.getHsCode() != null ? p.getHsCode() : "");
            map.put("description", p.getDescription() != null ? p.getDescription() : "");
            map.put("categoryName", p.getCategory() != null ? p.getCategory().getName() : "General Category");
            map.put("brandName", p.getBrand() != null ? p.getBrand().getName() : "Global Brand");
            return map;
        }).toList();
        return Response.ok(result).build();
    }

    @POST
    @Path("/products")
    public Response createProduct(CreateProductRequest req) {
        if (req == null || req.name() == null || req.name().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Product name is required"))
                    .build();
        }

        try {
            Product product = inventoryService.createProduct(
                    req.sku(),
                    req.name(),
                    req.price(),
                    req.weightKg(),
                    req.hsCode(),
                    req.description(),
                    req.categoryId(),
                    req.brandId()
            );

            return Response.status(Response.Status.CREATED)
                    .entity(Map.of(
                            "message", "Product created successfully",
                            "id", product.getId(),
                            "sku", product.getSku(),
                            "name", product.getName(),
                            "price", product.getPrice(),
                            "weightKg", product.getWeightKg(),
                            "hsCode", product.getHsCode()
                    ))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/items")
    public Response getAllInventories() {
        List<Inventory> inventories = inventoryService.getAllInventories();
        List<Map<String, Object>> result = inventories.stream().map(i -> {
            java.util.Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", i.getId());
            map.put("warehouseName", i.getWarehouse() != null ? i.getWarehouse().getName() : "Unknown Warehouse");
            map.put("productSku", i.getProduct() != null ? i.getProduct().getSku() : "Unknown SKU");
            map.put("productName", i.getProduct() != null ? i.getProduct().getName() : "Unknown Product");
            map.put("vendorName", i.getVendor() != null ? i.getVendor().getCompanyName() : "Default Vendor");
            map.put("unitPrice", i.getUnitPrice() != null ? i.getUnitPrice() : 0.0);
            map.put("weightKg", i.getProduct() != null && i.getProduct().getWeightKg() != null ? i.getProduct().getWeightKg() : 1.0);
            map.put("hsCode", i.getProduct() != null ? i.getProduct().getHsCode() : "");
            map.put("availableQty", i.getAvailableQty());
            map.put("reservedQty", i.getReservedQty());
            map.put("reorderThreshold", i.getReorderThreshold());
            return map;
        }).toList();
        return Response.ok(result).build();
    }

    @POST
    @Path("/items")
    public Response addInventory(AddInventoryRequest req) {
        if (req == null || req.warehouseId() == null || req.productId() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Warehouse ID and Product ID are required"))
                    .build();
        }

        try {
            Inventory inventory = inventoryService.addInventory(
                    req.warehouseId(),
                    req.productId(),
                    req.vendorId(),
                    req.unitPrice(),
                    req.availableQty(),
                    req.reorderThreshold()
            );

            return Response.status(Response.Status.CREATED)
                    .entity(Map.of(
                            "message", "Inventory stock allocated successfully",
                            "id", inventory.getId(),
                            "warehouse", inventory.getWarehouse().getName(),
                            "product", inventory.getProduct().getName(),
                            "vendor", inventory.getVendor() != null ? inventory.getVendor().getCompanyName() : "Vendor",
                            "unitPrice", inventory.getUnitPrice(),
                            "availableQty", inventory.getAvailableQty(),
                            "reorderThreshold", inventory.getReorderThreshold()
                    ))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/transactions")
    public Response getAllTransactions() {
        List<com.globaltrade.entity.InventoryTransaction> list = inventoryService.getAllInventoryTransactions();
        List<Map<String, Object>> result = list.stream().map(t -> {
            Map<String, Object> m = new java.util.HashMap<>();
            m.put("id", t.getId());
            m.put("inventoryId", t.getInventory() != null ? t.getInventory().getId() : 0);
            m.put("productName", (t.getInventory() != null && t.getInventory().getProduct() != null) ? t.getInventory().getProduct().getName() : "Product Cargo");
            m.put("warehouseName", (t.getInventory() != null && t.getInventory().getWarehouse() != null) ? t.getInventory().getWarehouse().getName() : "Logistics Hub");
            m.put("transactionType", t.getTransactionType() != null ? t.getTransactionType().name() : "STOCK_IN");
            m.put("quantityChanged", t.getQuantityChanged() != null ? t.getQuantityChanged() : 0);
            m.put("performedBy", t.getPerformedBy() != null ? t.getPerformedBy() : "System Logistics Agent");
            m.put("timestamp", t.getTimestamp() != null ? t.getTimestamp().toString() : "Recent");
            return m;
        }).toList();

        return Response.ok(result).build();
    }
}
