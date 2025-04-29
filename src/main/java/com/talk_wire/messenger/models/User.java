package com.talk_wire.messenger.models;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Table(name="users")
@Entity
public class User {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column
    private String username;
    @Column
    private String email;
    @Column
    private String password;

}
