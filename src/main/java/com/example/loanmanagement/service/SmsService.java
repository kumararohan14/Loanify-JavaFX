package com.example.loanmanagement.service;

import com.example.loanmanagement.exception.SmsSendingException;

public interface SmsService {
    void sendSms(String phoneNumber, String message) throws SmsSendingException;
}
