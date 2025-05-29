package com.talkwire.messenger.service;

import com.talkwire.messenger.dto.message.*;
import com.talkwire.messenger.exception.chat.*;
import com.talkwire.messenger.exception.message.*;
import com.talkwire.messenger.model.*;
import com.talkwire.messenger.repository.*;
import jakarta.transaction.Transactional;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageService {
  private final ChatRepository chatRepository;
  private final ChatMemberRepository chatMemberRepository;
  private final MessageRepository messageRepository;
  private final UserService userService;

  public List<MessageResponse> getMessages(Long chatId, Principal principal) {
    User currentUser = userService.getCurrentUser(principal);
    validateChatAccess(currentUser.getId(), chatId);

    return messageRepository.findByChatId(chatId).stream()
        .map(message -> mapToMessageDto(message, "GET"))
        .toList();
  }

  public MessageResponse createMessage(
      Long chatId, CreateMessageRequest request, Principal principal) {
    User currentUser = userService.getCurrentUser(principal);
    validateChatAccess(currentUser.getId(), chatId);

    Chat chat = chatRepository.findById(chatId)
        .orElseThrow(() -> new ChatNotFoundException("Chat not found"));

    Message message = new Message();
    message.setChat(chat);
    message.setUser(currentUser);
    message.setText(request.getText());
    message.setCreatedAt(LocalDateTime.now());

    Message savedMessage = messageRepository.save(message);
    validateMessageExists(savedMessage.getId());
    return mapToMessageDto(savedMessage, "CREATE");
  }

  public MessageResponse updateMessage(
      Long chatId,
      Long messageId,
      UpdateMessageRequest request,
      Principal principal) {
    User currentUser = userService.getCurrentUser(principal);
    validateChatAccess(currentUser.getId(), chatId);

    Message message = messageRepository.findById(messageId)
        .orElseThrow(() -> new MessageNotFoundException("Message not found"));

    validateMessageOwnership(currentUser, message);

    message.setText(request.getNewContent());
    messageRepository.save(message);

    return mapToMessageDto(message, "UPDATE");
  }

  @Transactional
  public MessageResponse deleteMessage(Long chatId, Long messageId, Principal principal) {
    User currentUser = userService.getCurrentUser(principal);
    Message message = messageRepository.findById(messageId)
        .orElseThrow(() -> new MessageNotFoundException("Message not found"));

    validateChatAccess(currentUser.getId(), chatId);
    validateMessageOwnership(currentUser, message);
    messageRepository.delete(message);
    validateMessageDeleted(messageId);

    return mapToMessageDto(message, "DELETE");
  }

  private void validateChatAccess(Long userId, Long chatId) {
    if (!chatMemberRepository.existsByUserIdAndChatId(userId, chatId)) {
      throw new ChatAccessDeniedException("Access denied: not a member of this chat");
    }
  }

  private void validateMessageOwnership(User user, Message message) {
    if (!message.getUser().getId().equals(user.getId())) {
      throw new MessageOperationException("Access denied: cannot modify others' messages");
    }
  }

  private MessageResponse mapToMessageDto(Message message, String action) {
    return new MessageResponse(
        message.getId(),
        message.getText(),
        message.getCreatedAt(),
        message.getUser().getId(),
        message.getUser().getUsername(),
        message.getChat().getId(),
        action);
  }

  private void validateMessageExists(Long messageId) {
    if (!messageRepository.existsById(messageId)) {
      throw new MessageOperationException("Failed to verify message operation:"
          + " message not found after operation");
    }
  }

  private void validateMessageDeleted(Long messageId) {
    if (messageRepository.existsById(messageId)) {
      throw new MessageOperationException("Failed to delete message:"
          + " message still exists after deletion");
    }
  }
}
