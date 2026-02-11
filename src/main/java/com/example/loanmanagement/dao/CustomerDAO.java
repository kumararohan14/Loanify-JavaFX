package com.example.loanmanagement.dao;

import com.example.loanmanagement.model.Customer;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;

public class CustomerDAO extends GenericDAOImpl<Customer, Long> {

    public CustomerDAO() {
        super(Customer.class);
    }

    public Customer findByNic(String nic) {
        try (Session session = getSession()) {
            Query<Customer> query = session.createQuery("FROM Customer WHERE nic = :nic", Customer.class);
            query.setParameter("nic", nic);
            return query.uniqueResult();
        }
    }

    public List<Customer> findActiveCustomers() {
        try (Session session = getSession()) {
            Query<Customer> query = session.createQuery("FROM Customer WHERE status = 'ACTIVE'", Customer.class);
            return query.list();
        }
    }
}
