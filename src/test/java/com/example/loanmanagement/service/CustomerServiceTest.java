package com.example.loanmanagement.service;

import com.example.loanmanagement.dao.CustomerDAO;
import com.example.loanmanagement.exception.SmsSendingException;
import com.example.loanmanagement.model.Customer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerDAO customerDAO;

    @Mock
    private SmsService smsService;

    private CustomerService customerService;

    @BeforeEach
    void setUp() {
        customerService = new CustomerService(customerDAO, smsService);
    }

    @Test
    void testAddCustomer_Success() throws SmsSendingException {
        Customer customer = new Customer();
        customer.setNic("123456789V");
        customer.setPhone("0771234567");

        when(customerDAO.findByNic("123456789V")).thenReturn(null);

        assertDoesNotThrow(() -> customerService.addCustomer(customer));

        verify(customerDAO, times(1)).save(customer);
        verify(smsService, times(1)).sendSms(anyString(), anyString());
        assertNotNull(customer.getOtp());
        assertEquals(Customer.Status.PENDING, customer.getStatus());
    }

    @Test
    void testAddCustomer_DuplicateNIC() {
        Customer customer = new Customer();
        customer.setNic("123456789V");

        when(customerDAO.findByNic("123456789V")).thenReturn(new Customer());

        assertThrows(IllegalArgumentException.class, () -> customerService.addCustomer(customer));

        verify(customerDAO, never()).save(any(Customer.class));
    }

    @Test
    void testVerifyOtp_Success() {
        Customer customer = new Customer();
        customer.setNic("123456789V");
        customer.setOtp("123456");
        customer.setOtpExpiry(LocalDateTime.now().plusMinutes(5));

        when(customerDAO.findByNic("123456789V")).thenReturn(customer);

        boolean result = customerService.verifyOtp("123456789V", "123456");

        assertTrue(result);
        assertEquals(Customer.Status.ACTIVE, customer.getStatus());
        assertNull(customer.getOtp());
        verify(customerDAO, times(1)).update(customer);
    }

    @Test
    void testVerifyOtp_InvalidOtp() {
        Customer customer = new Customer();
        customer.setNic("123456789V");
        customer.setOtp("123456");
        customer.setOtpExpiry(LocalDateTime.now().plusMinutes(5));

        when(customerDAO.findByNic("123456789V")).thenReturn(customer);

        boolean result = customerService.verifyOtp("123456789V", "654321");

        assertFalse(result);
        verify(customerDAO, never()).update(any(Customer.class));
    }

    @Test
    void testVerifyOtp_Expired() {
        Customer customer = new Customer();
        customer.setNic("123456789V");
        customer.setOtp("123456");
        customer.setOtpExpiry(LocalDateTime.now().minusMinutes(1)); // Expired

        when(customerDAO.findByNic("123456789V")).thenReturn(customer);

        assertThrows(IllegalArgumentException.class, () -> customerService.verifyOtp("123456789V", "123456"));
    }

    @Test
    void testVerifyOtp_CustomerNotFound() {
        when(customerDAO.findByNic("123456789V")).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> customerService.verifyOtp("123456789V", "123456"));
    }

    @Test
    void testResendOtp_Success() throws SmsSendingException {
        Customer customer = new Customer();
        customer.setNic("123456789V");
        customer.setPhone("0771234567");

        when(customerDAO.findByNic("123456789V")).thenReturn(customer);

        assertDoesNotThrow(() -> customerService.resendOtp("123456789V"));

        verify(customerDAO, times(1)).update(customer);
        verify(smsService, times(1)).sendSms(anyString(), anyString());
        assertNotNull(customer.getOtp());
    }

    @Test
    void testResendOtp_CustomerNotFound() {
        when(customerDAO.findByNic("123456789V")).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> customerService.resendOtp("123456789V"));
    }

    @Test
    void testDeleteCustomer_Success() {
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setNic("123456789V");
        customer.setActiveLoans(0);

        when(customerDAO.findById(1L)).thenReturn(customer);

        assertDoesNotThrow(() -> customerService.deleteCustomer(1L));

        verify(customerDAO, times(1)).delete(customer);
    }

    @Test
    void testDeleteCustomer_ActiveLoans() {
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setActiveLoans(1);

        when(customerDAO.findById(1L)).thenReturn(customer);

        assertThrows(IllegalStateException.class, () -> customerService.deleteCustomer(1L));

        verify(customerDAO, never()).delete(any(Customer.class));
    }

    @Test
    void testDeleteCustomer_NotFound() {
        when(customerDAO.findById(1L)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> customerService.deleteCustomer(1L));
    }
}
