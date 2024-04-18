package com.task.server.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class DbException extends RuntimeException {
    private final String message;
}
