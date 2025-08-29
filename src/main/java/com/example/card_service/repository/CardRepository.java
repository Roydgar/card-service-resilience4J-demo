package com.example.card_service.repository;

import com.example.card_service.repository.model.CardEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface CardRepository extends CrudRepository<CardEntity, UUID> {
}
