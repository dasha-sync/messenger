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
        .map(chat -> toChatResponse(chat, "GET", currentUser))
        .toList();

    return new ChatListResponse(currentUser.getUsername(), chats);
  }

  public ChatResponse getChatById(Long chatId, Principal principal) {
    User currentUser = getCurrentUser(principal);
    Chat chat = getChatForUser(chatId, currentUser.getId());

    return toChatResponse(chat, "GET", currentUser);
  }

  @Transactional
  public ChatResponse createChat(CreateChatRequest request, Principal principal) {
    User currentUser = getCurrentUser(principal);
    User targetUser = getUserByUsername(request.getUsername());

    validateDifferentUsers(currentUser, targetUser);
    ensureChatDoesNotExist(currentUser, targetUser);

    Chat newChat = createNewChat(currentUser, targetUser);
    Chat savedChat = chatRepository.save(newChat);
    verifyChatPersistence(savedChat);

    ChatResponse response = toChatResponse(savedChat, "CREATE", currentUser);
    sendThrowWebSocket(response, currentUser);

    response.setName(savedChat.getName(targetUser));
    sendThrowWebSocket(response, targetUser);

    return response;
  }

  @Transactional
  public ChatResponse deleteChat(Long chatId, Principal principal) {
    User currentUser = getCurrentUser(principal);
    Chat chat = chatRepository.findById(chatId)
        .orElseThrow(() -> new ChatNotFoundException("Chat not found"));
    User targetUser = chatMemberRepository.findOtherUserInChat(chatId, currentUser.getId())
        .orElseThrow(() -> new ChatOperationException("Second chat member not found"));

    validateChatAccess(currentUser.getId(), chatId);
    chatRepository.delete(chat);

    if (chatRepository.existsById(chatId) || chatMemberRepository.existsByChatId(chatId)) {
      throw new ChatOperationException("Failed to delete chat or its members");
    }

    ChatResponse response = toChatResponse(chat, "DELETE", currentUser);
    sendThrowWebSocket(response, currentUser);
    sendThrowWebSocket(response, targetUser);

    return response;
  }

  private User getCurrentUser(Principal principal) {
    return userService.getCurrentUser(principal);
  }

  private User getUserByUsername(String username) {
    return userRepository.findUserByUsername(username)
        .orElseThrow(() -> new UserNotFoundException("Target user not found"));
  }

  private void validateChatAccess(Long userId, Long chatId) {
    if (!chatMemberRepository.existsByUserIdAndChatId(userId, chatId)) {
      throw new ChatAccessDeniedException("Access denied: You are not a member of this chat");
    }
  }

  private Chat getChatForUser(Long chatId, Long userId) {
    return chatMemberRepository.findChatsByUserId(userId)
        .stream()
        .filter(chat -> chat.getId().equals(chatId))
        .findFirst()
        .orElseThrow(() -> new ChatNotFoundException("Chat not found"));
  }

  private ChatResponse toChatResponse(Chat chat, String action, User user) {
    return new ChatResponse(chat.getId(), chat.getName(user), action);
  }

  private void validateDifferentUsers(User currentUser, User targetUser) {
    if (currentUser.getId().equals(targetUser.getId())) {
      throw new ChatOperationException("Chat with yourself is not allowed");
    }
  }

  private void ensureChatDoesNotExist(User user1, User user2) {
    boolean exists = !chatMemberRepository
        .findChatsByTwoUsers(user1.getId(), user2.getId())
        .isEmpty();

    if (exists) {
      throw new ChatOperationException("Chat already exists between these users");
    }
  }

  private Chat createNewChat(User user1, User user2) {
    Chat chat = new Chat();
    chat.addChatMember(new ChatMember(chat, user1));
    chat.addChatMember(new ChatMember(chat, user2));
    return chat;
  }

  private void verifyChatPersistence(Chat chat) {
    Long chatId = chat.getId();
    if (chatId == null || !chatRepository.existsById(chatId)) {
      throw new ChatOperationException("Failed to persist chat");
    }

    if (chat.getChatMembers().isEmpty() || !chatMemberRepository.existsByChatId(chatId)) {
      throw new ChatOperationException("Chat members not persisted");
    }
  }

  private void sendThrowWebSocket(ChatResponse response, User user) {
    messagingTemplate.convertAndSend("/topic/chats/" + user.getUsername(), response);
  }
}
