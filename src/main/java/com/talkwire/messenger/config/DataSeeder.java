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
      if (userRepository.count() > 0
          || chatRepository.count() > 0
          || contactRepository.count() > 0
          || requestRepository.count() > 0
          || messageRepository.count() > 0
          || chatMemberRepository.count() > 0) {
        logger.info("Database is not empty. Skipping seed data creation.");
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
      Set<Set<Long>> existingChatPairs = new HashSet<>();

      for (User user : users) {
        logger.info("Processing user: {}", user.getUsername());

        int chatsCreated = 0;
        int chatAttempts = 0;
        while (chatsCreated < 10 && chatAttempts < 100) {
          chatAttempts++;
          User randomUser = getRandomUser(users, user);
          if (randomUser != null && createUniqueChat(
              user,
              randomUser,
              chatRepository,
              chatMemberRepository,
              messageRepository,
              existingChatPairs,
              faker)) {
            chatsCreated++;
          }
        }

        int contactsCreated = 0;
        int contactAttempts = 0;
        while (contactsCreated < 5 && contactAttempts < 100) {
          contactAttempts++;
          User randomUser = getRandomUser(users, user);
          if (randomUser != null
              && createBidirectionalContact(user, randomUser, contactRepository, createdContacts)) {
            contactsCreated++;
          }
        }

        int outgoingRequestsCreated = 0;
        int attempts = 0;
        while (outgoingRequestsCreated < 5 && attempts < 100) {
          attempts++;
          User randomUser = getRandomUser(users, user);
          if (randomUser != null && !relationshipExists(getRelationshipKey(
                  user.getId(),
                  randomUser.getId()),
                  getRelationshipKey(randomUser.getId(), user.getId()),
                  createdContacts)
              && createRequest(
                  user,
              randomUser,
              requestRepository,
              createdRequests,
              createdContacts)) {
            outgoingRequestsCreated++;
          }
        }

        int incomingRequestsCreated = 0;
        attempts = 0;
        while (incomingRequestsCreated < 5 && attempts < 100) {
          attempts++;
          User randomUser = getRandomUser(users, user);
          if (randomUser != null
              && !relationshipExists(getRelationshipKey(randomUser.getId(), user.getId()),
              getRelationshipKey(user.getId(), randomUser.getId()),
              createdContacts)
              && createRequest(
                  randomUser, user, requestRepository, createdRequests, createdContacts)) {
            incomingRequestsCreated++;
          }
        }
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

  private boolean relationshipExists(String key1, String key2, Set<String> existing) {
    return existing.contains(key1) || existing.contains(key2);
  }

  private boolean createBidirectionalContact(User user1, User user2,
                                             ContactRepository contactRepository,
                                             Set<String> createdContacts) {
    String key1 = getRelationshipKey(user1.getId(), user2.getId());
    String key2 = getRelationshipKey(user2.getId(), user1.getId());

    if (relationshipExists(key1, key2, createdContacts)) {
      return false;
    }

    try {
      Contact c1 = new Contact();
      c1.setUser(user1);
      c1.setContact(user2);
      contactRepository.save(c1);

      Contact c2 = new Contact();
      c2.setUser(user2);
      c2.setContact(user1);
      contactRepository.save(c2);

      createdContacts.add(key1);
      createdContacts.add(key2);
      return true;
    } catch (Exception e) {
      logger.error(
          "Failed to create bidirectional contact: {} <-> {}",
          user1.getUsername(),
          user2.getUsername(), e);
      return false;
    }
  }

  private boolean createRequest(User from, User to,
                                RequestRepository requestRepository,
                                Set<String> createdRequests,
                                Set<String> createdContacts) {
    String key = getRelationshipKey(from.getId(), to.getId());
    String reverseKey = getRelationshipKey(to.getId(), from.getId());

    if (relationshipExists(key, reverseKey, createdRequests)
        || relationshipExists(key, reverseKey, createdContacts)) {
      return false;
    }

    try {
      Request request = new Request();
      request.setUser(from);
      request.setContact(to);
      requestRepository.save(request);
      createdRequests.add(key);
      return true;
    } catch (Exception e) {
      logger.error("Failed to create request: {} -> {}", from.getUsername(), to.getUsername(), e);
      return false;
    }
  }

  private boolean createUniqueChat(User u1, User u2,
                                   ChatRepository chatRepository,
                                   ChatMemberRepository chatMemberRepository,
                                   MessageRepository messageRepository,
                                   Set<Set<Long>> existingChatPairs,
                                   Faker faker) {
    Set<Long> pair = new HashSet<>(Arrays.asList(u1.getId(), u2.getId()));
    if (existingChatPairs.contains(pair)) {
      return false;
    }

    Chat chat = new Chat();
    chat.setName("default");
    chatRepository.save(chat);

    ChatMember cm1 = new ChatMember();
    cm1.setUser(u1);
    cm1.setChat(chat);
    chatMemberRepository.save(cm1);

    ChatMember cm2 = new ChatMember();
    cm2.setUser(u2);
    cm2.setChat(chat);
    chatMemberRepository.save(cm2);

    for (ChatMember member : Arrays.asList(cm1, cm2)) {
      for (int j = 0; j < 5; j++) {
        Message message = new Message();
        message.setUser(member.getUser());
        message.setChat(chat);
        message.setText(faker.lorem().sentence());
        messageRepository.save(message);
      }
    }

    existingChatPairs.add(pair);
    return true;
  }
}
