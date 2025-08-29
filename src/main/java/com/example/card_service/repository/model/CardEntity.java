package com.example.card_service.repository.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "cards")
public class CardEntity extends AuditableEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "UUID")
    private UUID id;

    @Column(name = "png_url", nullable = false)
    private String pngUrl;

    @Column(name = "card_name", nullable = false)
    private String cardName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operation_id", nullable = false)
    private CardImportOperationEntity operation;
}