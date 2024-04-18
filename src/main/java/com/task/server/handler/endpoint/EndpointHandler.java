package com.task.server.handler.endpoint;

import com.task.server.dto.ClientRequest;
import com.task.server.dto.ClientResponse;
import java.io.IOException;
import org.springframework.http.HttpMethod;

public interface EndpointHandler {

    HttpMethod httpMethod();

    String requestMapping();

    ClientResponse processClientRequest(ClientRequest clientRequest) throws IOException;
}
