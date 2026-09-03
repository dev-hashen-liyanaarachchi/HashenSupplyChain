package com.globaltrade.web.customs;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import com.globaltrade.ejb.interfaces.CustomsService;
import com.globaltrade.entity.CustomsDocument;
import com.globaltrade.exception.CustomsException;

@Path("/customs")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CustomsPortalResource {

    @EJB
    private CustomsService customsService;

    @POST
    @Path("/inspect/{documentId}")
    @RolesAllowed({"CUSTOMS_AGENT", "SYSTEM_ADMIN"})
    public Response inspectCustomsDocument(@PathParam("documentId") Long documentId, @QueryParam("inspector") String inspector) throws CustomsException {
        CustomsDocument doc = customsService.inspectAndApprove(documentId, inspector);
        return Response.ok(doc).build();
    }
}
