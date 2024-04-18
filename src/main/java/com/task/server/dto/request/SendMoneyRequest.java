package com.task.server.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SendMoneyRequest(@JsonProperty("to") String toLogin, Double amount) {
}
