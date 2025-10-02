package com.puntored.prueba.exception;

import com.puntored.prueba.model.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidation(MethodArgumentNotValidException ex){
        return ResponseEntity.status(422).body(new ApiResponse<>(422, "Datos inválidos", ex.getBindingResult().getAllErrors()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleConstraint(ConstraintViolationException ex){
        return ResponseEntity.status(422).body(new ApiResponse<>(422, "Datos inválidos", ex.getConstraintViolations()));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleNotFound(EntityNotFoundException ex){
        return ResponseEntity.status(404).body(new ApiResponse<>(404, ex.getMessage(), null));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleBadRequest(IllegalArgumentException ex){
        return ResponseEntity.status(400).body(new ApiResponse<>(400, ex.getMessage(), null));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Object>> handleConflict(IllegalStateException ex){
        return ResponseEntity.status(409).body(new ApiResponse<>(409, ex.getMessage(), null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleOther(Exception ex){
        return ResponseEntity.status(500).body(new ApiResponse<>(500, "Error interno", ex.getMessage()));
    }
}

