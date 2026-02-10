package com.example.loanmanagement.service;

import com.example.loanmanagement.dao.CustomerDAO;
import com.example.loanmanagement.model.Customer;

import java.util.List;

public class CustomerService {
    private final CustomerDAO customerDAO;

    public CustomerService() {
        this.customerDAO = new CustomerDAO();
    }

    public void addCustomer(Customer customer) {
        if (customerDAO.findByNic(customer.getNic()) != null) {
            throw new IllegalArgumentException("Customer with this NIC already exists");
        }
        customer.setStatus(Customer.Status.ACTIVE);
        customerDAO.save(customer);
    }

    public void updateCustomer(Customer customer) {
        customerDAO.update(customer);
    }

    public List<Customer> getAllCustomers() {
        return customerDAO.findAll();
    }

    public Customer getCustomerById(Long id) {
        return customerDAO.findById(id);
    }
}
