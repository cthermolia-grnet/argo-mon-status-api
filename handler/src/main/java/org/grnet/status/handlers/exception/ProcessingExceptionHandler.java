package org.grnet.status.handlers.exception;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.grnet.status.dtos.InformativeResponse;
import org.jboss.logging.Logger;

@Provider
public class ProcessingExceptionHandler implements ExceptionMapper<ProcessingException> {

    private static final Logger LOG = Logger.getLogger(ProcessingExceptionHandler.class);

    @Override
    public Response toResponse(ProcessingException e) {

        // log full stacktrace
        LOG.error("ProcessingException while calling remote service", e);

        var response = new InformativeResponse();
        response.code = 502;
        response.message = buildMessage(e);

        return Response.status(Response.Status.BAD_GATEWAY)
                .entity(response)
                .build();
    }

    private String buildMessage(Throwable e) {
        // Keep user output clean and stable. No netty/jdk noise.
        var root = rootCause(e);
        var msg = (root.getMessage() == null) ? "" : root.getMessage().toLowerCase();

        if (msg.contains("connection refused")) {
            return "Remote service is unreachable (connection refused). Check the target URL.";
        }
        if (msg.contains("no route to host")) {
            return "Remote service is unreachable (no route to host). Check network access and DNS.";
        }
        if (msg.contains("ssl") || msg.contains("handshake")) {
            return "TLS handshake failed when connecting to the remote service. Check HTTPS configuration and certificates.";
        }
        if (msg.contains("timed out") || msg.contains("timeout")) {
            return "Remote service did not respond in time (timeout).";
        }

        return "Failed to reach remote service.";
    }

    private Throwable rootCause(Throwable t) {
        Throwable cur = t;
        int guard = 0;
        while (cur.getCause() != null && cur.getCause() != cur && guard++ < 20) {
            cur = cur.getCause();
        }
        return cur;
    }
}
