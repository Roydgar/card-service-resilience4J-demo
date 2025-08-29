package com.example.card_service.mapper;

import com.example.card_service.api.model.dto.GetCardsResponse;
import com.example.card_service.client.model.dto.ScryfallCardsCollectionRequest;
import com.example.card_service.repository.model.CardImportOperationEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class CardRequestResponseMapper {
    public GetCardsResponse toImportCardsResponse(final CardImportOperationEntity operation) {
        List<GetCardsResponse.CardImportResult> results = operation.getCards().stream()
                .map(card -> GetCardsResponse.CardImportResult.builder()
                        .name(card.getCardName())
                        .pngUrl(card.getPngUrl())
                        .build())
                .toList();

        List<GetCardsResponse.CardImportFailure> failures = operation.getCardImportErrors().stream()
                .map(card -> GetCardsResponse.CardImportFailure.builder()
                        .name(card.getCardName())
                        .error(card.getErrorDescription())
                        .build())
                .toList();


        return GetCardsResponse.builder()
                .operationId(operation.getOperationId())
                .status(operation.getStatus().toString())
                .results(results)
                .failures(failures)
                .build();
    }

    public ScryfallCardsCollectionRequest toCardsCollectionRequest(final List<String> cardNames) {
        if (CollectionUtils.isEmpty(cardNames)) {
            return new ScryfallCardsCollectionRequest(Collections.emptyList());
        }

        List<ScryfallCardsCollectionRequest.Identifier> identifiers = cardNames.stream()
                .map(ScryfallCardsCollectionRequest.Identifier::new)
                .toList();

        return new ScryfallCardsCollectionRequest(identifiers);
    }
}
