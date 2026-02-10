package com.example.loanmanagement.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "emi_schedules")
@Data
@NoArgsConstructor
public class EmiSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    @Column(name = "emi_number")
    private int emiNumber;

    @Column(name = "due_date")
    private LocalDate dueDate;

    private Double amount;
    private Double principal;
    private Double interest;

    @Enumerated(EnumType.STRING)
    private EmiStatus status;

    @Column(name = "paid_date")
    private LocalDate paidDate;

    private Double penalty;

    public enum EmiStatus {
        PAID, PENDING, OVERDUE
    }
}
