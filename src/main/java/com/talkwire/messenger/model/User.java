package com.talkwire.messenger.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.Set;
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

  @OneToMany(mappedBy = "user")
  private Set<Message> messages;

  @OneToMany(mappedBy = "user")
  private Set<ChatMember> chatMembers;

  @OneToMany(mappedBy = "user")
  private Set<UserContact> contacts;

  @OneToMany(mappedBy = "contact")
  private Set<UserContact> contactOf;
}
