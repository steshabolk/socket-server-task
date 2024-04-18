package com.task.server.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum HttpHeader {

    CONTENT_TYPE("Content-Type: application/json"),
    CONTENT_LENGTH("Content-Length"),
    ACCESS_TOKEN("X-Access-Token");

    private final String value;
}
