package com.example.loanmanagement.dao;

import com.example.loanmanagement.model.User;
import org.hibernate.Session;
import org.hibernate.query.Query;

public class UserDAO extends GenericDAOImpl<User, Long> {

    public UserDAO() {
        super(User.class);
    }

    public User findByEmail(String email) {
        try (Session session = getSession()) {
            Query<User> query = session.createQuery("FROM User WHERE email = :email", User.class);
            query.setParameter("email", email);
            return query.uniqueResult();
        }
    }
}
