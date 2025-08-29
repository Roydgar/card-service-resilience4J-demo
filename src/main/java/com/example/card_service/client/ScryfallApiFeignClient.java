package com.example.card_service.client;

import com.example.card_service.client.model.dto.ScryfallCardsCollectionResponse;
import com.example.card_service.client.model.dto.ScryfallCardsCollectionRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "scryfall", url = "${scryfall.base-url}")
interface ScryfallApiFeignClient {

    @PostMapping(value = "/cards/collection", consumes = MediaType.APPLICATION_JSON_VALUE)
    ScryfallCardsCollectionResponse getCardsCollection(@RequestBody ScryfallCardsCollectionRequest request);
}