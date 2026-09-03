package com.globaltrade.web.resource;

import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import com.globaltrade.entity.Warehouse;
import com.globaltrade.service.WarehouseService;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Path("/warehouses")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class WarehouseResource {

    @EJB
    private WarehouseService warehouseService;

    public record CreateWarehouseRequest(
            String name,
            String street,
            String city,
            String state,
            String postalCode,
            String countryCode,
            Integer maxCapacity
    ) {
    }

    @GET
    public Response getAllWarehouses() {
        List<Warehouse> warehouses = warehouseService.getAllWarehouses();
        List<Map<String, Object>> result = warehouses.stream().map(w -> Map.<String, Object>of(
                "id", w.getId(),
                "name", w.getName(),
                "maxCapacity", w.getMaxCapacity(),
                "currentCapacity", w.getCurrentCapacity(),
                "street", w.getAddress() != null ? w.getAddress().getStreetLine1() : "",
                "city", w.getAddress() != null ? w.getAddress().getCity() : "",
                "country", (w.getAddress() != null && w.getAddress().getCountry() != null) ? w.getAddress().getCountry().getName() : ""
        )).toList();

        return Response.ok(result).build();
    }

    @GET
    @Path("/{id}")
    public Response getWarehouseById(@PathParam("id") Long id) {
        Optional<Warehouse> optional = warehouseService.getWarehouseById(id);
        if (optional.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Warehouse not found with ID: " + id))
                    .build();
        }
        Warehouse w = optional.get();
        return Response.ok(Map.of(
                "id", w.getId(),
                "name", w.getName(),
                "maxCapacity", w.getMaxCapacity(),
                "currentCapacity", w.getCurrentCapacity(),
                "street", w.getAddress() != null ? w.getAddress().getStreetLine1() : "",
                "city", w.getAddress() != null ? w.getAddress().getCity() : "",
                "country", (w.getAddress() != null && w.getAddress().getCountry() != null) ? w.getAddress().getCountry().getName() : ""
        )).build();
    }

    @POST
    public Response createWarehouse(CreateWarehouseRequest req) {
        if (req == null || req.name() == null || req.name().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Warehouse name is required"))
                    .build();
        }

        Warehouse warehouse = warehouseService.createWarehouse(
                req.name(),
                req.street(),
                req.city(),
                req.state(),
                req.postalCode(),
                req.countryCode(),
                req.maxCapacity()
        );

        return Response.status(Response.Status.CREATED)
                .entity(Map.of(
                        "message", "Warehouse created successfully",
                        "id", warehouse.getId(),
                        "name", warehouse.getName(),
                        "maxCapacity", warehouse.getMaxCapacity(),
                        "currentCapacity", warehouse.getCurrentCapacity()
                ))
                .build();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteWarehouse(@PathParam("id") Long id) {
        boolean deleted = warehouseService.deleteWarehouse(id);
        if (deleted) {
            return Response.ok(Map.of("message", "Warehouse deleted successfully")).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Warehouse not found"))
                    .build();
        }
    }
}
