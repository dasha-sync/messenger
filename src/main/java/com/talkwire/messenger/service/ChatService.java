package com.talkwire.messenger.service;

import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Service;

/**
 * Provides basic message handling logic for the chat system.
 */
@Service
public class ChatService {
  private final String characters = "qwertyuiopasdfghjklzxcvbnm1234567890";

  /**
   * Generates a randomized string of the same length as the input.
   * Intended as a placeholder for a real chat response.
   *
   * @param data input string
   * @return randomized string
   */
  public String answerMessage(String data) {
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < data.length(); i++) {
      int index = ThreadLocalRandom.current().nextInt(characters.length());
      result.append(characters.charAt(index));
    }
    return result.toString();
  }
}
