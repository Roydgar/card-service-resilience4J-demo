package com.example.card_service.service;

import com.example.card_service.client.ScryfallApiClient;
import com.example.card_service.client.model.dto.ScryfallCardsCollectionRequest;
import com.example.card_service.client.model.dto.ScryfallCardsCollectionResponse;
import com.example.card_service.mapper.CardEntityMapper;
import com.example.card_service.mapper.CardRequestResponseMapper;
import com.example.card_service.repository.CardImportErrorRepository;
import com.example.card_service.repository.CardImportOperationRepository;
import com.example.card_service.repository.CardRepository;
import com.example.card_service.repository.model.CardEntity;
import com.example.card_service.repository.model.CardImportErrorEntity;
import com.example.card_service.repository.model.CardImportOperationEntity;
import com.example.card_service.repository.model.CardImportOperationEntity.Status;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class CardAsyncImportService implements  CardImportService {
    // TODO: make it configurable if needed
    // API limitation - max 75 cards per request
    private static final int BATCH_SIZE = 75;
    public static final String CARD_NOT_FOUND_MESSAGE = "Card not found in Scryfall API";

    private final ScryfallApiClient scryfallApiClient;
    private final CardRepository cardRepository;
    private final CardImportErrorRepository cardImportErrorRepository;
    private final CardImportOperationRepository cardImportOperationRepository;
    private final CardEntityMapper entityMapper;
    private final CardRequestResponseMapper requestMapper;

    // Can be avoided by using a separate service for the @Transactional method
    private @Lazy @Autowired CardAsyncImportService self;

    /**
     * Imports a list of cards asynchronously and persists the results.
     *
     * <p><b>Transaction flow:</b>
     * <ol>
     *     <li>{@link #createOperation(UUID)} atomically creates an operation record exists even if the import fails.</li>
     *     <li>{@link #doImport(List, CardImportOperationEntity)} always runs in its own transaction, fetches the cards
     *          from Scryfall API and commits all the results.  If transaction fails, it rollbacks and operation status
     *          is updated to FAILURE. If it succeeds, the operation status is resolved based on the results and updated.
     *     </li>
     *     <li>In this way, the operation record is always created and its status is always updated to reflect the final state of the import.</li>
     * </ol>
     *
     *
     * @param cardNames   the list of card names to import
     * @param operationId the unique identifier of the card import operation
     */
    @Async
    @Override
    public void importCardsByNames(List<String> cardNames, UUID operationId) {
        CardImportOperationEntity operation = createOperation(operationId);

        try {
            Status status = self.doImport(cardNames, operation);
            operation.setStatus(status);
            cardImportOperationRepository.save(operation);
        } catch (RuntimeException e) {
            log.error("Unexpected error occurred during card import operation {}", operationId, e);
            operation.setStatus(Status.FAILURE);
            cardImportOperationRepository.save(operation);
        }
    }

    private CardImportOperationEntity createOperation(UUID operationId) {
        CardImportOperationEntity operation = new CardImportOperationEntity();
        operation.setOperationId(operationId);
        operation.setStatus(Status.PROCESSING);
        return cardImportOperationRepository.save(operation);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Status doImport(List<String> cardNames, CardImportOperationEntity operationEntity) {
        List<CardEntity> cardEntities = new ArrayList<>();
        List<CardImportErrorEntity> errorEntities = new ArrayList<>();

        for (List<String> batchOfCardNames : ListUtils.partition(cardNames, BATCH_SIZE)) {
            ScryfallCardsCollectionRequest request = requestMapper.toCardsCollectionRequest(batchOfCardNames);

            try {
                ScryfallCardsCollectionResponse response = scryfallApiClient.getCardsCollection(request);

                if (!CollectionUtils.isEmpty(response.getData())) {
                    cardEntities.addAll(entityMapper.toCardEntities(response.getData(), operationEntity));
                }

                if (!CollectionUtils.isEmpty(response.getNotFound())) {
                    errorEntities.addAll(entityMapper.toCardImportErrorEntities(response.getNotFound(), operationEntity, CARD_NOT_FOUND_MESSAGE));
                }

            } catch (FeignException | CallNotPermittedException e) {
                log.error("Failed to connect to Scryfall API for batch of {} cards", batchOfCardNames.size(), e);
                // TODO: persist more detailed error messages if needed depending on error status code if needed
                // WARNING: EXCEPTION MESSAGE EXPOSES INTERNAL ERROR DETAILS, CONSIDER SECURITY IMPLICATIONS!!!
                // DO NOT RETURN THE EXCEPTION ITSELF TO THE CLIENT AS IT MAY CONTAIN SENSITIVE INFORMATION
                errorEntities.addAll(entityMapper.namesToCardImportErrorEntities(batchOfCardNames, operationEntity, e.getMessage()));
            }
        }

        cardRepository.saveAll(cardEntities);
        cardImportErrorRepository.saveAll(errorEntities);

        Status status = resolveStatus(cardEntities, errorEntities);
        log.info("Card import operation {} completed with status: {}. Number of found cards: {}. " +
                "Failed to import {} card(s)", operationEntity.getOperationId(), status, cardEntities.size(), errorEntities.size());
        return status;
    }

    private Status resolveStatus(List<CardEntity> cardEntities, List<CardImportErrorEntity> errorEntities) {
        if (cardEntities.isEmpty()) {
            return Status.FAILURE;
        }

        return errorEntities.isEmpty() ? Status.SUCCESS : Status.PARTIAL_SUCCESS;
    }
}
