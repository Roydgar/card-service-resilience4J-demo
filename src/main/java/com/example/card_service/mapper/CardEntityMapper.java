package com.example.card_service.mapper;

import com.example.card_service.client.model.dto.ScryfallCardsCollectionResponse;
import com.example.card_service.repository.model.CardEntity;
import com.example.card_service.repository.model.CardImportErrorEntity;
import com.example.card_service.repository.model.CardImportOperationEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class CardEntityMapper {

    public List<CardEntity> toCardEntities(final List<ScryfallCardsCollectionResponse.Card> cards,
                                           final CardImportOperationEntity cardImportOperation) {
        return cards.stream()
                .map(card -> toCardEntity(card, cardImportOperation))
                .toList();
    }

    public List<CardImportErrorEntity> namesToCardImportErrorEntities(final List<String> cardNames,
                                                                      final CardImportOperationEntity cardImportOperation,
                                                                      final String errorDescription) {
        return cardNames.stream()
                .map(card -> toCardImportErrorEntity(card, cardImportOperation, errorDescription))
                .toList();
    }

    public List<CardImportErrorEntity> toCardImportErrorEntities(final List<ScryfallCardsCollectionResponse.NotFoundCard> cards,
                                                                 final CardImportOperationEntity cardImportOperation,
                                                                 final String errorDescription) {
        return cards.stream()
                .map(card -> toCardImportErrorEntity(card.getName(), cardImportOperation, errorDescription))
                .toList();
    }

    public CardEntity toCardEntity(final ScryfallCardsCollectionResponse.Card card,
                                   final CardImportOperationEntity cardImportOperation) {
        CardEntity cardEntity = new CardEntity();
        cardEntity.setId(UUID.randomUUID());
        cardEntity.setCardName(card.getName());
        cardEntity.setPngUrl(card.getImageUris() != null ? card.getImageUris().getPng() : null);
        cardEntity.setOperation(cardImportOperation);
        return cardEntity;
    }

    public CardImportErrorEntity toCardImportErrorEntity(final String cardName,
                                                         final CardImportOperationEntity cardImportOperation,
                                                         final String errorDescription) {
        CardImportErrorEntity cardEntity = new CardImportErrorEntity();
        cardEntity.setId(UUID.randomUUID());
        cardEntity.setCardName(cardName);
        cardEntity.setErrorDescription(errorDescription);
        cardEntity.setOperation(cardImportOperation);
        return cardEntity;
    }
}
