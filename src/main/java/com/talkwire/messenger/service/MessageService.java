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
        .map(this::mapToMessageDto)
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

    messageRepository.save(message);
    return mapToMessageDto(message);
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

    return mapToMessageDto(message);
  }

  @Transactional
  public void deleteMessage(Long chatId, Long messageId, Principal principal) {
    User currentUser = userService.getCurrentUser(principal);
    validateChatAccess(currentUser.getId(), chatId);

    Message message = messageRepository.findById(messageId)
        .orElseThrow(() -> new MessageNotFoundException("Message not found"));

    validateMessageOwnership(currentUser, message);

    messageRepository.delete(message);
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

  private MessageResponse mapToMessageDto(Message message) {
    return new MessageResponse(
        message.getId(),
        message.getText(),
        message.getCreatedAt(),
        message.getUser().getId(),
        message.getUser().getUsername(),
        message.getChat().getId()
    );
  }
}
