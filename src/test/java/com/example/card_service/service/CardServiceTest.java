package com.example.card_service.service;

import com.example.card_service.exceptions.CardImportOperationNotFoundException;
import com.example.card_service.repository.CardImportOperationRepository;
import com.example.card_service.repository.model.CardImportOperationEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {
    @Mock private CardImportOperationRepository cardImportOperationRepository;
    @Mock private CardAsyncImportService cardAsyncImportService;

    @Captor private org.mockito.ArgumentCaptor<UUID> uuidArgumentCaptor;
    @InjectMocks private CardService subject;

    @Test
    void importCards() {
        List<String> cardNames = List.of("Card1", "Card2", "Card3");


        UUID operationId = subject.importCards(cardNames);

        verify(cardAsyncImportService).importCardsByNames(eq(cardNames), uuidArgumentCaptor.capture());
        verifyNoMoreInteractions(cardAsyncImportService);

        assertThat(operationId).isEqualTo(uuidArgumentCaptor.getValue());


    }

    @Test
    void getCards() {
        CardImportOperationEntity entity = new CardImportOperationEntity();
        UUID id = UUID.randomUUID();

        when(cardImportOperationRepository.getByOperationId(any(UUID.class))).thenReturn(Optional.of(entity));

        CardImportOperationEntity operation = subject.getCards(id);

        verify(cardImportOperationRepository).getByOperationId(id);
        verifyNoMoreInteractions(cardImportOperationRepository);

        assertThat(operation).isEqualTo(entity);
    }

    @Test
    void getCards_whenCardImportOperationEntityIsNotFound_throwsCardImportOperationNotFoundException() {
        UUID id = UUID.randomUUID();

        when(cardImportOperationRepository.getByOperationId(any(UUID.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> subject.getCards(id))
                .isInstanceOf(CardImportOperationNotFoundException.class)
                .hasMessage("No import operation found for ID: " + id);
    }
}