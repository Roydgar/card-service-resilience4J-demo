package com.example.card_service.exceptions;

public class CardImportOperationNotFoundException extends RuntimeException {
    public CardImportOperationNotFoundException(String message) {
        super(message);
    }
}
