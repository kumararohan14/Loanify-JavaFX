package com.example.loanmanagement.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "loans")
@Data
@NoArgsConstructor
public class Loan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "loan_id", unique = true, nullable = false)
    private String loanId;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Enumerated(EnumType.STRING)
    private LoanType type;

    private Double amount;

    @Column(name = "interest_rate")
    private Double interestRate;

    @Column(name = "document_charges")
    private Double documentCharges;

    @Column(name = "duration_months")
    private Integer durationMonths;

    private Double emi;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    private LoanStatus status;

    @Column(name = "paid_emis")
    private int paidEmis;

    @Column(name = "total_emis")
    private int totalEmis;

    @Column(name = "outstanding_amount")
    private Double outstandingAmount;

    public enum LoanType {
        DAY(30.0), WEEK(30.0);

        private final double interestRate;

        LoanType(double interestRate) {
            this.interestRate = interestRate;
        }

        public double getInterestRate() {
            return interestRate;
        }
    }

    public enum LoanStatus {
        ACTIVE, CLOSED, OVERDUE, PENDING
    }
}
