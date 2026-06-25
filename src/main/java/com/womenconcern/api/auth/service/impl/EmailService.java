package com.womenconcern.api.auth.service.impl;

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

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    // ── Generic send ─────────────────────────────────────────────

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

    // ── Welcome email (new account) ───────────────────────────────

    @Async
    public void sendWelcomeEmail(String to, String firstName, String tempPassword) {
        String subject = "Welcome to Women Concern Management System — Your Account is Ready";
        String html = """
            <div style="font-family: Arial, sans-serif; max-width: 560px; margin: auto;">
                <div style="background: #2D5016; padding: 24px; border-radius: 8px 8px 0 0;">
                    <h2 style="color: #F4A623; margin: 0;">Women Concern Management System</h2>
                </div>
                <div style="padding: 24px; border: 1px solid #e0e0e0; border-top: none; border-radius: 0 0 8px 8px;">
                    <p>Hi <strong>%s</strong>,</p>
                    <p>Your account has been created. Use the credentials below to log in:</p>
                    <table style="border-collapse: collapse; width: 100%%; margin: 16px 0;">
                        <tr>
                            <td style="padding: 8px; background: #f5f5f5; font-weight: bold; width: 40%%;">Email</td>
                            <td style="padding: 8px; background: #f5f5f5;">%s</td>
                        </tr>
                        <tr>
                            <td style="padding: 8px; font-weight: bold;">Temporary Password</td>
                            <td style="padding: 8px; font-family: monospace; font-size: 16px; color: #2D5016;">%s</td>
                        </tr>
                    </table>
                    <p style="color: #e53935;"><strong>You will be required to change your password on first login.</strong></p>
                    <a href="%s/login"
                       style="background:#7CB342;color:white;padding:12px 24px;
                              text-decoration:none;border-radius:6px;display:inline-block;
                              margin-top:8px;font-weight:bold;">
                        Log In Now
                    </a>
                    <p style="margin-top: 24px; color: #757575; font-size: 13px;">
                        If you did not expect this email, please contact your administrator.
                    </p>
                </div>
            </div>
            """.formatted(firstName, to, tempPassword, frontendUrl);

        sendEmail(to, subject, html);
    }

    // ── Password reset email ──────────────────────────────────────

    @Async
    public void sendPasswordResetEmail(String to, String firstName, String newPassword) {
        String subject = "Women Concern — Your Password Has Been Reset";
        String html = """
            <div style="font-family: Arial, sans-serif; max-width: 560px; margin: auto;">
                <div style="background: #2D5016; padding: 24px; border-radius: 8px 8px 0 0;">
                    <h2 style="color: #F4A623; margin: 0;">Women Concern Management System</h2>
                </div>
                <div style="padding: 24px; border: 1px solid #e0e0e0; border-top: none; border-radius: 0 0 8px 8px;">
                    <p>Hi <strong>%s</strong>,</p>
                    <p>Your password has been reset. Use the temporary password below to log in:</p>
                    <p style="font-family: monospace; font-size: 20px; letter-spacing: 2px;
                               background: #f5f5f5; padding: 12px 20px; border-radius: 6px;
                               display: inline-block; color: #2D5016;">%s</p>
                    <p style="color: #e53935;"><strong>You will be required to set a new password on login.</strong></p>
                    <a href="%s/login"
                       style="background:#7CB342;color:white;padding:12px 24px;
                              text-decoration:none;border-radius:6px;display:inline-block;
                              margin-top:8px;font-weight:bold;">
                        Log In Now
                    </a>
                    <p style="margin-top: 24px; color: #757575; font-size: 13px;">
                        If you did not request a password reset, contact your administrator immediately.
                    </p>
                </div>
            </div>
            """.formatted(firstName, newPassword, frontendUrl);

        sendEmail(to, subject, html);
    }

    // ── Task approval notification ────────────────────────────────

    @Async
    public void sendTaskApprovalNotification(String to, String taskName, String approverName) {
        String subject = "Task Approved — " + taskName;
        String html = """
            <div style="font-family: Arial, sans-serif; max-width: 500px; margin: auto;">
                <div style="background: #2D5016; padding: 20px; border-radius: 8px 8px 0 0;">
                    <h2 style="color: #F4A623; margin: 0;">Women Concern Management System</h2>
                </div>
                <div style="padding: 20px; border: 1px solid #e0e0e0; border-top: none;">
                    <p>Your task <strong>%s</strong> has been approved by <strong>%s</strong>.</p>
                    <p>Log in to view the details and next steps.</p>
                    <a href="%s/dashboard"
                       style="background:#7CB342;color:white;padding:10px 20px;
                              text-decoration:none;border-radius:6px;display:inline-block;
                              margin-top:10px;">
                        View Dashboard
                    </a>
                </div>
            </div>
            """.formatted(taskName, approverName, frontendUrl);

        sendEmail(to, subject, html);
    }
}
