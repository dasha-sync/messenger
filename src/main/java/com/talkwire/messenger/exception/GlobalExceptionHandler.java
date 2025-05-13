package com.talkwire.messenger.exception;

import com.talkwire.messenger.dto.exception.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;


@RestControllerAdvice
public class GlobalExceptionHandler {

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

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGeneralException(
      Exception ex,
      HttpServletRequest request
  ) {
    return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), request);
  }

  @ExceptionHandler(ChatAccessDeniedException.class)
  public ResponseEntity<ErrorResponse> handleForbiddenException(
      ChatAccessDeniedException ex,
      HttpServletRequest request
  ) {
    return buildErrorResponse(HttpStatus.FORBIDDEN, ex.getMessage(), request);
  }

  @ExceptionHandler(ChatNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleForbiddenException(
      ChatNotFoundException ex,
      HttpServletRequest request
  ) {
    return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
  }

  @ExceptionHandler(ChatOperationException.class)
  public ResponseEntity<ErrorResponse> handleForbiddenException(
      ChatOperationException ex,
      HttpServletRequest request
  ) {
    return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage(), request);
  }

  @ExceptionHandler(MessageNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleForbiddenException(
      MessageNotFoundException ex,
      HttpServletRequest request
  ) {
    return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
  }

  @ExceptionHandler(MessageOperationException.class)
  public ResponseEntity<ErrorResponse> handleForbiddenException(
      MessageOperationException ex,
      HttpServletRequest request
  ) {
    return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage(), request);
  }

  @ExceptionHandler(UserAlreadyExistsException.class)
  public ResponseEntity<ErrorResponse> handleForbiddenException(
      UserAlreadyExistsException ex,
      HttpServletRequest request
  ) {
    return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage(), request);
  }

  @ExceptionHandler(UserDeleteException.class)
  public ResponseEntity<ErrorResponse> handleForbiddenException(
      UserDeleteException ex,
      HttpServletRequest request
  ) {
    return buildErrorResponse(HttpStatus.FORBIDDEN, ex.getMessage(), request);
  }

  @ExceptionHandler(UserNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleForbiddenException(
      UserNotFoundException ex,
      HttpServletRequest request
  ) {
    return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage(), request);
  }

  @ExceptionHandler(UserUpdateException.class)
  public ResponseEntity<ErrorResponse> handleForbiddenException(
      UserUpdateException ex,
      HttpServletRequest request
  ) {
    return buildErrorResponse(HttpStatus.FORBIDDEN, ex.getMessage(), request);
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
