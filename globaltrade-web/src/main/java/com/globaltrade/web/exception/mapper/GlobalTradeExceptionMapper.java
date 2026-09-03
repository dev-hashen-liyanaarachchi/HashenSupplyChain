package com.globaltrade.web.exception.mapper;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import com.globaltrade.exception.GlobalTradeException;

@Provider
public class GlobalTradeExceptionMapper implements ExceptionMapper<GlobalTradeException> {

    @Override
    public Response toResponse(GlobalTradeException exception) {
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ErrorMessage(400, "Business Processing Exception", exception.getMessage()))
                .build();
    }

    public static record ErrorMessage(int status, String error, String message) {}
}
