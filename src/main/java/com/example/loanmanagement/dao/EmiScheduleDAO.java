package com.example.loanmanagement.dao;

import com.example.loanmanagement.model.EmiSchedule;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.time.LocalDate;
import java.util.List;

public class EmiScheduleDAO extends GenericDAOImpl<EmiSchedule, Long> {

    public EmiScheduleDAO() {
        super(EmiSchedule.class);
    }

    public List<EmiSchedule> findByLoanId(Long loanId) {
        try (Session session = getSession()) {
            Query<EmiSchedule> query = session
                    .createQuery("FROM EmiSchedule WHERE loan.id = :loanId ORDER BY dueDate ASC", EmiSchedule.class);
            query.setParameter("loanId", loanId);
            return query.list();
        }
    }

    public List<EmiSchedule> findOverdueEmis() {
        try (Session session = getSession()) {
            Query<EmiSchedule> query = session
                    .createQuery("FROM EmiSchedule WHERE dueDate < :today AND status != 'PAID'", EmiSchedule.class);
            query.setParameter("today", LocalDate.now());
            return query.list();
        }
    }
}
