package com.example.card_service.client.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class ScryfallCardsCollectionResponse {
    private List<Card> data;
    @JsonProperty("not_found")
    private List<NotFoundCard> notFound;

    @Data
    public static class Card {
        private String name;
        @JsonProperty("image_uris")
        private ImageURIs imageUris;
    }

    @Data
    public static class ImageURIs {
        private String png;
    }

    @Data
    public static class NotFoundCard {
        private String name;
    }
}