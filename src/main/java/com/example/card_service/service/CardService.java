package com.example.card_service.service;

import com.example.card_service.exceptions.CardImportOperationNotFoundException;
import com.example.card_service.repository.CardImportOperationRepository;
import com.example.card_service.repository.model.CardImportOperationEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CardService {
    private final CardImportOperationRepository cardImportOperationRepository;
    private final CardAsyncImportService cardAsyncImportService;

    public UUID importCards(List<String> cardNames) {
        UUID operationId = UUID.randomUUID();
        log.info("Importing {} cards with operation ID: {}", cardNames.size(), operationId);

        cardAsyncImportService.importCardsByNames(cardNames, operationId);

        return operationId;
    }

    public CardImportOperationEntity getCards(UUID operationId) {
        log.info("Getting cards by operation id: {}", operationId);
        return cardImportOperationRepository.getByOperationId(operationId)
                .orElseThrow(() -> new CardImportOperationNotFoundException("No import operation found for ID: " + operationId));
    }
}
