package com.example.card_service.repository.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "card_import_errors")
public class CardImportErrorEntity extends AuditableEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "UUID")
    private UUID id;

    @Column(name = "card_name", nullable = false)
    private String cardName;

    @Column(name = "errorDescription", nullable = false)
    private String errorDescription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operation_id", nullable = false)
    private CardImportOperationEntity operation;
}