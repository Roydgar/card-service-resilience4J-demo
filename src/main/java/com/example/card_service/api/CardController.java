package com.example.card_service.api;

import com.example.card_service.api.model.dto.GetCardsResponse;
import com.example.card_service.api.model.dto.ImportCardsRequest;
import com.example.card_service.api.model.dto.ImportCardsResponse;
import com.example.card_service.mapper.CardRequestResponseMapper;
import com.example.card_service.repository.model.CardImportOperationEntity;
import com.example.card_service.service.CardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;
    private final CardRequestResponseMapper cardMapper;

    @PostMapping("/import")
    public ImportCardsResponse importCards(@RequestBody @Valid ImportCardsRequest request) {
        UUID operationId = cardService.importCards(request.getCardNames());
        return new ImportCardsResponse(operationId);
    }

    @GetMapping
    public GetCardsResponse getCardsByOperationId(@RequestParam UUID operationId) {
        CardImportOperationEntity importOperation = cardService.getCards(operationId);
        return cardMapper.toImportCardsResponse(importOperation);
    }
}
