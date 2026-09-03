package com.globaltrade.web.customer;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import com.globaltrade.ejb.interfaces.OrderService;
import com.globaltrade.ejb.interfaces.ProductService;
import com.globaltrade.dto.OrderDTO;
import com.globaltrade.entity.Order;
import com.globaltrade.exception.InventoryException;
import com.globaltrade.exception.OrderException;

@Path("/customer")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CustomerPortalResource {

    @EJB
    private ProductService productService;

    @EJB
    private OrderService orderService;

    @GET
    @Path("/products")
    public Response browseCatalog() {
        return Response.ok(productService.getAllProducts()).build();
    }

    @POST
    @Path("/orders")
    @RolesAllowed({"CUSTOMER", "SYSTEM_ADMIN"})
    public Response placeOrder(OrderDTO dto) throws OrderException, InventoryException {
        Order order = orderService.placeOrder(dto);
        return Response.status(Response.Status.CREATED).entity(order).build();
    }
}
