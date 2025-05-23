package com.talkwire.messenger.exception;

import com.talkwire.messenger.dto.exception.ErrorResponse;
import com.talkwire.messenger.exception.chat.*;
import com.talkwire.messenger.exception.contact.*;
import com.talkwire.messenger.exception.message.*;
import com.talkwire.messenger.exception.request.*;
import com.talkwire.messenger.exception.user.*;
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

  @ExceptionHandler(ChatNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleNoSuchElementException(
      ChatNotFoundException ex,
      HttpServletRequest request
  ) {
    return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
  }

  @ExceptionHandler(MessageNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleNoSuchElementException(
      MessageNotFoundException ex,
      HttpServletRequest request
  ) {
    return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
  }

  @ExceptionHandler(RequestNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleNoSuchElementException(
      RequestNotFoundException ex,
      HttpServletRequest request
  ) {
    return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
  }

  @ExceptionHandler(ContactNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleNoSuchElementException(
      ContactNotFoundException ex,
      HttpServletRequest request
  ) {
    return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
  }

  @ExceptionHandler(UserNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleConflictException(
      UserNotFoundException ex,
      HttpServletRequest request
  ) {
    return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
  }

  @ExceptionHandler(ChatAccessDeniedException.class)
  public ResponseEntity<ErrorResponse> handleForbiddenException(
      ChatAccessDeniedException ex,
      HttpServletRequest request
  ) {
    return buildErrorResponse(HttpStatus.FORBIDDEN, ex.getMessage(), request);
  }

  @ExceptionHandler(UserDeleteException.class)
  public ResponseEntity<ErrorResponse> handleForbiddenException(
      UserDeleteException ex,
      HttpServletRequest request
  ) {
    return buildErrorResponse(HttpStatus.FORBIDDEN, ex.getMessage(), request);
  }

  @ExceptionHandler(UserUpdateException.class)
  public ResponseEntity<ErrorResponse> handleForbiddenException(
      UserUpdateException ex,
      HttpServletRequest request
  ) {
    return buildErrorResponse(HttpStatus.FORBIDDEN, ex.getMessage(), request);
  }

  @ExceptionHandler(RequestAccessDeniedException.class)
  public ResponseEntity<ErrorResponse> handleForbiddenException(
      RequestAccessDeniedException ex,
      HttpServletRequest request
  ) {
    return buildErrorResponse(HttpStatus.FORBIDDEN, ex.getMessage(), request);
  }

  @ExceptionHandler(ContactAccessDeniedException.class)
  public ResponseEntity<ErrorResponse> handleForbiddenException(
      ContactAccessDeniedException ex,
      HttpServletRequest request
  ) {
    return buildErrorResponse(HttpStatus.FORBIDDEN, ex.getMessage(), request);
  }

  @ExceptionHandler(ChatOperationException.class)
  public ResponseEntity<ErrorResponse> handleConflictException(
      ChatOperationException ex,
      HttpServletRequest request
  ) {
    return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage(), request);
  }

  @ExceptionHandler(MessageOperationException.class)
  public ResponseEntity<ErrorResponse> handleConflictException(
      MessageOperationException ex,
      HttpServletRequest request
  ) {
    return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage(), request);
  }

  @ExceptionHandler(UserAlreadyExistsException.class)
  public ResponseEntity<ErrorResponse> handleConflictException(
      UserAlreadyExistsException ex,
      HttpServletRequest request
  ) {
    return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage(), request);
  }


  @ExceptionHandler(RequestOperationException.class)
  public ResponseEntity<ErrorResponse> handleConflictException(
      RequestOperationException ex,
      HttpServletRequest request
  ) {
    return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage(), request);
  }

  @ExceptionHandler(UserOperationException.class)
  public ResponseEntity<ErrorResponse> handleConflictException(
      UserOperationException ex,
      HttpServletRequest request
  ) {
    return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage(), request);
  }

  @ExceptionHandler(ContactOperationException.class)
  public ResponseEntity<ErrorResponse> handleConflictException(
      ContactOperationException ex,
      HttpServletRequest request
  ) {
    return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage(), request);
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
