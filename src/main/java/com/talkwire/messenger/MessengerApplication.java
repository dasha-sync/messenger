package com.talkwire.messenger;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Entry point for the Messenger application.
 */
@SpringBootApplication
@EnableJpaRepositories("com.talkwire.messenger.repository")
public class MessengerApplication {

  /**
   * Launches the Spring Boot application.
   *
   * @param args command-line arguments
   */
  public static void main(String[] args) {
    SpringApplication.run(MessengerApplication.class, args);
  }

  /**
   * CommandLineRunner for optional startup logic.
   *
   * @param ctx the application context
   * @return a runnable executed after context initialization
   */
  @Bean
  public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
    return args -> {
      System.out.println("Let's inspect the beans provided by Spring Boot:");

      /*
      String[] beanNames = ctx.getBeanDefinitionNames();
      Arrays.sort(beanNames);
      for (String beanName : beanNames) {
        System.out.println(beanName);
      }
      */
    };
  }
}
