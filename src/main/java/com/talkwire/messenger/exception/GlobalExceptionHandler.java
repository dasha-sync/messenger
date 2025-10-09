package com.talkwire.messenger.exception;

import com.talkwire.messenger.dto.exception.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.*;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;


@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGeneralException(
      Exception ex,
      HttpServletRequest request
  ) {
    String uri = request.getRequestURI();
    if (uri.startsWith("/v3/api-docs") || uri.startsWith("/swagger-ui")) {
      throw new RuntimeException(ex);
    }
    return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), request);
  }

  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<ErrorResponse> handleResponseStatusException(
      ResponseStatusException ex,
      HttpServletRequest request
  ) {
    return buildErrorResponse((HttpStatus) ex.getStatusCode(), ex.getReason(), request);
  }

  @ExceptionHandler(NoSuchElementException.class)
  public ResponseEntity<ErrorResponse> handleNoSuchElementException(
      NoSuchElementException ex,
      HttpServletRequest request
  ) {
    return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
  }

  @ExceptionHandler(GlobalException.class)
  public ResponseEntity<ErrorResponse> handleForbiddenException(
      GlobalException ex,
      HttpServletRequest request
  ) {
    return buildErrorResponse(ex.getStatus(), ex.getMessage(), request);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, Object>> handleValidationException(
      MethodArgumentNotValidException ex) {
    Map<String, String> errors = new LinkedHashMap<>();

    ex.getBindingResult().getFieldErrors().forEach(error ->
        errors.put(error.getField(), error.getDefaultMessage())
    );

    Map<String, Object> response = new LinkedHashMap<>();
    response.put("status", HttpStatus.BAD_REQUEST.value());
    response.put("error", "Validation failed");
    response.put("timestamp", new Date());
    response.put("errors", errors);

    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }

  private ResponseEntity<ErrorResponse> buildErrorResponse(
      HttpStatus status, String message, HttpServletRequest request
  ) {
    ErrorResponse errorResponse = new ErrorResponse(
        status.value(),
        status.getReasonPhrase(),
        message,
        request.getRequestURI(),
        LocalDateTime.now()
    );
    return ResponseEntity.status(status).body(errorResponse);
  }
}
