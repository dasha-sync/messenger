package com.talkwire.messenger.model;

import jakarta.persistence.*;
import java.util.*;
import lombok.Data;

@Data
@Table(name = "users")
@Entity
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column
  private String username;

  @Column
  private String email;

  @Column
  private String password;

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Message> messages = new ArrayList<>();

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ChatMember> chatMembers = new ArrayList<>();

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Contact> contacts = new ArrayList<>();

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Request> requests = new ArrayList<>();

  public void addChatMembers(ChatMember chatMember) {
    chatMembers.add(chatMember);
    chatMember.setUser(this);
  }

  public void addMessages(Message message) {
    messages.add(message);
    message.setUser(this);
  }

  public void addContacts(Contact contact) {
    contacts.add(contact);
    contact.setUser(this);
  }

  public void addRequest(Request request) {
    requests.add(request);
    request.setUser(this);
  }
}
