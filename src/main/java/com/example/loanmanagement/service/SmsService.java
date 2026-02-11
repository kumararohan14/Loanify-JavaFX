package com.example.loanmanagement.service;

public interface SmsService {
    void sendSms(String phoneNumber, String message);
}
