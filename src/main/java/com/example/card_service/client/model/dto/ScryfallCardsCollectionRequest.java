package com.example.card_service.client.model.dto;

import java.util.List;

public record ScryfallCardsCollectionRequest(List<Identifier> identifiers) {
    public record Identifier(String name) {
    }
}