package com.task.server.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.task.server.dto.ClientResponse;
import com.task.server.enums.HttpHeader;
import com.task.server.exception.ApiException;
import com.task.server.exception.ApiExceptionResponse;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class ClientResponseHandler {

    private static final String HTTP_VERSION = "HTTP/1.1";
    private final ObjectMapper mapper;

    public void handleClientResponse(BufferedWriter writer, ClientResponse clientResponse) throws IOException {
        writeResponse(writer, HttpStatus.OK, clientResponse.body());
    }

    public void handleExceptionResponse(BufferedWriter writer, ApiException e) throws IOException {
        ApiExceptionResponse response =
            new ApiExceptionResponse(e.getCode(), e.getStatus().value(), e.getMessage());
        log.info("{}", response);
        writeResponse(writer, e.getStatus(), response);
    }

    private void writeResponse(BufferedWriter writer, HttpStatus status, Object body) throws IOException {
        String responseBody = buildBody(body);
        writer.write(buildStatusLine(status));
        writer.newLine();
        writer.write(buildGeneralHeaders());
        writer.newLine();
        if (responseBody != null) {
            writer.write(buildBodyHeaders(responseBody.getBytes(StandardCharsets.UTF_8).length));
            writer.newLine();
            writer.newLine();
            writer.write(responseBody);
        } else {
            writer.newLine();
        }
        writer.flush();
    }

    private String buildStatusLine(HttpStatus status) {
        return String.format("%s %s %s", HTTP_VERSION, status.value(), status.getReasonPhrase());
    }

    private String buildGeneralHeaders() {
        return "Date: " + DateTimeFormatter
            .ofPattern("EEE, dd MMM yyyy HH:mm:ss O")
            .withZone(ZoneOffset.UTC)
            .withLocale(Locale.US)
            .format(Instant.now());
    }

    private String buildBodyHeaders(int length) {
        return HttpHeader.CONTENT_TYPE.getValue() + "\n" + buildContentLengthHeader(length);
    }

    private String buildContentLengthHeader(int length) {
        return String.format("%s: %s", HttpHeader.CONTENT_LENGTH.getValue(), length);
    }

    private String buildBody(Object body) throws JsonProcessingException {
        String responseBody = null;
        if (body != null) {
            responseBody = mapper.writeValueAsString(body);
        }
        return responseBody;
    }
}
