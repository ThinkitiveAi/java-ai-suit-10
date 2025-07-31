package com.healthfirst.exception;

import com.healthfirst.dto.ProviderRegistrationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handle validation errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        
        Map<String, String> errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .collect(Collectors.toMap(
                FieldError::getField,
                FieldError::getDefaultMessage,
                (existing, replacement) -> existing
            ));

        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "Validation failed");
        response.put("errors", errors);

        logger.warn("Validation errors: {}", errors);
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Handle data integrity violations (duplicate keys, etc.)
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ProviderRegistrationResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex) {
        
        String message = "Data conflict occurred";
        
        if (ex.getMessage() != null) {
            if (ex.getMessage().contains("email")) {
                message = "Email address is already registered";
            } else if (ex.getMessage().contains("phone")) {
                message = "Phone number is already registered";
            } else if (ex.getMessage().contains("license")) {
                message = "License number is already registered";
            }
        }

        logger.warn("Data integrity violation: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ProviderRegistrationResponse.error(message));
    }

    /**
     * Handle malformed JSON requests
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex) {
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "Malformed JSON request");

        logger.warn("Malformed JSON request: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Handle method argument type mismatch
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex) {
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", String.format("Invalid value for parameter '%s'", ex.getName()));

        logger.warn("Type mismatch for parameter {}: {}", ex.getName(), ex.getValue());
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Handle generic exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGlobalException(
            Exception ex, WebRequest request) {
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "An unexpected error occurred");

        logger.error("Unexpected error occurred: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * Handle runtime exceptions
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "A system error occurred");

        logger.error("Runtime exception: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
} 