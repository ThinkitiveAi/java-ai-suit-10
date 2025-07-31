package com.healthfirst.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * Send verification email to provider
     */
    public void sendVerificationEmail(String toEmail, String firstName, UUID providerId) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Provider Registration - Email Verification Required");
            
            String verificationUrl = generateVerificationUrl(providerId);
            String emailBody = buildVerificationEmailBody(firstName, verificationUrl);
            
            message.setText(emailBody);
            
            mailSender.send(message);
            logger.info("Verification email sent successfully to: {}", toEmail);
            
        } catch (Exception e) {
            logger.error("Failed to send verification email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send verification email", e);
        }
    }

    /**
     * Send welcome email after verification
     */
    public void sendWelcomeEmail(String toEmail, String firstName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Welcome to HealthFirst - Account Verified");
            
            String emailBody = buildWelcomeEmailBody(firstName);
            message.setText(emailBody);
            
            mailSender.send(message);
            logger.info("Welcome email sent successfully to: {}", toEmail);
            
        } catch (Exception e) {
            logger.error("Failed to send welcome email to: {}", toEmail, e);
        }
    }

    /**
     * Send rejection notification email
     */
    public void sendRejectionEmail(String toEmail, String firstName, String reason) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Provider Registration - Application Status");
            
            String emailBody = buildRejectionEmailBody(firstName, reason);
            message.setText(emailBody);
            
            mailSender.send(message);
            logger.info("Rejection email sent successfully to: {}", toEmail);
            
        } catch (Exception e) {
            logger.error("Failed to send rejection email to: {}", toEmail, e);
        }
    }

    private String generateVerificationUrl(UUID providerId) {
        // In a real application, this would be your frontend URL
        return "http://localhost:3000/verify-email?token=" + providerId.toString();
    }

    private String buildVerificationEmailBody(String firstName, String verificationUrl) {
        return String.format(
            "Dear %s,\n\n" +
            "Thank you for registering as a healthcare provider with HealthFirst.\n\n" +
            "To complete your registration, please verify your email address by clicking the link below:\n" +
            "%s\n\n" +
            "This link will expire in 24 hours for security purposes.\n\n" +
            "If you did not register for this account, please ignore this email.\n\n" +
            "Best regards,\n" +
            "The HealthFirst Team\n\n" +
            "Note: This is an automated email. Please do not reply to this message.",
            firstName, verificationUrl
        );
    }

    private String buildWelcomeEmailBody(String firstName) {
        return String.format(
            "Dear %s,\n\n" +
            "Congratulations! Your HealthFirst provider account has been successfully verified.\n\n" +
            "You can now access your provider dashboard and start managing your practice.\n\n" +
            "If you have any questions or need assistance, please contact our support team.\n\n" +
            "Best regards,\n" +
            "The HealthFirst Team",
            firstName
        );
    }

    private String buildRejectionEmailBody(String firstName, String reason) {
        return String.format(
            "Dear %s,\n\n" +
            "Thank you for your interest in joining HealthFirst as a healthcare provider.\n\n" +
            "After reviewing your application, we regret to inform you that we cannot approve your registration at this time.\n\n" +
            "Reason: %s\n\n" +
            "If you believe this decision was made in error or if you have additional documentation to support your application, " +
            "please contact our support team.\n\n" +
            "Best regards,\n" +
            "The HealthFirst Team",
            firstName, reason != null ? reason : "Application does not meet our current requirements"
        );
    }
} 