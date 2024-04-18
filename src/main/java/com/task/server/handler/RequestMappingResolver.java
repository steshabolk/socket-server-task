package com.task.server.handler;

import com.task.server.handler.endpoint.EndpointHandler;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

@Component
public class RequestMappingResolver {

    private Map<HttpMethod, Map<String, EndpointHandler>> handlers;

    public RequestMappingResolver(List<EndpointHandler> endpointHandlers) {
        initHandlers(endpointHandlers);
    }

    public Optional<EndpointHandler> getEndpointHandler(HttpMethod method, String requestMapping) {
        return Optional.ofNullable(handlers.get(method))
            .map(it -> it.get(requestMapping));
    }

    private void initHandlers(List<EndpointHandler> endpointHandlers) {
        handlers = new HashMap<>();
        endpointHandlers.forEach(it -> {
            if (!handlers.containsKey(it.httpMethod())) {
                handlers.put(it.httpMethod(), new HashMap<>());
            }
            handlers.get(it.httpMethod()).put(it.requestMapping(), it);
        });
    }
}
