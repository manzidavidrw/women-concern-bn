package com.womenconcern.api.auth.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromEmail;

    @Value("${app.mail.from-name}")
    private String fromName;

    @Async
    public void sendEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Email sent to {}: {}", to, subject);

        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }

    @Async
    public void sendTaskApprovalNotification(String to, String taskName,
                                             String approverName) {
        String subject = "Task Approved — " + taskName;
        String html = """
            <div style="font-family: Arial, sans-serif; max-width: 500px; margin: auto;">
                <div style="background: #2D5016; padding: 20px; border-radius: 8px 8px 0 0;">
                    <h2 style="color: #F4A623; margin: 0;">Women Concern Management System</h2>
                </div>
                <div style="padding: 20px; border: 1px solid #e0e0e0; border-top: none;">
                    <p>Your task <strong>%s</strong> has been approved by <strong>%s</strong>.</p>
                    <p>Log in to view the details and next steps.</p>
                    <a href="http://localhost:5173/dashboard"
                       style="background:#7CB342;color:white;padding:10px 20px;
                              text-decoration:none;border-radius:6px;display:inline-block;
                              margin-top:10px;">
                        View Dashboard
                    </a>
                </div>
            </div>
            """.formatted(taskName, approverName);

        sendEmail(to, subject, html);
    }
}