package com.task.server.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.task.server.dto.ClientRequest;
import com.task.server.dto.ClientResponse;
import com.task.server.exception.ApiException;
import com.task.server.exception.ApiExceptionType;
import com.task.server.exception.DbException;
import com.task.server.handler.endpoint.EndpointHandler;
import com.task.server.util.ClientRequestParser;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class ClientRequestHandler {

    private final ClientResponseHandler responseHandler;
    private final RequestMappingResolver requestMappingResolver;

    public void handleClientRequest(BufferedReader reader, BufferedWriter writer) throws IOException {
        try {
            ClientRequest clientRequest = ClientRequestParser.parseClientRequest(reader);
            if (clientRequest == null) {
                return;
            }
            log.debug(
                "client request: {} {}\nheaders: {}\nbody: {}",
                clientRequest.method(),
                clientRequest.requestMapping(),
                clientRequest.headers(),
                clientRequest.body()
            );
            EndpointHandler endpointHandler = getEndpointHandler(clientRequest);
            ClientResponse clientResponse = endpointHandler.processClientRequest(clientRequest);
            responseHandler.handleClientResponse(writer, clientResponse);
        } catch (ApiException e) {
            responseHandler.handleExceptionResponse(writer, e);
        } catch (JsonProcessingException e) {
            responseHandler.handleExceptionResponse(
                writer,
                ApiExceptionType.VALIDATION_FAILURE.toException("invalid request body")
            );
        } catch (IOException | DbException e) {
            log.info("{}", e.getMessage());
            responseHandler.handleExceptionResponse(writer, ApiExceptionType.INTERNAL_SERVER_ERROR.toException());
        }
    }

    private EndpointHandler getEndpointHandler(ClientRequest clientRequest) {
        Optional<EndpointHandler> endpointHandler = requestMappingResolver.getEndpointHandler(
            clientRequest.method(), clientRequest.requestMapping());
        if (endpointHandler.isEmpty()) {
            throw ApiExceptionType.REQUEST_MAPPING_NOT_FOUND.toException(
                clientRequest.method(),
                clientRequest.requestMapping()
            );
        }
        return endpointHandler.get();
    }
}
