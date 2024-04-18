package com.task.server.exception;

public record ApiExceptionResponse(String code, int status, String message) {
}
