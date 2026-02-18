package com.example.loanmanagement.service;

import com.example.loanmanagement.exception.SmsSendingException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class TextLkSmsService implements SmsService {

    private String apiUrl;
    private String apiToken;
    private String senderId;

    public TextLkSmsService() {
        loadConfig();
    }

    private void loadConfig() {
        try (java.io.InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                System.out.println("Sorry, unable to find config.properties");
                return;
            }
            java.util.Properties prop = new java.util.Properties();
            prop.load(input);

            this.apiUrl = prop.getProperty("textlk.api_url");
            this.apiToken = prop.getProperty("textlk.api_token");
            this.senderId = prop.getProperty("textlk.sender_id");

        } catch (java.io.IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void sendSms(String phoneNumber, String messageBody) throws SmsSendingException {
        if (apiUrl == null || apiToken == null || senderId == null) {
            throw new SmsSendingException("Text.lk configuration is missing in config.properties");
        }

        try {
            // Clean phone number (remove + if present, ensuring it matches 94...)
            // API example uses "9471..." format for recipient
            String recipient = phoneNumber.replace("+", "").trim();

            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);

            // Escape message for JSON (simple escape)
            String escapedMessage = messageBody.replace("\"", "\\\"").replace("\n", "\\n");

            String jsonInputString = String.format(
                    "{\"api_token\":\"%s\",\"recipient\":\"%s\",\"sender_id\":\"%s\",\"type\":\"plain\",\"message\":\"%s\"}",
                    apiToken, recipient, senderId, escapedMessage);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == 200 || responseCode == 201 || responseCode == 202) {
                System.out.println("SMS sent successfully to " + recipient);
            } else {
                throw new SmsSendingException("Failed to send SMS. HTTP Code: " + responseCode);
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new SmsSendingException("Error sending SMS: " + e.getMessage(), e);
        }
    }
}
