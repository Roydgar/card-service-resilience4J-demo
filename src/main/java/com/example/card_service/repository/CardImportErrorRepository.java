package com.example.card_service.repository;

import com.example.card_service.repository.model.CardImportErrorEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface CardImportErrorRepository extends CrudRepository<CardImportErrorEntity, UUID> {
}
