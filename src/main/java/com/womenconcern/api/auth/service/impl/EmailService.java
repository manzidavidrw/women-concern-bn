package com.womenconcern.api.auth.service.impl;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Slf4j
@Service
public class EmailService {

    private final WebClient webClient;

    @Value("${app.mail.from}")
    private String fromEmail;

    @Value("${app.mail.from-name}")
    private String fromName;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @Value("${brevo.api.key}")
    private String apiKey;

    public EmailService(WebClient webClient) {
        this.webClient = webClient;
    }

    @PostConstruct
    public void debugConfig() {
        log.info("Brevo API key starts with: {}",
                apiKey != null ? apiKey.substring(0, 15) : "NULL - NOT LOADED");
        log.info("Mail from: {}", fromEmail);
    }

    // ── Generic send ──────────────────────────────────────────────

    @Async
    public void sendEmail(String to, String subject, String htmlContent) {
        try {
            Map<String, Object> body = Map.of(
                    "sender", Map.of("email", fromEmail, "name", fromName),
                    "to", new Object[]{Map.of("email", to)},
                    "subject", subject,
                    "htmlContent", htmlContent
            );

            String response = webClient.post()
                    .uri("/smtp/email")
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header("api-key", apiKey)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("Email sent to {}: {} | Response: {}", to, subject, response);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }

    // ── Welcome email ─────────────────────────────────────────────

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

    // ── Activity assignment ───────────────────────────────────────

    @Async
    public void sendActivityAssignmentEmail(String to, String firstName, String activityTitle) {
        String subject = "New Activity Assigned — " + activityTitle;
        String html = """
            <div style="font-family: Arial, sans-serif; max-width: 560px; margin: auto;">
                <div style="background: #2D5016; padding: 24px; border-radius: 8px 8px 0 0;">
                    <h2 style="color: #F4A623; margin: 0;">Women Concern Management System</h2>
                </div>
                <div style="padding: 24px; border: 1px solid #e0e0e0; border-top: none; border-radius: 0 0 8px 8px;">
                    <p>Hi <strong>%s</strong>,</p>
                    <p>You have been assigned a new activity:</p>
                    <div style="background: #f5f5f5; padding: 16px; border-left: 4px solid #7CB342;
                                border-radius: 4px; margin: 16px 0;">
                        <strong style="font-size: 16px; color: #2D5016;">%s</strong>
                    </div>
                    <p>Please log in to view full details, related tasks, and deadlines.</p>
                    <a href="%s/dashboard"
                       style="background:#7CB342;color:white;padding:12px 24px;
                              text-decoration:none;border-radius:6px;display:inline-block;
                              margin-top:8px;font-weight:bold;">
                        View My Activities
                    </a>
                </div>
            </div>
            """.formatted(firstName, activityTitle, frontendUrl);
        sendEmail(to, subject, html);
    }

    // ── Activity unassignment ─────────────────────────────────────

    @Async
    public void sendActivityUnassignmentEmail(String to, String firstName, String activityTitle) {
        String subject = "Activity Unassigned — " + activityTitle;
        String html = """
            <div style="font-family: Arial, sans-serif; max-width: 560px; margin: auto;">
                <div style="background: #2D5016; padding: 24px; border-radius: 8px 8px 0 0;">
                    <h2 style="color: #F4A623; margin: 0;">Women Concern Management System</h2>
                </div>
                <div style="padding: 24px; border: 1px solid #e0e0e0; border-top: none; border-radius: 0 0 8px 8px;">
                    <p>Hi <strong>%s</strong>,</p>
                    <p>You have been <strong style="color: #e53935;">unassigned</strong> from the following activity:</p>
                    <div style="background: #f5f5f5; padding: 16px; border-left: 4px solid #e53935;
                                border-radius: 4px; margin: 16px 0;">
                        <strong style="font-size: 16px; color: #2D5016;">%s</strong>
                    </div>
                    <p>If you believe this was done in error, please contact your Project Manager.</p>
                    <a href="%s/dashboard"
                       style="background:#7CB342;color:white;padding:12px 24px;
                              text-decoration:none;border-radius:6px;display:inline-block;
                              margin-top:8px;font-weight:bold;">
                        View Dashboard
                    </a>
                </div>
            </div>
            """.formatted(firstName, activityTitle, frontendUrl);
        sendEmail(to, subject, html);
    }
}