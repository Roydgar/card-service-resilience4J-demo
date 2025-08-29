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
public class CardAsyncImportService {
    private static final int BATCH_SIZE = 75;
    public static final String CARD_NOT_FOUND_MESSAGE = "Card not found in Scryfall API";

    private final ScryfallApiClient scryfallApiClient;
    private final CardRepository cardRepository;
    private final CardImportErrorRepository cardImportErrorRepository;
    private final CardImportOperationRepository cardImportOperationRepository;
    private final CardEntityMapper entityMapper;
    private final CardRequestResponseMapper requestMapper;

    private @Lazy @Autowired CardAsyncImportService self;

    /**
     * Imports a list of cards asynchronously and persists the results.
     *
     * <p><b>Transaction flow:</b>
     * <ol>
     *     <li>{@link #createOperation(UUID)} is executed in a separate transaction and is always committed,
     *         ensuring that the operation record exists even if the import fails.</li>
     *     <li>{@code importCardsByNames()} runs in the main transaction. If this transaction fails,
     *         all changes within it are rolled back, but the operation record remains.</li>
     * </ol>
     *
     * <p>After successful completion, the operation status is updated accordingly.
     * This design guarantees that there is always a record of the operation, providing visibility
     * into whether the import succeeded, partially succeeded, or failed.
     *
     * (!!!!!!) Design consideration: In case of main transaction failure, the operation status remains "PROCESSING".
     * This could be improved by implementing a mechanism to periodically check and update the status of
     * long-running operations and generate some alerts
     *
     * @param cardNames   the list of card names to import
     * @param operationId the unique identifier of the card import operation
     */

    // Alternate approach to consider: Utilize non-blocking WebFlux client based on Project reactor to handle asynchronous calls more efficiently.
    // This would allow better resource utilization and scalability, especially under high load.
    // However, this would require significant changes to the existing synchronous codebase and careful handling of reactive streams.
    // Reactive programming introduces complexity in development, debugging, and maintenance, so it should be adopted only if the benefits outweigh these challenges.
    // For simplicity and maintainability, the current approach using @Async is chosen. Moreover, only 1 - 100 card names are typically imported at once (basically two API calls),
    // so the performance gain from a reactive approach may be negligible in this context.
    @Async
    @Transactional
    public void importCardsByNames(List<String> cardNames, UUID operationId) {
        CardImportOperationEntity operationEntity = self.createOperation(operationId);

        List<CardEntity> cardEntities = new ArrayList<>();
        List<CardImportErrorEntity> errorEntities = new ArrayList<>();

        for (List<String> batchOfCardNames : ListUtils.partition(cardNames, BATCH_SIZE)) {
            ScryfallCardsCollectionRequest request = requestMapper.toCardsCollectionRequest(batchOfCardNames);

            try {
                ScryfallCardsCollectionResponse response = scryfallApiClient.getCardsCollection(request);

                // TODO: refactor
                if (!CollectionUtils.isEmpty(response.getData())) {
                    cardEntities.addAll(entityMapper.toCardEntities(response.getData(), operationEntity));
                }

                if (!CollectionUtils.isEmpty(response.getNotFound())) {
                    errorEntities.addAll(entityMapper.toCardImportErrorEntities(response.getNotFound(), operationEntity, CARD_NOT_FOUND_MESSAGE));
                }

                // TODO: persist more detailed error messages if needed depending on error status code if needed
            } catch (FeignException | CallNotPermittedException e) {
                log.error("Failed to connect to Scryfall API for batch of {} cards", batchOfCardNames.size(), e);
                // WARNING: EXCEPTION MESSAGE EXPOSES INTERNAL ERROR DETAILS, CONSIDER SECURITY IMPLICATIONS!!!
                // DO NOT SAVE THE EXCEPTION ITSELF, AS IT MAY CONTAIN SENSITIVE INFORMATION
                errorEntities.addAll(entityMapper.namesToCardImportErrorEntities(batchOfCardNames, operationEntity, e.getMessage()));
            }
        }

        cardRepository.saveAll(cardEntities);
        cardImportErrorRepository.saveAll(errorEntities);

        Status status = resolveStatus(cardEntities, errorEntities);
        log.info("Card import operation {} completed with status: {}. Number of found cards: {}. " +
                "Number of cards that were not found: {}", operationId, status, cardEntities.size(), errorEntities.size());

        operationEntity.setStatus(status);
        cardImportOperationRepository.save(operationEntity);
    }

    // Execute in a new transaction to avoid rollback in case of main transaction failure, so there is always a operation record
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public CardImportOperationEntity createOperation(UUID operationId) {
        CardImportOperationEntity operation = new CardImportOperationEntity();
        operation.setOperationId(operationId);
        operation.setStatus(Status.PROCESSING);
        return cardImportOperationRepository.save(operation);
    }

    private Status resolveStatus(List<CardEntity> cardEntities, List<CardImportErrorEntity> errorEntities) {
        if (cardEntities.isEmpty()) {
            return Status.FAILURE;
        }

        return errorEntities.isEmpty() ? Status.SUCCESS : Status.PARTIAL_SUCCESS;
    }
}
