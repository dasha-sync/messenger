package com.talkwire.messenger.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "chat_members")
@Data
@NoArgsConstructor
public class ChatMember {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "chat_id", nullable = false)
  @JsonBackReference
  private Chat chat;

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  public ChatMember(Chat chat, User user) {
    this.chat = chat;
    this.user = user;
  }
}
