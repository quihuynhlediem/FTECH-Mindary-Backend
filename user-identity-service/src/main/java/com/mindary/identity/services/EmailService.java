package com.mindary.identity.services;

import jakarta.mail.MessagingException;

import java.io.UnsupportedEncodingException;

public interface EmailService {
    public void sendOtp(String receiver, String username, String otp) throws UnsupportedEncodingException, MessagingException;
}
