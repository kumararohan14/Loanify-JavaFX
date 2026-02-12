package com.example.loanmanagement.dao;

import com.example.loanmanagement.model.Loan;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;

public class LoanDAO extends GenericDAOImpl<Loan, Long> {

    public LoanDAO() {
        super(Loan.class);
    }

    public List<Loan> findByCustomerId(Long customerId) {
        try (Session session = getSession()) {
            Query<Loan> query = session.createQuery("FROM Loan WHERE customer.id = :customerId", Loan.class);
            query.setParameter("customerId", customerId);
            return query.list();
        }
    }

    public List<Loan> findActiveLoans() {
        try (Session session = getSession()) {
            Query<Loan> query = session.createQuery("FROM Loan WHERE status = 'ACTIVE'", Loan.class);
            return query.list();
        }
    }

    public Loan findByIdWithCustomer(Long id) {
        try (Session session = getSession()) {
            Query<Loan> query = session.createQuery("FROM Loan l JOIN FETCH l.customer WHERE l.id = :id", Loan.class);
            query.setParameter("id", id);
            return query.uniqueResult();
        }
    }

    public List<Loan> findByStatus(Loan.LoanStatus status) {

    try (org.hibernate.Session session =
                 com.example.loanmanagement.util.HibernateUtil
                         .getSessionFactory()
                         .openSession()) {

        return session.createQuery(
                "FROM Loan WHERE status = :status", Loan.class)
                .setParameter("status", status)
                .getResultList();
    }
}

}
