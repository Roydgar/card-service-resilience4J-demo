package com.example.card_service.repository;

import com.example.card_service.repository.model.CardImportOperationEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.UUID;

public interface CardImportOperationRepository extends CrudRepository<CardImportOperationEntity, UUID> {
    Optional<CardImportOperationEntity> getByOperationId(UUID operationId);
}
