package com.example.loanmanagement.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payment_id", unique = true, nullable = false)
    private String paymentId;

    @ManyToOne
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    @Column(name = "customer_name")
    private String customerName;

    private Double amount;
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    private PaymentMethod method;

    private String status;

    @Column(name = "emi_number")
    private Integer emiNumber;

    private Double penalty;

    public enum PaymentMethod {
        CASH, BANK, ONLINE
    }
}
