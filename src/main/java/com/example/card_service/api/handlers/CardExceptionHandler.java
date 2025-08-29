package com.example.card_service.api.handlers;

import com.example.card_service.api.model.dto.ApiErrorResponse;
import com.example.card_service.exceptions.CardImportOperationNotFoundException;
import com.example.card_service.utils.DateTimeProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@RequiredArgsConstructor
public class CardExceptionHandler {
    private static final int INPUT_VALIDATION_FAILED_CODE = 1001;
    private static final int CARD_IMPORT_OPERATION_NOT_FOUND_CODE = 1002;
    private static final String CARD_IMPORT_OPERATION_NOT_FOUND_MESSAGE = "Cards not found for the given operation ID";

    private final DateTimeProvider dateTimeProvider;

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(CardImportOperationNotFoundException.class)
    public ApiErrorResponse handleOperationNotFound(CardImportOperationNotFoundException e) {
        return new ApiErrorResponse(
                CARD_IMPORT_OPERATION_NOT_FOUND_CODE,
                CARD_IMPORT_OPERATION_NOT_FOUND_MESSAGE,
                dateTimeProvider.now()
        );
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiErrorResponse handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        FieldError::getDefaultMessage,
                        (existing, replacement) -> existing
                ));

        return new ApiErrorResponse(
                INPUT_VALIDATION_FAILED_CODE,
                errors.toString(),
                dateTimeProvider.now()
        );
    }
}
