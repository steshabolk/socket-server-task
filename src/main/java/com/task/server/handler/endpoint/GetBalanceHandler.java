package com.task.server.handler.endpoint;

import com.task.server.dto.ClientRequest;
import com.task.server.dto.ClientResponse;
import com.task.server.dto.response.BalanceResponse;
import com.task.server.service.UserService;
import com.task.server.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class GetBalanceHandler implements EndpointHandler {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    @Override
    public HttpMethod httpMethod() {
        return HttpMethod.GET;
    }

    @Override
    public String requestMapping() {
        return "/money";
    }

    @Override
    public ClientResponse processClientRequest(ClientRequest clientRequest) {
        String login = jwtUtil.parseLoginFromTokenHeader(clientRequest);
        BalanceResponse response = userService.getBalance(login);
        return new ClientResponse(response);
    }
}
