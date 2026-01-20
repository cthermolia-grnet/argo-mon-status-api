package org.grnet.status.handlers.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.grnet.status.dtos.InformativeResponse;
import org.jboss.logging.Logger;

import java.util.Arrays;
import java.util.stream.Collectors;

@Provider
public class JacksonBadRequestHandler implements ExceptionMapper<InvalidFormatException> {

    private static final Logger LOG = Logger.getLogger(JacksonBadRequestHandler.class);

    @Override
    public Response toResponse(InvalidFormatException e) {

        String message = "Invalid request payload.";

        if (e.getCause() instanceof InvalidFormatException ife && ife.getTargetType().isEnum()) {
            String allowed = Arrays.stream(ife.getTargetType().getEnumConstants())
                    .map(Object::toString)
                    .collect(Collectors.joining(", "));
            message = "Invalid value. Allowed values: " + allowed;
        }

        LOG.warn(message);

        var response = new InformativeResponse();
        response.message = message;
        response.code = 400;

        return Response.status(Response.Status.BAD_REQUEST).entity(response).build();
    }
}
