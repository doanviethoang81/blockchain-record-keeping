package com.example.blockchain.record.keeping.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name ="action_changes")
public class ActionChange {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "log_id")
    private Log log;

    @Column(name = "field_name")
    private String fieldName;

    @Column(name = "old_value")
    private String oldValue;

    @Column(name = "new_value ")
    private String newValue;
}
