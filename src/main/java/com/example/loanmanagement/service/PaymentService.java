package com.example.loanmanagement.service;

import com.example.loanmanagement.dao.EmiScheduleDAO;
import com.example.loanmanagement.dao.LoanDAO;
import com.example.loanmanagement.dao.PaymentDAO;
import com.example.loanmanagement.model.EmiSchedule;
import com.example.loanmanagement.model.Loan;
import com.example.loanmanagement.model.Payment;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class PaymentService {
    private final PaymentDAO paymentDAO;
    private final LoanDAO loanDAO;
    private final EmiScheduleDAO emiScheduleDAO;

    public PaymentService() {
        this.paymentDAO = new PaymentDAO();
        this.loanDAO = new LoanDAO();
        this.emiScheduleDAO = new EmiScheduleDAO();
    }

    public void recordPayment(Payment payment) {
        Loan loan = loanDAO.findById(payment.getLoan().getId());
        if (loan == null) {
            throw new IllegalArgumentException("Loan not found");
        }

        // Logic to update loan balance and EMI status
        // Find the EMI that this payment covers
        // Since we don't have direct mapping in payment form usually,
        // we might auto-allocate to the oldest unpaid EMI.

        List<EmiSchedule> emis = emiScheduleDAO.findByLoanId(loan.getId());
        // emis are ordered by due date

        double amountRemaining = payment.getAmount();

        for (EmiSchedule emi : emis) {
            if (emi.getStatus() != EmiSchedule.EmiStatus.PAID) {
                if (amountRemaining >= emi.getAmount()) {
                    emi.setStatus(EmiSchedule.EmiStatus.PAID);
                    emi.setPaidDate(LocalDate.now());
                    amountRemaining -= emi.getAmount();
                    // Update Loan paid emis count
                    loan.setPaidEmis(loan.getPaidEmis() + 1);
                } else {
                    // Partial payment logic?
                    // For simplicity, let's assume full EMI payments or reject.
                    // Or we can just mark it as PENDING but with partial amount paid?
                    // The entity doesn't have "paidAmount" field on EmiSchedule.
                    // Let's assume strict EMI payment amounts for now.
                    break;
                }
                emiScheduleDAO.update(emi);
            }
            if (amountRemaining <= 0)
                break;
        }

        loan.setOutstandingAmount(loan.getOutstandingAmount() - payment.getAmount());
        if (loan.getPaidEmis() >= loan.getTotalEmis()) {
            loan.setStatus(Loan.LoanStatus.CLOSED);
        }

        loanDAO.update(loan);

        payment.setPaymentId("PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        payment.setDate(LocalDate.now());
        payment.setStatus("COMPLETED");
        paymentDAO.save(payment);
    }

    public List<Payment> getPaymentsByLoan(Long loanId) {
        return paymentDAO.findByLoanId(loanId);
    }

    public List<Payment> getAllPayments() {
        return paymentDAO.findAll();
    }
}
