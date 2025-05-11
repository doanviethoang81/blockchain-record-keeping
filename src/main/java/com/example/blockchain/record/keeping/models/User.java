package com.example.blockchain.record.keeping.models;

import jakarta.persistence.*;
import lombok.*;

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

}
