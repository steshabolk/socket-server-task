package com.task.server.util;

import com.task.server.dto.ClientRequest;
import com.task.server.enums.HttpHeader;
import com.task.server.exception.ApiExceptionType;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import lombok.experimental.UtilityClass;
import org.springframework.http.HttpMethod;
import org.springframework.util.StringUtils;

@UtilityClass
public class ClientRequestParser {

    public static ClientRequest parseClientRequest(BufferedReader reader) throws IOException {
        String[] requestLine = parseRequestLine(reader);
        if (requestLine == null) {
            return null;
        }
        Map<String, String> headers = parseHeaders(reader);
        String body = parseBody(reader, headers.get(HttpHeader.CONTENT_LENGTH.getValue()));
        return buildClientRequest(requestLine, headers, body);
    }

    private static String[] parseRequestLine(BufferedReader reader) throws IOException {
        String requestLine = reader.readLine();
        if (!StringUtils.hasText(requestLine)) {
            return null;
        }
        String[] arr = requestLine.split(" ");
        if (arr.length != 3) {
            throw ApiExceptionType.INVALID_REQUEST_LINE.toException();
        }
        return arr;
    }

    private static Map<String, String> parseHeaders(BufferedReader reader) throws IOException {
        Map<String, String> headers = new HashMap<>();
        String line;
        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            String[] header = line.split(":");
            if (header.length == 2) {
                headers.put(header[0].trim(), header[1].trim());
            }
        }
        return headers;
    }

    private static String parseBody(BufferedReader reader, String contentLengthHeader) throws IOException {
        String body = null;
        if (contentLengthHeader != null) {
            int contentLength = Integer.parseInt(contentLengthHeader);
            if (contentLength > 0) {
                char[] chars = new char[contentLength];
                int readied = reader.read(chars, 0, contentLength);
                if (readied != contentLength) {
                    throw ApiExceptionType.INVALID_CONTENT_LENGTH_HEADER.toException();
                }
                body = new String(chars);
            }
        }
        return body;
    }

    private static ClientRequest buildClientRequest(String[] requestLine, Map<String, String> headers, String body) {
        return new ClientRequest(HttpMethod.valueOf(requestLine[0]), requestLine[1], requestLine[2], headers, body);
    }
}
