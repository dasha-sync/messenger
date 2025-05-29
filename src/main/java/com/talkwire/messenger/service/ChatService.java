package com.talkwire.messenger.service;

import com.talkwire.messenger.dto.chat.*;
import com.talkwire.messenger.exception.chat.*;
import com.talkwire.messenger.exception.user.UserNotFoundException;
import com.talkwire.messenger.model.*;
import com.talkwire.messenger.repository.*;
import jakarta.transaction.Transactional;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {
  private final ChatRepository chatRepository;
  private final ChatMemberRepository chatMemberRepository;
  private final UserRepository userRepository;
  private final UserService userService;
  private final SimpMessagingTemplate messagingTemplate;

  public ChatListResponse getUserChats(Principal principal) {
    User currentUser = getCurrentUser(principal);

    List<ChatResponse> chats = chatMemberRepository.findChatsByUserId(currentUser.getId())
        .stream()
        .map(chat -> mapToChatResponse(chat, "GET", currentUser))
        .toList();

    return new ChatListResponse(currentUser.getUsername(), chats);
  }

  public ChatResponse getChatById(Long chatId, Principal principal) {
    User currentUser = getCurrentUser(principal);
    Chat chat = getChatForUser(chatId, currentUser.getId());

    return mapToChatResponse(chat, "GET", currentUser);
  }

  @Transactional
  public ChatResponse createChat(CreateChatRequest request, Principal principal) {
    User currentUser = getCurrentUser(principal);
    User targetUser = getUserByUsername(request.getUsername());

    if (currentUser.getId().equals(targetUser.getId())) {
      throw new ChatOperationException("Chat with yourself is not allowed");
    }

    if (chatExistsBetween(currentUser, targetUser)) {
      throw new ChatOperationException("Chat already exists between these users");
    }

    Chat chat = createAndPersistChat(currentUser, targetUser);

    ChatResponse responseForCurrentUser = mapToChatResponse(chat, "CREATE", currentUser);
    ChatResponse responseForTargetUser = mapToChatResponse(chat, "CREATE", targetUser);

    sendWebSocketUpdate(responseForCurrentUser, currentUser);
    sendWebSocketUpdate(responseForTargetUser, targetUser);

    return responseForCurrentUser;
  }

  @Transactional
  public ChatResponse deleteChat(Long chatId, Principal principal) {
    User currentUser = getCurrentUser(principal);

    validateChatAccess(chatId, currentUser.getId());

    Chat chat = chatRepository.findById(chatId)
        .orElseThrow(() -> new ChatNotFoundException("Chat not found"));

    User targetUser = chatMemberRepository.findOtherUserInChat(chatId, currentUser.getId())
        .orElseThrow(() -> new ChatOperationException("Second chat member not found"));

    chatRepository.delete(chat);
    verifyChatDeletion(chatId);

    ChatResponse response = mapToChatResponse(chat, "DELETE", currentUser);

    sendWebSocketUpdate(response, currentUser);
    sendWebSocketUpdate(response, targetUser);

    return response;
  }

  private User getCurrentUser(Principal principal) {
    return userService.getCurrentUser(principal);
  }

  private User getUserByUsername(String username) {
    return userRepository.findUserByUsername(username)
        .orElseThrow(() -> new UserNotFoundException("Target user not found"));
  }

  private void validateChatAccess(Long chatId, Long userId) {
    if (!chatMemberRepository.existsByUserIdAndChatId(userId, chatId)) {
      throw new ChatAccessDeniedException("Access denied: You are not a member of this chat");
    }
  }

  private boolean chatExistsBetween(User user1, User user2) {
    return !chatMemberRepository.findChatsByTwoUsers(user1.getId(), user2.getId()).isEmpty();
  }

  private Chat createAndPersistChat(User user1, User user2) {
    Chat chat = new Chat();
    chat.addChatMember(new ChatMember(chat, user1));
    chat.addChatMember(new ChatMember(chat, user2));

    Chat savedChat = chatRepository.save(chat);
    Long chatId = savedChat.getId();

    if (chatId == null || !chatRepository.existsById(chatId)) {
      throw new ChatOperationException("Failed to persist chat");
    }

    if (savedChat.getChatMembers().isEmpty() || !chatMemberRepository.existsByChatId(chatId)) {
      throw new ChatOperationException("Chat members not persisted");
    }

    return savedChat;
  }

  private void verifyChatDeletion(Long chatId) {
    if (chatRepository.existsById(chatId) || chatMemberRepository.existsByChatId(chatId)) {
      throw new ChatOperationException("Failed to delete chat or its members");
    }
  }

  private Chat getChatForUser(Long chatId, Long userId) {
    return chatMemberRepository.findChatsByUserId(userId)
        .stream()
        .filter(chat -> chat.getId().equals(chatId))
        .findFirst()
        .orElseThrow(() -> new ChatNotFoundException("Chat not found"));
  }

  private ChatResponse mapToChatResponse(Chat chat, String action, User user) {
    return new ChatResponse(chat.getId(), chat.getName(user), action);
  }

  private void sendWebSocketUpdate(ChatResponse response, User user) {
    messagingTemplate.convertAndSend("/topic/chats/" + user.getUsername(), response);
  }
}
