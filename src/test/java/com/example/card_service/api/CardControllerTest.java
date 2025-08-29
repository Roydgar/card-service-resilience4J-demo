package com.example.card_service.api;

import com.example.card_service.TestcontainersConfiguration;
import com.example.card_service.api.model.dto.GetCardsResponse;
import com.example.card_service.api.model.dto.ImportCardsRequest;
import com.example.card_service.mapper.CardRequestResponseMapper;
import com.example.card_service.repository.model.CardImportOperationEntity;
import com.example.card_service.service.CardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// TODO: Add more assertions
@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class CardControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private CardService cardService;
    @MockitoBean private CardRequestResponseMapper cardMapper;

    @Test
    void importCards_shouldReturnOperationId() throws Exception {
        UUID operationId = UUID.randomUUID();
        ImportCardsRequest request = new ImportCardsRequest();
        request.setCardNames(List.of("Black Lotus", "Mox Pearl"));

        when(cardService.importCards(request.getCardNames())).thenReturn(operationId);

        mockMvc.perform(post("/api/v1/cards/import")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.operationId").value(operationId.toString()));
    }

    @Test
    void getCardsByOperationId_shouldReturnCards() throws Exception {
        UUID operationId = UUID.randomUUID();
        CardImportOperationEntity entity = new CardImportOperationEntity();
        GetCardsResponse response = new GetCardsResponse();

        when(cardService.getCards(operationId)).thenReturn(entity);
        when(cardMapper.toImportCardsResponse(entity)).thenReturn(response);

        mockMvc.perform(get("/api/v1/cards")
                        .param("operationId", operationId.toString()))
                .andExpect(status().isOk());
    }
}