package com.example.loanmanagement.service;

import com.example.loanmanagement.dao.CustomerDAO;
import com.example.loanmanagement.model.Customer;

import java.util.List;

public class CustomerService {
    private final CustomerDAO customerDAO;
    private final SmsService smsService;

    public CustomerService() {
        this.customerDAO = new CustomerDAO();
        // Use TwilioSmsService. If credentials are not set, it defaults to mock behavior.
        this.smsService = new TwilioSmsService();
    }

    public void addCustomer(Customer customer) {
        if (customerDAO.findByNic(customer.getNic()) != null) {
            throw new IllegalArgumentException("Customer with this NIC already exists");
        }
        
        // Generate 6-digit OTP
        String otp = String.format("%06d", new java.util.Random().nextInt(999999));
        customer.setOtp(otp);
        customer.setOtpExpiry(java.time.LocalDateTime.now().plusMinutes(5));
        customer.setStatus(Customer.Status.PENDING);
        
        customerDAO.save(customer);
        
        // Send OTP via SMS
        if (customer.getPhone() != null && !customer.getPhone().isEmpty()) {
            String formattedPhone = formatPhoneNumber(customer.getPhone());
            smsService.sendSms(formattedPhone, "Your OTP for Loanify is: " + otp);
        }
    }
    
    public boolean verifyOtp(String nic, String otp) {
        Customer customer = customerDAO.findByNic(nic);
        if (customer == null) {
            throw new IllegalArgumentException("Customer not found");
        }
        
        if (customer.getOtp() == null || !customer.getOtp().equals(otp)) {
            return false;
        }
        
        if (customer.getOtpExpiry().isBefore(java.time.LocalDateTime.now())) {
            throw new IllegalArgumentException("OTP has expired");
        }
        
        // OTP is valid
        customer.setStatus(Customer.Status.ACTIVE);
        customer.setOtp(null);
        customer.setOtpExpiry(null);
        customerDAO.update(customer);
        return true;
    }

    public void resendOtp(String nic) {
        Customer customer = customerDAO.findByNic(nic);
        if (customer == null) {
            throw new IllegalArgumentException("Customer not found");
        }
        
        // Generate new 6-digit OTP
        String otp = String.format("%06d", new java.util.Random().nextInt(999999));
        customer.setOtp(otp);
        customer.setOtpExpiry(java.time.LocalDateTime.now().plusMinutes(5));
        
        customerDAO.update(customer);
        
        // Send OTP via SMS
        if (customer.getPhone() != null && !customer.getPhone().isEmpty()) {
            String formattedPhone = formatPhoneNumber(customer.getPhone());
            smsService.sendSms(formattedPhone, "Your new OTP for Loanify is: " + otp);
        } else {
             throw new IllegalArgumentException("Customer does not have a phone number to receive OTP.");
        }
    }

    private String formatPhoneNumber(String phone) {
        if (phone == null) return null;
        phone = phone.trim().replaceAll("\\s+", ""); // Remove spaces
        
        // Sri Lanka specific formatting
        if (phone.startsWith("0")) {
            return "+94" + phone.substring(1);
        } else if (phone.startsWith("94")) {
            return "+" + phone;
        } else if (!phone.startsWith("+")) {
            // Assume local if no prefix, though 9 digits starting with 7 is common
            if (phone.length() == 9) {
                return "+94" + phone;
            }
        }
        return phone;
    }

    public void deleteCustomer(Long id) {
        Customer customer = customerDAO.findById(id);
        if (customer == null) {
            throw new IllegalArgumentException("Customer not found");
        }
        
        if (customer.getActiveLoans() > 0) {
            throw new IllegalStateException("Cannot delete customer with active loans.");
        }
        
        customerDAO.delete(customer);
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
