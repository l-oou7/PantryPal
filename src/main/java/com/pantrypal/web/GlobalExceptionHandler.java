package com.pantrypal.web;

import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.Map;

/** Turns every error into {"message": "...", "fieldErrors": {...}} so the frontend can show a friendly toast. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Map<String, Object>> api(ApiException e) {
        return body(e.getStatus(), e.getMessage(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> invalid(MethodArgumentNotValidException e) {
        Map<String, String> fields = new LinkedHashMap<>();
        for (FieldError fe : e.getBindingResult().getFieldErrors()) {
            fields.putIfAbsent(fe.getField(), fe.getDefaultMessage());
        }
        String first = fields.values().stream().findFirst().orElse("Please check the form.");
        return body(HttpStatus.BAD_REQUEST, first, fields);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> constraint(ConstraintViolationException e) {
        String msg = e.getConstraintViolations().stream().map(v -> v.getMessage()).findFirst().orElse("Invalid input.");
        return body(HttpStatus.BAD_REQUEST, msg, null);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            IllegalArgumentException.class})
    public ResponseEntity<Map<String, Object>> unreadable(Exception e) {
        String msg = e instanceof IllegalArgumentException && e.getMessage() != null
                ? e.getMessage() : "Some of the values sent were not in the expected format.";
        return body(HttpStatus.BAD_REQUEST, msg, null);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> badLogin(BadCredentialsException e) {
        return body(HttpStatus.UNAUTHORIZED, "That email and password don't match. Try again?", null);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> integrity(DataIntegrityViolationException e) {
        log.warn("Integrity violation: {}", e.getMostSpecificCause().getMessage());
        return body(HttpStatus.CONFLICT, "That conflicts with something you already have.", null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> other(Exception e) {
        if (e instanceof org.springframework.web.ErrorResponse er) {
            // framework errors that already carry a status: 404 no resource, 405 wrong method, 415 ...
            HttpStatus st = HttpStatus.resolve(er.getStatusCode().value());
            if (st != null && st.is4xxClientError()) {
                return body(st, st == HttpStatus.NOT_FOUND ? "We couldn't find that page." : st.getReasonPhrase(), null);
            }
        }
        if (e instanceof org.springframework.security.access.AccessDeniedException) {
            return body(HttpStatus.FORBIDDEN, "You don't have access to that.", null);
        }
        log.error("Unexpected error", e);
        return body(HttpStatus.INTERNAL_SERVER_ERROR, "Oops, something boiled over in the kitchen. Please try again.", null);
    }

    private ResponseEntity<Map<String, Object>> body(HttpStatus status, String message, Map<String, String> fields) {
        Map<String, Object> b = new LinkedHashMap<>();
        b.put("status", status.value());
        b.put("message", message);
        if (fields != null && !fields.isEmpty()) b.put("fieldErrors", fields);
        return ResponseEntity.status(status).body(b);
    }
}
