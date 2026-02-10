package com.example.loanmanagement.dao;

import com.example.loanmanagement.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

public class GenericDAOImpl<T, ID> implements GenericDAO<T, ID> {

    private Class<T> type;

    public GenericDAOImpl(Class<T> type) {
        this.type = type;
    }

    protected Session getSession() {
        return HibernateUtil.getSessionFactory().openSession();
    }

    @Override
    public void save(T entity) {
        Transaction transaction = null;
        try (Session session = getSession()) {
            transaction = session.beginTransaction();
            session.persist(entity);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }

    @Override
    public void update(T entity) {
        Transaction transaction = null;
        try (Session session = getSession()) {
            transaction = session.beginTransaction();
            session.merge(entity);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }

    @Override
    public void delete(T entity) {
        Transaction transaction = null;
        try (Session session = getSession()) {
            transaction = session.beginTransaction();
            session.remove(entity);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }

    @Override
    public T findById(ID id) {
        try (Session session = getSession()) {
            return session.get(type, id);
        }
    }

    @Override
    public List<T> findAll() {
        try (Session session = getSession()) {
            Query<T> query = session.createQuery("from " + type.getName(), type);
            return query.list();
        }
    }
}
