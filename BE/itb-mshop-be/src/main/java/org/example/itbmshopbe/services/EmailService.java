package org.example.itbmshopbe.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;

    public void sendEmail(String to, String subject, String verificationUrl) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);

            String htmlContent = "<!DOCTYPE html>" +
                    "<html>" +
                    "<body style='font-family: Arial, sans-serif;'>" +
                    "  <h1 style='color: #4CAF50; text-align: center;'>Welcome to ITBMS-Shop</h1>" +
                    "  <p style='font-size: 16px; text-align: center;'>Please click the link below to verify your account:</p>" +
                    "  <p style='text-align: center;'>" +
                    "    <a href='" + verificationUrl + "' " +
                    "       style='background-color:#4CAF50; color:white; padding:10px 20px; text-decoration:none; border-radius:5px;'>" +
                    "       Verify Your Account</a>" +
                    "  </p>" +
                    "  <br/>" +
                    "  <p style='text-align: center;'>If you did not request this, please ignore this email.</p>" +
                    "</body>" +
                    "</html>";

            helper.setText(htmlContent, true); // true = send as HTML

            mailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }
}
