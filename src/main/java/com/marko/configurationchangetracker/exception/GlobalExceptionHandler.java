package com.marko.configurationchangetracker.exception;

import com.marko.configurationchangetracker.dto.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ConfigurationChangeNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleChangeNotFound(
        ConfigurationChangeNotFoundException e
    ) {

        ErrorResponse response = new ErrorResponse(
            404,
            e.getMessage(),
            LocalDateTime.now()
        );

        return ResponseEntity.status(404).body(response);
    }

    @ExceptionHandler(InvalidConfigurationChangeException.class)
    public ResponseEntity<ErrorResponse> handleInvalidChange(
        InvalidConfigurationChangeException e
    ) {
        ErrorResponse response = new ErrorResponse(
            400,
            e.getMessage(),
            LocalDateTime.now()
        );

        return ResponseEntity.status(400).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
        MethodArgumentNotValidException e
    ) {
        String message = e.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .findFirst()
            .orElse("Validation failed");

        ErrorResponse response = new ErrorResponse(
            400,
            message,
            LocalDateTime.now()
        );

        return ResponseEntity.status(400).body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRequestBody(
        HttpMessageNotReadableException e
    ) {
        ErrorResponse response = new ErrorResponse(
            400,
            "Invalid request body",
            LocalDateTime.now()
        );

        return ResponseEntity.status(400).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(
        Exception e
    ) {
        ErrorResponse response = new ErrorResponse(
            500,
            "Unexpected internal server error",
            LocalDateTime.now()
        );

        return ResponseEntity.status(500).body(response);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
        MethodArgumentTypeMismatchException e
    ) {
        ErrorResponse response = new ErrorResponse(
            400,
            "Invalid request parameter value",
            LocalDateTime.now()
        );

        return ResponseEntity.status(400).body(response);
    }

}
