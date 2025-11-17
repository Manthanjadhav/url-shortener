package com.manthan.urlShortener.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    private ResponseEntity<Map<String, String>> buildResponse(String message, HttpStatus status) {
        Map<String, String> body = new HashMap<>();
        body.put("error", message);
        return new ResponseEntity<>(body, status);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleUserNotFound(UserNotFoundException ex) {
        log.warn("User not found: {}", ex.getMessage());
        return buildResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UrlNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleUrlNotFound(UrlNotFoundException ex) {
        log.warn("URL not found: {}", ex.getMessage());
        return buildResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Invalid request: {}", ex.getMessage());
        return buildResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DateTimeParseException.class)
    public ResponseEntity<Map<String, String>> handleInvalidDate(DateTimeParseException ex) {
        log.warn("Invalid date format: {}", ex.getParsedString());
        return buildResponse("Invalid date format. Use ISO format.", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        log.warn("Validation failed: {}", ex.getMessage());
        return buildResponse("Validation failed", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleUnexpected(Exception ex) {
        log.error("Unexpected error occurred:", ex);
        return buildResponse("Something went wrong. Please try again later.", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

