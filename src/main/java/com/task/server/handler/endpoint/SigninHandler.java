package com.task.server.handler.endpoint;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.task.server.dto.ClientRequest;
import com.task.server.dto.ClientResponse;
import com.task.server.dto.request.SigninRequest;
import com.task.server.dto.response.TokenResponse;
import com.task.server.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class SigninHandler implements EndpointHandler {

    private final ObjectMapper mapper;
    private final UserService userService;

    @Override
    public HttpMethod httpMethod() {
        return HttpMethod.POST;
    }

    @Override
    public String requestMapping() {
        return "/signin";
    }

    @Override
    public ClientResponse processClientRequest(ClientRequest clientRequest) throws JsonProcessingException {
        SigninRequest request = mapper.readValue(clientRequest.body(), SigninRequest.class);
        TokenResponse response = userService.login(request);
        return new ClientResponse(response);
    }
}
