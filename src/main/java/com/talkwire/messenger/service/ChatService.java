package com.talkwire.messenger.service;

import com.talkwire.messenger.dto.chat.*;
import com.talkwire.messenger.exception.*;
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
    User targetUser = userRepository.findUserByUsername(request.getUsername())
        .orElseThrow(() -> new UserNotFoundException("Target user not found"));

    if (currentUser.getId().equals(targetUser.getId())) {
      throw new ChatOperationException("Chat with yourself feature in develop");
    }

    List<Chat> existingChats = chatMemberRepository
        .findChatsByTwoUsers(currentUser.getId(), targetUser.getId());

    if (!existingChats.isEmpty()) {
      throw new ChatOperationException("Chat already exists between these users");
    }

    Chat chat = new Chat();
    createChatMember(chat, currentUser);
    createChatMember(chat, targetUser);
    chatRepository.save(chat);

    if (chat.getChatMembers().isEmpty()) {
      throw new ChatOperationException("Chat members not created");
    }

    return new ChatResponse(chat.getId(), chat.getName(currentUser));
  }

  @Transactional
  public void deleteChat(Long chatId, Principal principal) {
    User currentUser = userService.getCurrentUser(principal);
    validateChatAccess(currentUser.getId(), chatId);

    chatRepository.deleteById(chatId);

    if (chatRepository.existsById(chatId)) {
      throw new ChatOperationException("Failed to delete chat");
    }
  }

  private void createChatMember(Chat chat, User user) {
    ChatMember chatMember = new ChatMember();
    chatMember.setChat(chat);
    chatMember.setUser(user);
    chat.addChatMember(chatMember);
  }

  private void validateChatAccess(Long userId, Long chatId) {
    if (!chatMemberRepository.existsByUserIdAndChatId(userId, chatId)) {
      throw new ChatAccessDeniedException("Access denied: You are not a member of this chat");
    }
  }
}
