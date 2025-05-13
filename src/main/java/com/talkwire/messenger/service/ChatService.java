package com.talkwire.messenger.service;

import com.talkwire.messenger.dto.chat.*;
import com.talkwire.messenger.model.*;
import com.talkwire.messenger.repository.*;
import jakarta.transaction.Transactional;
import java.security.Principal;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

@Service
@RequiredArgsConstructor
public class ChatService {
  private final ChatRepository chatRepository;
  private final ChatMemberRepository chatMemberRepository;
  private final UserRepository userRepository;
  private final UserService userService;

  // TODO: Websockets
  public String answerMessage(String data) {
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < data.length(); i++) {
      String characters = "qwertyuiopasdfghjklzxcvbnm1234567890";
      int index = ThreadLocalRandom.current().nextInt(characters.length());
      result.append(characters.charAt(index));
    }
    return result.toString();
  }

  public ChatListResponse getUserChats(Principal principal) {
    User currentUser = userService.getCurrentUser(principal);
    List<ChatResponse> chats = chatMemberRepository.findChatsByUserId(currentUser.getId())
        .stream()
        .map(chat -> new ChatResponse(chat.getId(), chat.getName(currentUser)))
        .toList();

    return new ChatListResponse(currentUser.getUsername(), chats);
  }

  public ChatResponse getChatById(Long chatId, Principal principal) {
    User currentUser = userService.getCurrentUser(principal);
    validateChatAccess(currentUser.getId(), chatId);

    Chat chat = chatMemberRepository.findChatsByUserId(currentUser.getId())
        .stream()
        .filter(c -> c.getId().equals(chatId))
        .findFirst()
        .orElseThrow(() -> new RuntimeException("Chat not found"));

    return new ChatResponse(chat.getId(), chat.getName(currentUser));
  }

  @Transactional
  public ChatResponse createChat(@RequestBody CreateChatRequest request, Principal principal) {
    User currentUser = userService.getCurrentUser(principal);
    // TODO: UserNotFoundException("Target user not found"));
    User targetUser = userRepository.findUserByUsername(request.getUsername())
        .orElseThrow(() -> new RuntimeException("Target user not found"));

    // TODO: ChatOperationException("Chat already exists between these users"));
    if (currentUser.getId().equals(targetUser.getId())) {
      throw new RuntimeException("Cannot create chat with yourself");
    }

    List<Chat> existingChats = chatMemberRepository
        .findChatsByTwoUsers(currentUser.getId(), targetUser.getId());

    // TODO: ChatOperationException("Target user not found");
    if (!existingChats.isEmpty()) {
      throw new RuntimeException("Chat already exists between these users");
    }

    Chat chat = new Chat();
    createChatMember(chat, currentUser);
    createChatMember(chat, targetUser);
    chatRepository.save(chat);

    // TODO: ChatOperationException("Chat members not created"));
    if (chat.getChatMembers().isEmpty()) {
      throw new RuntimeException("Chat members not created");
    }

    return new ChatResponse(chat.getId(), chat.getName(currentUser));
  }

  @Transactional
  public void deleteChat(Long chatId, Principal principal) {
    User currentUser = userService.getCurrentUser(principal);
    validateChatAccess(currentUser.getId(), chatId);

    chatRepository.deleteById(chatId);

    // TODO: ChatOperationException("Failed to delete chat")
    if (chatRepository.existsById(chatId)) {
      throw new RuntimeException("Failed to delete chat");
    }
  }

  private void createChatMember(Chat chat, User user) {
    ChatMember chatMember = new ChatMember();
    chatMember.setChat(chat);
    chatMember.setUser(user);
    chat.addChatMember(chatMember);
  }

  // TODO:  ChatAccessDeniedException("Access denied: You are not a member of this chat");
  private void validateChatAccess(Long userId, Long chatId) {
    if (!chatMemberRepository.existsByUserIdAndChatId(userId, chatId)) {
      throw new RuntimeException("Access denied: You are not a member of this chat");
    }
  }
}
