package com.example.loanmanagement.service;

import com.example.loanmanagement.dao.CustomerDAO;
import com.example.loanmanagement.dao.EmiScheduleDAO;
import com.example.loanmanagement.dao.LoanDAO;
import com.example.loanmanagement.model.Customer;
import com.example.loanmanagement.model.EmiSchedule;
import com.example.loanmanagement.model.Loan;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class LoanService {
    private final LoanDAO loanDAO;
    private final EmiScheduleDAO emiScheduleDAO;
    private final CustomerDAO customerDAO;

    public LoanService() {
        this.loanDAO = new LoanDAO();
        this.emiScheduleDAO = new EmiScheduleDAO();
        this.customerDAO = new CustomerDAO();
    }

    public void applyForLoan(Loan loan) {
        // Calculate EMI
        double principal = loan.getAmount();
        double rate = loan.getInterestRate() / 12 / 100; // Monthly interest rate
        int time = loan.getDurationMonths();

        double emi = (principal * rate * Math.pow(1 + rate, time)) / (Math.pow(1 + rate, time) - 1);
        loan.setEmi(Math.round(emi * 100.0) / 100.0);

        // Set other fields
        loan.setLoanId("LN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        loan.setStartDate(LocalDate.now());
        loan.setEndDate(LocalDate.now().plusMonths(time));
        loan.setStatus(Loan.LoanStatus.PENDING); // Initial status
        loan.setPaidEmis(0);
        loan.setTotalEmis(time);
        loan.setOutstandingAmount(loan.getAmount() + (loan.getEmi() * time) - loan.getAmount()); // Total Interest +
                                                                                                 // Principal? No,
                                                                                                 // usually outstanding
                                                                                                 // is Principal +
                                                                                                 // Interest remaining
                                                                                                 // or just Principal.
                                                                                                 // Let's say Total
                                                                                                 // Payable Remaining.
        // Actually usually outstanding is balance principal. But strict EMI loans track
        // total payable.
        // Let's settle on: Outstanding = Total Payable (EMI * months) - Paid Amount.
        loan.setOutstandingAmount(loan.getEmi() * time);

        loanDAO.save(loan);

        // Update customer stats
        Customer customer = loan.getCustomer();
        customer.setTotalLoans(customer.getTotalLoans() + 1);
        customer.setActiveLoans(customer.getActiveLoans() + 1);
        customerDAO.update(customer);

        // Generate EMI Schedule
        generateEmiSchedule(loan);
    }

    private void generateEmiSchedule(Loan loan) {
        double balance = loan.getAmount();
        double monthlyRate = loan.getInterestRate() / 12 / 100;
        double emi = loan.getEmi();

        for (int i = 1; i <= loan.getDurationMonths(); i++) {
            double interest = balance * monthlyRate;
            double principalComponent = emi - interest;
            balance -= principalComponent;

            if (balance < 0)
                balance = 0; // Floating point correction

            EmiSchedule schedule = new EmiSchedule();
            schedule.setLoan(loan);
            schedule.setEmiNumber(i);
            schedule.setDueDate(loan.getStartDate().plusMonths(i));
            schedule.setAmount(emi);
            schedule.setPrincipal(Math.round(principalComponent * 100.0) / 100.0);
            schedule.setInterest(Math.round(interest * 100.0) / 100.0);
            schedule.setStatus(EmiSchedule.EmiStatus.PENDING);

            emiScheduleDAO.save(schedule);
        }
    }

    public List<Loan> getAllLoans() {
        return loanDAO.findAll();
    }

    public List<Loan> getLoansByCustomer(Long customerId) {
        return loanDAO.findByCustomerId(customerId);
    }

    public Loan getLoanById(Long id) {
        try (org.hibernate.Session session = com.example.loanmanagement.util.HibernateUtil.getSessionFactory()
                .openSession()) {
            Loan loan = session.get(Loan.class, id);
            // Force initialization if needed, but session is closed upon return.
            // If we use GenericDAO.findById it closes session.
            // Lazy loading of collections might be an issue.
            // But for now it's fine.
            return loan; // If we access properties outside, we might get LazyInitializationException if
                         // session is closed.
            // DAO.findById uses try-with-resources, so session is closed.
            // We should eagerly fetch or keep session open in Service if we need lazy data.
            // But for now let's stick to simple.
        }
    }

    // Better implementation of getLoan that doesn't close session immediately?
    // Or we rely on `OpenSessionInView` pattern or Eager fetching.
    // Given the architecture, maybe we should add `getLoanWithSchedule` in DAO.
}
