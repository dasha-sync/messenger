package com.talkwire.messenger.config;

import com.talkwire.messenger.model.*;
import com.talkwire.messenger.repository.*;
import java.util.*;
import net.datafaker.Faker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;


@Configuration
public class DataSeeder {
  private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);

  @Bean
  CommandLineRunner initDatabase(
      UserRepository userRepository,
      ChatRepository chatRepository,
      ChatMemberRepository chatMemberRepository,
      ContactRepository contactRepository,
      RequestRepository requestRepository,
      MessageRepository messageRepository,
      PasswordEncoder passwordEncoder) {

    return args -> {
      long userCount = userRepository.count();
      if (userCount > 0) {
        logger.info("Database is not empty ({} users found). Skipping seed data creation.",
            userCount);
        return;
      }

      logger.info("Database is empty. Starting seed data creation...");
      Faker faker = new Faker();
      List<User> users = new ArrayList<>();

      for (int i = 0; i < 20; i++) {
        User user = new User();
        user.setUsername(faker.name().username());
        user.setEmail("example." + i + "@example.com");
        user.setPassword(passwordEncoder.encode("password"));
        users.add(user);
      }
      userRepository.saveAll(users);
      logger.info("Created {} users", users.size());

      Set<String> createdContacts = new HashSet<>();
      Set<String> createdRequests = new HashSet<>();

      for (User user : users) {
        logger.info("Processing user: {}", user.getUsername());

        for (int i = 0; i < 10; i++) {
          Chat chat = new Chat();
          chat.setName("default");
          chatRepository.save(chat);

          ChatMember userChatMember = new ChatMember();
          userChatMember.setUser(user);
          userChatMember.setChat(chat);
          chatMemberRepository.save(userChatMember);

          User randomUser = getRandomUser(users, user);
          ChatMember otherChatMember = new ChatMember();
          otherChatMember.setUser(randomUser);
          otherChatMember.setChat(chat);
          chatMemberRepository.save(otherChatMember);

          // Add two messages from each user in the chat
          for (ChatMember member : Arrays.asList(userChatMember, otherChatMember)) {
            for (int j = 0; j < 2; j++) {
              Message message = new Message();
              message.setUser(member.getUser());
              message.setChat(chat);
              message.setText(faker.lorem().sentence()); // Generate random text
              messageRepository.save(message);
              logger.info("Created message from {} in chat {}: {}",
                  member.getUser().getUsername(),
                  chat.getId(),
                  message.getText());
            }
          }
        }
        logger.info("Created 10 chats with messages for user: {}", user.getUsername());

        int contactsCreated = 0;
        int attempts = 0;
        while (contactsCreated < 5 && attempts < 100) {
          attempts++;
          User contactUser = getRandomUser(users, user);
          if (contactUser != null) {
            String contactKey = getRelationshipKey(user.getId(), contactUser.getId());
            String reverseKey = getRelationshipKey(contactUser.getId(), user.getId());

            if (!createdContacts.contains(contactKey) && !createdContacts.contains(reverseKey)) {
              try {
                Contact contact = new Contact();
                contact.setUser(user);
                contact.setContact(contactUser);
                contactRepository.save(contact);
                createdContacts.add(contactKey);
                contactsCreated++;
                logger.info("Created contact: {} -> {}",
                    user.getUsername(),
                    contactUser.getUsername());
              } catch (Exception e) {
                logger.error("Failed to create contact: {} -> {}",
                    user.getUsername(),
                    contactUser.getUsername(), e);
              }
            }
          }
        }
        logger.info("Created {} contacts for user: {}", contactsCreated, user.getUsername());

        // Create 5 outgoing requests
        int outgoingRequestsCreated = 0;
        attempts = 0;
        while (outgoingRequestsCreated < 5 && attempts < 100) {
          attempts++;
          User contactUser = getRandomUser(users, user);
          if (contactUser != null) {
            String requestKey = getRelationshipKey(user.getId(), contactUser.getId());
            String reverseKey = getRelationshipKey(contactUser.getId(), user.getId());

            if (!createdRequests.contains(requestKey) && !createdRequests.contains(reverseKey)
                && !createdContacts.contains(requestKey) && !createdContacts.contains(reverseKey)) {
              try {
                Request request = new Request();
                request.setUser(user);
                request.setContact(contactUser);
                requestRepository.save(request);
                createdRequests.add(requestKey);
                outgoingRequestsCreated++;
                logger.info("Created outgoing request: {} -> {}",
                    user.getUsername(),
                    contactUser.getUsername());
              } catch (Exception e) {
                logger.error("Failed to create outgoing request: {} -> {}",
                    user.getUsername(),
                    contactUser.getUsername(), e);
              }
            }
          }
        }
        logger.info("Created {} outgoing requests for user: {}",
            outgoingRequestsCreated,
            user.getUsername());

        // Create 5 incoming requests
        int incomingRequestsCreated = 0;
        attempts = 0;
        while (incomingRequestsCreated < 5 && attempts < 100) {
          attempts++;
          User requestUser = getRandomUser(users, user);
          if (requestUser != null) {
            String requestKey = getRelationshipKey(requestUser.getId(), user.getId());
            String reverseKey = getRelationshipKey(user.getId(), requestUser.getId());

            if (!createdRequests.contains(requestKey) && !createdRequests.contains(reverseKey)
                && !createdContacts.contains(requestKey) && !createdContacts.contains(reverseKey)) {
              try {
                Request request = new Request();
                request.setUser(requestUser);
                request.setContact(user);
                requestRepository.save(request);
                createdRequests.add(requestKey);
                incomingRequestsCreated++;
                logger.info("Created incoming request: {} -> {}",
                    requestUser.getUsername(),
                    user.getUsername());
              } catch (Exception e) {
                logger.error("Failed to create incoming request: {} -> {}",
                    requestUser.getUsername(),
                    user.getUsername(), e);
              }
            }
          }
        }
        logger.info("Created {} incoming requests for user: {}",
            incomingRequestsCreated,
            user.getUsername());
      }

      logger.info("Seed data creation completed successfully");
    };
  }

  private User getRandomUser(List<User> users, User excludeUser) {
    List<User> availableUsers = new ArrayList<>(users);
    availableUsers.remove(excludeUser);
    if (availableUsers.isEmpty()) {
      return null;
    }
    return availableUsers.get(new Random().nextInt(availableUsers.size()));
  }

  private String getRelationshipKey(Long userId1, Long userId2) {
    return userId1 + ":" + userId2;
  }
}
