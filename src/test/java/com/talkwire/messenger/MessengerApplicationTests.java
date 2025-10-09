package com.talkwire.messenger;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@SpringBootTest
class MessengerApplicationTests {

  @Autowired
  private ApplicationContext context;

  @Test
  void contextLoads() {
    assertNotNull(context);
  }

  @Test
  void testAutoConfiguration() {
    assertNotNull(context.getBean("dataSource"));
    assertNotNull(context.getBean("entityManagerFactory")); // для JPA
  }
}
