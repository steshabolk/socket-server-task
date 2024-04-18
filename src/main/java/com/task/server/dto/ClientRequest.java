package com.task.server.dto;

import java.util.Map;
import org.springframework.http.HttpMethod;

public record ClientRequest(HttpMethod method, String requestMapping, String httpVersion,
                            Map<String, String> headers, String body) {
}
