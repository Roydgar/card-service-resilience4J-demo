package com.example.card_service.api.model.dto;

import java.time.Instant;

public record ApiErrorResponse(
        int errorCode,
        String message,
        Instant timestamp
) {}