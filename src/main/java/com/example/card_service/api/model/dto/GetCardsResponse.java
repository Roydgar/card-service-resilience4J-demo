package com.example.card_service.api.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetCardsResponse {
    private UUID operationId;
    private String status;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<CardImportResult> results;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<CardImportFailure> failures;

    @Data
    @Builder
    public static class CardImportResult {
        private String name;
        private String pngUrl;
    }

    @Data
    @Builder
    public static class CardImportFailure {
        private String name;
        private String error;
    }
}
