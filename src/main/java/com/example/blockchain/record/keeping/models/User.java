package com.example.blockchain.record.keeping.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name ="role_id")
    private Role role;

    @ManyToOne
    @JoinColumn(name ="department_id")
    private Department department;

    @ManyToOne
    @JoinColumn(name ="university_id")
    private University university;

    @Column(name ="password")
    private String password;

    @Column(name ="email")
    private String email;

    @Column(name ="is_locked")
    private boolean isLocked;

    @Column(name="is_verified ")
    private boolean isVerified;

    @Column(name="created_at")
    private LocalDateTime createdAt;

    @Column(name="updated_at")
    private LocalDateTime updatedAt;

}
