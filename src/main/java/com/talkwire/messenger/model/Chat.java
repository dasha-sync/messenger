package com.talkwire.messenger.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.util.*;
import lombok.Data;

@Data
@Table(name = "chats")
@Entity
public class Chat {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column
  private String name = "default";

  @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Message> messages = new ArrayList<>();

  @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL, orphanRemoval = true)
  @JsonManagedReference
  private List<ChatMember> chatMembers = new ArrayList<>();

  public void addChatMember(ChatMember chatMember) {
    chatMembers.add(chatMember);
    chatMember.setChat(this);
  }

  public void addMessage(Message message) {
    messages.add(message);
    message.setChat(this);
  }

  public String getName(User currentUser) {
    if (chatMembers == null || chatMembers.isEmpty()) {
      return name;
    }

    if (chatMembers.size() > 2) {
      return name;
    }

    if (chatMembers.size() == 2) {
      return chatMembers.stream()
          .map(ChatMember::getUser)
          .filter(user -> !user.getId().equals(currentUser.getId()))
          .map(User::getUsername)
          .findFirst()
          .orElse(name);
    }

    return "favorites";
  }
}
