package com.globaltrade.web.vendor;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import com.globaltrade.ejb.interfaces.ProductService;
import com.globaltrade.dto.ProductDTO;
import com.globaltrade.entity.Product;

@Path("/vendor")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class VendorPortalResource {

    @EJB
    private ProductService productService;

    @POST
    @Path("/products")
    @RolesAllowed({"VENDOR_REP", "SYSTEM_ADMIN"})
    public Response createProduct(ProductDTO dto) {
        Product product = productService.createProduct(dto);
        return Response.status(Response.Status.CREATED).entity(product).build();
    }
}
