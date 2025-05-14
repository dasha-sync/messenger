package com.talkwire.messenger.exception.request;

public class RequestNotFoundException extends RuntimeException {
  public RequestNotFoundException(String message) {
    super(message);
  }
}