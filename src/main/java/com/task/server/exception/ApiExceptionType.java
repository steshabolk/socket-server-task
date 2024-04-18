package com.task.server.exception;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum ApiExceptionType {

    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase()),
    REQUEST_MAPPING_NOT_FOUND(HttpStatus.NOT_FOUND, "not found %s %s"),
    VALIDATION_FAILURE(HttpStatus.BAD_REQUEST, "%s"),
    INVALID_REQUEST_LINE(HttpStatus.BAD_REQUEST, "invalid request line"),
    INVALID_CONTENT_LENGTH_HEADER(HttpStatus.BAD_REQUEST, "request body length does not match Content-Length header"),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "jwt token is expired"),
    MISSING_TOKEN(HttpStatus.UNAUTHORIZED, "jwt token is required"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "invalid jwt token"),
    USER_ALREADY_EXISTS(HttpStatus.CONFLICT, "login \"%s\" already registered"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "user \"%s\" not found"),
    WRONG_PASSWORD(HttpStatus.BAD_REQUEST, "wrong password for login \"%s\""),
    INSUFFICIENT_FUNDS(HttpStatus.BAD_REQUEST, "insufficient funds on the balance of user \"%s\"");

    private final HttpStatus httpStatus;
    private final String message;

    public ApiException toException(Object... args) {
        return new ApiException(
            name(),
            httpStatus,
            String.format(message, args)
        );
    }
}
