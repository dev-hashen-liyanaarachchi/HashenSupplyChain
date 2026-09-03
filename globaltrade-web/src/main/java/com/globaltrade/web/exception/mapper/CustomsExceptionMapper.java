package com.globaltrade.web.exception.mapper;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import com.globaltrade.exception.CustomsException;

@Provider
public class CustomsExceptionMapper implements ExceptionMapper<CustomsException> {

    @Override
    public Response toResponse(CustomsException exception) {
        return Response.status(Response.Status.FORBIDDEN)
                .entity(new ErrorMessage(403, "Customs Clearance Violation", exception.getMessage()))
                .build();
    }

    public static record ErrorMessage(int status, String error, String message) {
    }
}
