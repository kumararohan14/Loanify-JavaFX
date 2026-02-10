package com.example.loanmanagement.dao;

import com.example.loanmanagement.model.Payment;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;

public class PaymentDAO extends GenericDAOImpl<Payment, Long> {

    public PaymentDAO() {
        super(Payment.class);
    }

    public List<Payment> findByLoanId(Long loanId) {
        try (Session session = getSession()) {
            Query<Payment> query = session.createQuery("FROM Payment WHERE loan.id = :loanId ORDER BY date DESC",
                    Payment.class);
            query.setParameter("loanId", loanId);
            return query.list();
        }
    }

    public List<Payment> getRecentPayments(int limit) {
        try (Session session = getSession()) {
            Query<Payment> query = session.createQuery("FROM Payment ORDER BY date DESC", Payment.class);
            query.setMaxResults(limit);
            return query.list();
        }
    }
}
