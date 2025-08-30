package com.example.card_service.service;

import java.util.List;
import java.util.UUID;

public interface CardImportService {
    void importCardsByNames(List<String> cardNames, UUID operationId);
}
