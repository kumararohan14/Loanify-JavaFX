package com.example.loanmanagement.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "customers")
@Data
@NoArgsConstructor
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", unique = true, nullable = false)
    private String customerId;

    @Column(nullable = false)
    private String name;

    private String email;
    private String phone;
    private String nic;
    private String address;

    @Column(name = "active_loans")
    private int activeLoans;

    @Column(name = "total_loans")
    private int totalLoans;

    @Enumerated(EnumType.STRING)
    private Status status;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public enum Status {
        ACTIVE, INACTIVE
    }
}
