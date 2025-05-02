package com.talkwire.messenger.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * Entity class representing a user in the system.
 */
@Data
@Table(name = "users")
@Entity
public class User {

  /**
   * The unique identifier of the user.
   */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /**
   * The username of the user.
   */
  @Column
  private String username;

  /**
   * The email address of the user.
   */
  @Column
  private String email;

  /**
   * The encrypted password of the user.
   */
  @Column
  private String password;
}

