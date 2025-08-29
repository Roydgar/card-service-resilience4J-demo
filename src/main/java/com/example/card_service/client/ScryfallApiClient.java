package com.example.card_service.client;

import com.example.card_service.client.model.dto.ScryfallCardsCollectionRequest;
import com.example.card_service.client.model.dto.ScryfallCardsCollectionResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScryfallApiClient {

    private final ScryfallApiFeignClient feignClient;

    @Retry(name = "scryfall-api-retry")
    @CircuitBreaker(name = "scryfall-circuit-breaker")
    @RateLimiter(name = "scryfall-rate-limiter")
    public ScryfallCardsCollectionResponse getCardsCollection(ScryfallCardsCollectionRequest request) {
        log.info("Making a call to Scryfall API to get {} cards", request.identifiers().size());
        return feignClient.getCardsCollection(request);
    }

}
