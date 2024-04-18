package com.task.server.handler.endpoint;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.task.server.dto.ClientRequest;
import com.task.server.dto.ClientResponse;
import com.task.server.dto.request.SendMoneyRequest;
import com.task.server.dto.response.BalanceResponse;
import com.task.server.service.UserService;
import com.task.server.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class SendMoneyHandler implements EndpointHandler {

    private final ObjectMapper mapper;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    @Override
    public HttpMethod httpMethod() {
        return HttpMethod.POST;
    }

    @Override
    public String requestMapping() {
        return "/money";
    }

    @Override
    public ClientResponse processClientRequest(ClientRequest clientRequest) throws JsonProcessingException {
        String login = jwtUtil.parseLoginFromTokenHeader(clientRequest);
        SendMoneyRequest request = mapper.readValue(clientRequest.body(), SendMoneyRequest.class);
        BalanceResponse response = userService.sendMoney(login, request);
        return new ClientResponse(response);
    }
}
