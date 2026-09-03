package com.globaltrade.web.exception.mapper;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import com.globaltrade.exception.InventoryException;

@Provider
public class InventoryExceptionMapper implements ExceptionMapper<InventoryException> {

    @Override
    public Response toResponse(InventoryException exception) {
        return Response.status(Response.Status.CONFLICT)
                .entity(new ErrorMessage(409, "Inventory Stock Shortage", exception.getMessage()))
                .build();
    }

    public static record ErrorMessage(int status, String error, String message) {
    }
}
