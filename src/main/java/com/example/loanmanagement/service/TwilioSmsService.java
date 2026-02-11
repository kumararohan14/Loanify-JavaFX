package com.example.loanmanagement.service;

import com.example.loanmanagement.exception.SmsSendingException;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

public class TwilioSmsService implements SmsService {

    private String accountSid;
    private String authToken;
    private String fromPhoneNumber;

    public TwilioSmsService() {
        loadConfig();
        if (isValidConfig()) {
            try {
                Twilio.init(accountSid, authToken);
            } catch (Exception e) {
                System.err.println("Failed to initialize Twilio: " + e.getMessage());
            }
        } else {
            System.err.println("Twilio configuration is missing or invalid in config.properties");
        }
    }

    private void loadConfig() {
        try (java.io.InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                System.out.println("Sorry, unable to find config.properties");
                return;
            }
            java.util.Properties prop = new java.util.Properties();
            prop.load(input);

            this.accountSid = prop.getProperty("twilio.account_sid");
            this.authToken = prop.getProperty("twilio.auth_token");
            this.fromPhoneNumber = prop.getProperty("twilio.phone_number");

        } catch (java.io.IOException ex) {
            ex.printStackTrace();
        }
        
        System.out.println("DEBUG: Twilio Config Loaded");
        System.out.println("SID: " + (this.accountSid != null ? this.accountSid.substring(0, Math.min(4, this.accountSid.length())) + "***" : "null"));
        System.out.println("Token: " + (this.authToken != null ? "***" : "null"));
        System.out.println("Phone: " + this.fromPhoneNumber);
    }

    private boolean isValidConfig() {
        return accountSid != null && !accountSid.equals("YOUR_TWILIO_ACCOUNT_SID") &&
               authToken != null && !authToken.equals("YOUR_TWILIO_AUTH_TOKEN") &&
               fromPhoneNumber != null && !fromPhoneNumber.equals("YOUR_TWILIO_PHONE_NUMBER");
    }

    @Override
    public void sendSms(String phoneNumber, String messageBody) throws SmsSendingException {
    if (!isValidConfig()) {
        throw new SmsSendingException("Twilio credentials are not configured properly.");
    }

    try {
        Message message = Message.creator(
                new PhoneNumber(phoneNumber),
                new PhoneNumber(fromPhoneNumber),
                messageBody
        ).create();

        System.out.println("SMS sent successfully. SID: " + message.getSid());

    } catch (Exception e) {
        throw new SmsSendingException("Failed to send SMS: " + e.getMessage(), e);
    }
}

}
