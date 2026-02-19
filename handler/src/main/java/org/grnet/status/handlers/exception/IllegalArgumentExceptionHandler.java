package org.grnet.status.handlers.exception;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.grnet.status.dtos.InformativeResponse;
import org.jboss.logging.Logger;

@Provider
public class IllegalArgumentExceptionHandler implements ExceptionMapper<IllegalArgumentException> {

    private static final Logger LOG = Logger.getLogger(IllegalArgumentExceptionHandler.class);

    @Override
    public Response toResponse(IllegalArgumentException e) {

        LOG.error("Illegal argument", e);

        var msg = (e.getMessage() == null) ? "" : e.getMessage();

        int httpStatus = Response.Status.BAD_REQUEST.getStatusCode();
        String outMessage = msg;

        if (msg.contains("id to load is required")) {
            outMessage =
                    "Entity load failed. Required id was null (a path/query parameter was missing or incorrectly bound).";
        }

        var response = new InformativeResponse();
        response.code = httpStatus;
        response.message = outMessage;

        return Response.status(httpStatus)
                .entity(response)
                .build();
    }
}
