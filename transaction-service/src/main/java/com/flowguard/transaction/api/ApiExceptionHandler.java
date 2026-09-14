package com.flowguard.transaction.api;

import com.flowguard.transaction.application.IdempotencyConflictException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(IdempotencyConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleIdempotencyConflict(
            IdempotencyConflictException exception) {

        return new ErrorResponse(
                "IDEMPOTENCY_CONFLICT",
                exception.getMessage());
    }

    public record ErrorResponse(
            String code,
            String message) {
    }
}