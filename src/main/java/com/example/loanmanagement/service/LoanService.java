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
        System.out.println("DEBUG: LoanService INSTANTIATED - VERSION WITH DAY/WEEK LOGIC");
        this.loanDAO = new LoanDAO();
        this.emiScheduleDAO = new EmiScheduleDAO();
        this.customerDAO = new CustomerDAO();
    }

    public void applyForLoan(Loan loan) {
        // Calculate "Installment" (mapped to EMI field)
        double principal = loan.getAmount();
        int duration = loan.getDurationMonths(); // Storing Days/Weeks count here

        if (loan.getType() == Loan.LoanType.DAY || loan.getType() == Loan.LoanType.WEEK) {
            // UNIQUE CALCULATION (Not Standard EMI)
            // The loan.getInterestRate() is the EFFECTIVE rate for the full term
            // (calculated by Controller)
            // Interest = Principal * (Rate / 100)

            double effectiveRate = loan.getInterestRate();
            double totalInterest = principal * (effectiveRate / 100.0);

            double documentCharges = loan.getDocumentCharges() != null ? loan.getDocumentCharges() : 0.0;
            double totalPayable = principal + totalInterest + documentCharges;

            double installment = totalPayable / duration;

            loan.setEmi(Math.round(installment * 100.0) / 100.0);
            loan.setPaidEmis(0);
            loan.setTotalEmis(duration);
            loan.setOutstandingAmount(Math.round(totalPayable * 100.0) / 100.0);
            loan.setStatus(Loan.LoanStatus.PENDING);
        } else {
            // Standard Reducing Balance Logic (Legacy/Fallback)
            // ... strict legacy logic ...
            double rate = loan.getInterestRate() / 12 / 100; // Monthly interest rate
            double emi = (principal * rate * Math.pow(1 + rate, duration)) / (Math.pow(1 + rate, duration) - 1);
            loan.setEmi(Math.round(emi * 100.0) / 100.0);
            loan.setPaidEmis(0);
            loan.setTotalEmis(duration);
            loan.setOutstandingAmount(loan.getEmi() * duration);
            loan.setStatus(Loan.LoanStatus.PENDING);
        }

        loan.setLoanId("LN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        // Start/End are set by Controller

        loanDAO.save(loan);

        // Update customer stats
        Customer customer = loan.getCustomer();
        customer.setTotalLoans(customer.getTotalLoans() + 1);
        customer.setActiveLoans(customer.getActiveLoans() + 1);
        customerDAO.update(customer);

        // Generate Schedule
        generateEmiSchedule(loan);
    }

    private void generateEmiSchedule(Loan loan) {
        // Ensure strictly only DAY or WEEK logic runs.
        if (loan.getType() == Loan.LoanType.DAY || loan.getType() == Loan.LoanType.WEEK) {
            // Flat Rate Schedule Logic
            double installment = loan.getEmi();
            double principalComponent = loan.getAmount() / loan.getTotalEmis();
            double interestComponent = installment - principalComponent;

            for (int i = 1; i <= loan.getTotalEmis(); i++) {
                EmiSchedule schedule = new EmiSchedule();
                schedule.setLoan(loan);
                schedule.setEmiNumber(i);

                if (loan.getType() == Loan.LoanType.DAY) {
                    schedule.setDueDate(loan.getStartDate().plusDays(i));
                    System.out.println("DEBUG: Gen Schedule DAY " + i + " " + schedule.getDueDate());
                } else { // WEEK
                    schedule.setDueDate(loan.getStartDate().plusWeeks(i));
                    System.out.println("DEBUG: Gen Schedule WEEK " + i + " " + schedule.getDueDate());
                }

                schedule.setAmount(installment);
                schedule.setPrincipal(Math.round(principalComponent * 100.0) / 100.0);
                schedule.setInterest(Math.round(interestComponent * 100.0) / 100.0);
                schedule.setStatus(EmiSchedule.EmiStatus.PENDING);

                emiScheduleDAO.save(schedule);
            }
        } else {
            System.err.println("ERROR: Unknown Loan Type for Schedule Generation: " + loan.getType());
        }
    }

    public List<Loan> getAllLoans() {
        return loanDAO.findAll();
    }

    public List<EmiSchedule> getEmiSchedule(Long loanId) {
        return emiScheduleDAO.findByLoanId(loanId);
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

    // ================================
    // UPDATE EXISTING LOAN
    // ================================
    public void updateLoan(Loan loan) {
        loanDAO.update(loan);
    }

    // ================================
    // APPROVE LOAN (PENDING → ACTIVE)
    // ================================
    public void approveLoan(Loan loan) {

        if (loan.getStatus() != Loan.LoanStatus.PENDING) {
            throw new IllegalStateException("Only PENDING loans can be approved.");
        }

        loan.setStatus(Loan.LoanStatus.ACTIVE);
        loanDAO.update(loan);
    }

    // ================================
    // CLOSE LOAN (ACTIVE → CLOSED)
    // ================================
    public void closeLoan(Loan loan) {

        if (loan.getStatus() != Loan.LoanStatus.ACTIVE) {
            throw new IllegalStateException("Only ACTIVE loans can be closed.");
        }

        if (loan.getOutstandingAmount() > 0) {
            throw new IllegalStateException("Loan cannot be closed. Outstanding amount exists.");
        }

        loan.setStatus(Loan.LoanStatus.CLOSED);
        loanDAO.update(loan);
    }

    // ================================
    // AUTO CHECK OVERDUE LOANS
    // ================================
    public void checkAndUpdateOverdueLoans() {

        List<Loan> activeLoans = loanDAO.findByStatus(Loan.LoanStatus.ACTIVE);

        for (Loan loan : activeLoans) {

            // If end date passed and still has outstanding amount
            if (loan.getEndDate().isBefore(LocalDate.now())
                    && loan.getOutstandingAmount() > 0) {

                loan.setStatus(Loan.LoanStatus.OVERDUE);
                loanDAO.update(loan);
            }
        }
    }

    // ================================
    // GET LOANS BY STATUS
    // ================================
    public List<Loan> getLoansByStatus(Loan.LoanStatus status) {
        return loanDAO.findByStatus(status);
    }

}
