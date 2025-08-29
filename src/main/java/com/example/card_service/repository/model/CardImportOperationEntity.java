package com.example.card_service.repository.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "card_import_operations")
public class CardImportOperationEntity extends AuditableEntity {

    @Id
    @Column(name = "operation_id", nullable = false, updatable = false, columnDefinition = "UUID")
    private UUID operationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private Status status;

    @OneToMany(mappedBy = "operation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CardEntity> cards = new ArrayList<>();

    @OneToMany(mappedBy = "operation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CardImportErrorEntity> cardImportErrors = new ArrayList<>();

    public enum Status {
        PROCESSING, SUCCESS, FAILURE, PARTIAL_SUCCESS
    }
}