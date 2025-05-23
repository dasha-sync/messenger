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
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

@Service
@RequiredArgsConstructor
public class ChatService {
  private final ChatRepository chatRepository;
  private final ChatMemberRepository chatMemberRepository;
  private final UserRepository userRepository;
  private final UserService userService;

  public ChatListResponse getUserChats(Principal principal) {
    User currentUser = userService.getCurrentUser(principal);
    List<ChatResponse> chats = chatMemberRepository.findChatsByUserId(currentUser.getId())
        .stream()
        .map(chat -> mapToChatDto(chat, "GET", currentUser))
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
        .orElseThrow(() -> new ChatNotFoundException("Chat not found"));

    return mapToChatDto(chat, "GET", currentUser);
  }

  @Transactional
  public ChatResponse createChat(@RequestBody CreateChatRequest request, Principal principal) {
    User currentUser = userService.getCurrentUser(principal);
    User targetUser = userRepository.findUserByUsername(request.getUsername())
        .orElseThrow(() -> new UserNotFoundException("Target user not found"));
    List<Chat> existingChats = chatMemberRepository
        .findChatsByTwoUsers(currentUser.getId(), targetUser.getId());

    if (currentUser.getId().equals(targetUser.getId())) {
      throw new ChatOperationException("Chat with yourself feature in develop");
    }

    if (!existingChats.isEmpty()) {
      throw new ChatOperationException("Chat already exists between these users");
    }

    Chat chat = new Chat();
    createChatMember(chat, currentUser);
    createChatMember(chat, targetUser);
    Chat savedChat = chatRepository.save(chat);

    if (savedChat.getId() == null || !chatRepository.existsById(savedChat.getId())) {
      throw new ChatOperationException("Failed to create chat");
    }

    if (savedChat.getChatMembers().isEmpty()
        || !chatMemberRepository.existsByChatId(savedChat.getId())) {
      throw new ChatOperationException("Failed to create chat members");
    }

    return mapToChatDto(savedChat, "CREATE", currentUser);
  }

  @Transactional
  public ChatResponse deleteChat(Long chatId, Principal principal) {
    User currentUser = userService.getCurrentUser(principal);
    Chat chat = chatRepository.findById(chatId)
        .orElseThrow(() -> new ChatNotFoundException("Chat not found"));
    validateChatAccess(currentUser.getId(), chatId);
    chatRepository.delete(chat);

    if (chatRepository.existsById(chatId)) {
      throw new ChatOperationException("Failed to delete chat");
    }

    if (chatMemberRepository.existsByChatId(chatId)) {
      throw new ChatOperationException("Failed to delete chat members");
    }

    return mapToChatDto(chat, "DELETE", currentUser);
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

  private ChatResponse mapToChatDto(Chat chat, String action, User currentUser) {
    return new ChatResponse(chat.getId(), chat.getName(currentUser), action);
  }
}
