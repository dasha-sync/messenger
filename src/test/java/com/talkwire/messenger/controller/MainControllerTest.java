package com.talkwire.messenger.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;

/**
 * Test class for the MainController.
 * Provides integration tests for the controller endpoints.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MainControllerTest {

  /**
   * RestTemplate for testing HTTP requests and responses.
   * Injected by Spring for making test API calls.
   */
  @Autowired
  private TestRestTemplate template;
}
