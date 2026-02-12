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

   public boolean recordPayment(Payment payment) {
    try {
        Loan loan = loanDAO.findById(payment.getLoan().getId());
        if (loan == null) {
            return false; // Loan not found
        }

        // Find the EMI that this payment covers
        List<EmiSchedule> emis = emiScheduleDAO.findByLoanId(loan.getId());
        double amountRemaining = payment.getAmount();

        for (EmiSchedule emi : emis) {
            if (emi.getStatus() != EmiSchedule.EmiStatus.PAID) {
                if (amountRemaining >= emi.getAmount()) {
                    emi.setStatus(EmiSchedule.EmiStatus.PAID);
                    emi.setPaidDate(LocalDate.now());
                    amountRemaining -= emi.getAmount();
                    loan.setPaidEmis(loan.getPaidEmis() + 1);
                } else {
                    // Partial payment not allowed
                    break;
                }
                emiScheduleDAO.update(emi);
            }
            if (amountRemaining <= 0) break;
        }

        // Update loan outstanding
        loan.setOutstandingAmount(loan.getOutstandingAmount() - payment.getAmount());
        if (loan.getPaidEmis() >= loan.getTotalEmis()) {
            loan.setStatus(Loan.LoanStatus.CLOSED);
        }
        loanDAO.update(loan);

        // Save payment
        payment.setPaymentId("PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        payment.setDate(LocalDate.now());
        payment.setStatus("COMPLETED");
        paymentDAO.save(payment);

        return true; // Success
    } catch (Exception e) {
        e.printStackTrace();
        return false; // Failed
    }
}


    public List<Payment> getPaymentsByLoan(Long loanId) {
        return paymentDAO.findByLoanId(loanId);
    }

    public List<Payment> getAllPayments() {
        return paymentDAO.findAll();
    }
}
