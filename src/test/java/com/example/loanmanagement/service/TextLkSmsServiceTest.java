package com.example.loanmanagement.service;

import com.example.loanmanagement.exception.SmsSendingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TextLkSmsServiceTest {

    private TextLkSmsService smsService;
    private HttpURLConnection mockConnection;
    private OutputStream mockOutputStream;

    @BeforeEach
    void setUp() throws IOException {
        mockConnection = mock(HttpURLConnection.class);
        mockOutputStream = new ByteArrayOutputStream();

        // Subclass to override createConnection for testing
        smsService = new TextLkSmsService() {
            @Override
            protected HttpURLConnection createConnection(URL url) {
                return mockConnection;
            }
        };
    }

    @Test
    void testSendSms_Success() throws IOException, SmsSendingException {
        when(mockConnection.getOutputStream()).thenReturn(mockOutputStream);
        when(mockConnection.getResponseCode()).thenReturn(200);

        assertDoesNotThrow(() -> smsService.sendSms("0771234567", "Test Message"));

        verify(mockConnection, times(1)).setRequestMethod("POST");
        verify(mockConnection, times(1)).getResponseCode();
    }

    @Test
    void testSendSms_Failure() throws IOException {
        when(mockConnection.getOutputStream()).thenReturn(mockOutputStream);
        when(mockConnection.getResponseCode()).thenReturn(500);

        assertThrows(SmsSendingException.class, () -> smsService.sendSms("0771234567", "Test Message"));
    }

    @Test
    void testSendSms_Exception() throws IOException {
        when(mockConnection.getOutputStream()).thenThrow(new IOException("Connection failed"));

        assertThrows(SmsSendingException.class, () -> smsService.sendSms("0771234567", "Test Message"));
    }
}
