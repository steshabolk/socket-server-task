package com.task.server.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public class ApiException extends RuntimeException {
    private final String code;
    private final HttpStatus status;
    private final String message;
}
