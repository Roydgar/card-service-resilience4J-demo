package com.example.card_service.api.model.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.List;

@Data
public class ImportCardsRequest {
    // TODO: move messages to some external configuration file
    @NotNull(message = "cardNames must not be null")
    @NotEmpty(message = "cardNames must contain at least one card")
    private List<
            @NotNull(message = "cardName must not be null")
            @NotEmpty(message = "cardName must not be empty")
            @Pattern(regexp = "^[\\w\\s-]+$", message = "Invalid characters") String> cardNames;
}
