package lk.sunrise.dental.util;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;

/**
 * ================================================================
 * EmailService.java
 * Email Notification Service using Mailtrap SMTP
 *
 * Design Pattern : Singleton Pattern
 * Purpose        : Centralized email sending for all notifications
 * Package        : lk.sunrise.dental.util
 * ================================================================
 */
public class EmailService {

    // ── Mailtrap SMTP Configuration ────────────────────────────
    // Loaded from app.properties (see AppConfig) instead of being
    // hardcoded, so the SMTP credentials aren't sitting in source.
    private static final String SMTP_HOST;
    private static final String SMTP_PORT;
    private static final String SMTP_USERNAME;
    private static final String SMTP_PASSWORD;
    private static final String FROM_EMAIL;
    private static final String FROM_NAME;

    static {
        AppConfig config = AppConfig.getInstance();
        SMTP_HOST     = config.get("smtp.host");
        SMTP_PORT     = config.get("smtp.port");
        SMTP_USERNAME = config.get("smtp.username");
        SMTP_PASSWORD = config.get("smtp.password");
        FROM_EMAIL    = config.get("smtp.from.email");
        FROM_NAME     = config.get("smtp.from.name");
    }

    // ── Singleton Instance ──────────────────────────────────────
    private static EmailService instance;
    private final Session mailSession;

    // ── Private Constructor ─────────────────────────────────────
    private EmailService() {
        Properties props = new Properties();
        props.put("mail.smtp.auth",            "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host",            SMTP_HOST);
        props.put("mail.smtp.port",            SMTP_PORT);
        props.put("mail.smtp.ssl.trust",       SMTP_HOST);

        mailSession = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SMTP_USERNAME, SMTP_PASSWORD);
            }
        });

        System.out.println("[EmailService] ✅ Mail session initialized");
    }

    /**
     * Get singleton instance
     */
    public static synchronized EmailService getInstance() {
        if (instance == null) {
            instance = new EmailService();
        }
        return instance;
    }

    /**
     * Send plain text email
     */
    public boolean sendEmail(String toEmail, String toName,
                              String subject, String body) {
        try {
            Message message = new MimeMessage(mailSession);
            message.setFrom(new InternetAddress(FROM_EMAIL, FROM_NAME));
            message.setRecipients(
                Message.RecipientType.TO,
                InternetAddress.parse(toEmail)
            );
            message.setSubject(subject);
            message.setText(body);

            Transport.send(message);

            System.out.println("[EmailService] ✅ Email sent to: " + toEmail);
            return true;

        } catch (Exception e) {
            System.err.println("[EmailService] ❌ Failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Send HTML email
     */
    public boolean sendHtmlEmail(String toEmail, String toName,
                                  String subject, String htmlBody) {
        try {
            Message message = new MimeMessage(mailSession);
            message.setFrom(new InternetAddress(FROM_EMAIL, FROM_NAME));
            message.setRecipients(
                Message.RecipientType.TO,
                InternetAddress.parse(toEmail)
            );
            message.setSubject(subject);
            message.setContent(htmlBody, "text/html; charset=utf-8");

            Transport.send(message);

            System.out.println("[EmailService] ✅ HTML Email sent to: " + toEmail);
            return true;

        } catch (Exception e) {
            System.err.println("[EmailService] ❌ HTML Email failed: " + e.getMessage());
            return false;
        }
    }

    // ── Pre-built Email Templates ───────────────────────────────

    /**
     * Send appointment confirmation email
     */
    public boolean sendAppointmentConfirmation(
            String toEmail,
            String patientName,
            String aptCode,
            String dentistName,
            String treatmentName,
            String aptDate,
            String aptTime) {

        String subject = "✅ Appointment Confirmed - " + aptCode;

        String html = """
            <html>
            <body style="font-family: Arial, sans-serif; max-width: 600px;
                         margin: 0 auto; background: #f5f5f5; padding: 20px;">

                <div style="background: #0ea5e9; padding: 30px;
                            border-radius: 10px 10px 0 0; text-align: center;">
                    <h1 style="color: white; margin: 0;">🦷 Sunrise Dental Clinic</h1>
                    <p style="color: rgba(255,255,255,0.8); margin: 5px 0 0;">
                        Colombo, Sri Lanka
                    </p>
                </div>

                <div style="background: white; padding: 30px;
                            border-radius: 0 0 10px 10px;">

                    <h2 style="color: #1a1a2e;">
                        ✅ Appointment Confirmed!
                    </h2>

                    <p style="color: #555;">Dear <strong>%s</strong>,</p>

                    <p style="color: #555;">
                        Your appointment has been successfully booked.
                        Here are your appointment details:
                    </p>

                    <div style="background: #f0f9ff; border-left: 4px solid #0ea5e9;
                                padding: 20px; border-radius: 5px; margin: 20px 0;">
                        <table style="width: 100%%; border-collapse: collapse;">
                            <tr>
                                <td style="padding: 8px 0; color: #888;
                                           font-size: 13px; width: 140px;">
                                    📋 Appointment Code
                                </td>
                                <td style="padding: 8px 0; font-weight: bold;
                                           color: #0ea5e9;">%s</td>
                            </tr>
                            <tr>
                                <td style="padding: 8px 0; color: #888; font-size: 13px;">
                                    🩺 Dentist
                                </td>
                                <td style="padding: 8px 0; font-weight: bold;">%s</td>
                            </tr>
                            <tr>
                                <td style="padding: 8px 0; color: #888; font-size: 13px;">
                                    💊 Treatment
                                </td>
                                <td style="padding: 8px 0; font-weight: bold;">%s</td>
                            </tr>
                            <tr>
                                <td style="padding: 8px 0; color: #888; font-size: 13px;">
                                    📅 Date
                                </td>
                                <td style="padding: 8px 0; font-weight: bold;">%s</td>
                            </tr>
                            <tr>
                                <td style="padding: 8px 0; color: #888; font-size: 13px;">
                                    🕐 Time
                                </td>
                                <td style="padding: 8px 0; font-weight: bold;">%s</td>
                            </tr>
                        </table>
                    </div>

                    <div style="background: #fff3cd; border: 1px solid #ffc107;
                                padding: 15px; border-radius: 5px; margin: 20px 0;">
                        <strong>⚠️ Important Reminders:</strong>
                        <ul style="margin: 10px 0; padding-left: 20px; color: #555;">
                            <li>Please arrive 10 minutes early</li>
                            <li>Bring any previous dental records</li>
                            <li>Contact us 24 hours before to reschedule</li>
                        </ul>
                    </div>

                    <p style="color: #555;">
                        For inquiries, call us at
                        <strong>011-234-5678</strong>
                    </p>

                    <p style="color: #555;">
                        Thank you for choosing Sunrise Dental Clinic! 😊
                    </p>

                </div>

                <div style="text-align: center; padding: 20px;
                            color: #888; font-size: 12px;">
                    <p>© 2025 Sunrise Dental Clinic, Colombo, Sri Lanka</p>
                    <p>This is an automated message. Please do not reply.</p>
                </div>

            </body>
            </html>
            """.formatted(
                patientName, aptCode, dentistName,
                treatmentName, aptDate, aptTime
            );

        return sendHtmlEmail(toEmail, patientName, subject, html);
    }

    /**
     * Send bill/invoice email
     */
    public boolean sendBillNotification(
            String toEmail,
            String patientName,
            String billCode,
            String treatmentName,
            String totalAmount,
            String paymentMethod) {

        String subject = "🧾 Invoice " + billCode + " - Sunrise Dental Clinic";

        String html = """
            <html>
            <body style="font-family: Arial, sans-serif; max-width: 600px;
                         margin: 0 auto; background: #f5f5f5; padding: 20px;">

                <div style="background: #0ea5e9; padding: 30px;
                            border-radius: 10px 10px 0 0; text-align: center;">
                    <h1 style="color: white; margin: 0;">🦷 Sunrise Dental Clinic</h1>
                </div>

                <div style="background: white; padding: 30px;
                            border-radius: 0 0 10px 10px;">

                    <h2 style="color: #1a1a2e;">🧾 Payment Invoice</h2>

                    <p style="color: #555;">Dear <strong>%s</strong>,</p>
                    <p style="color: #555;">
                        Please find your invoice details below:
                    </p>

                    <div style="background: #f0f9ff; border-left: 4px solid #0ea5e9;
                                padding: 20px; border-radius: 5px; margin: 20px 0;">
                        <table style="width: 100%%; border-collapse: collapse;">
                            <tr>
                                <td style="padding: 8px 0; color: #888; font-size: 13px;">
                                    🧾 Invoice Number
                                </td>
                                <td style="padding: 8px 0; font-weight: bold;
                                           color: #0ea5e9;">%s</td>
                            </tr>
                            <tr>
                                <td style="padding: 8px 0; color: #888; font-size: 13px;">
                                    💊 Treatment
                                </td>
                                <td style="padding: 8px 0; font-weight: bold;">%s</td>
                            </tr>
                            <tr>
                                <td style="padding: 8px 0; color: #888; font-size: 13px;">
                                    💰 Total Amount
                                </td>
                                <td style="padding: 8px 0; font-weight: bold;
                                           color: #22c55e; font-size: 18px;">%s</td>
                            </tr>
                            <tr>
                                <td style="padding: 8px 0; color: #888; font-size: 13px;">
                                    💳 Payment Method
                                </td>
                                <td style="padding: 8px 0; font-weight: bold;">%s</td>
                            </tr>
                        </table>
                    </div>

                    <p style="color: #555;">
                        Thank you for your payment!
                        We look forward to serving you again.
                    </p>

                </div>

                <div style="text-align: center; padding: 20px;
                            color: #888; font-size: 12px;">
                    <p>© 2025 Sunrise Dental Clinic, Colombo, Sri Lanka</p>
                </div>

            </body>
            </html>
            """.formatted(
                patientName, billCode,
                treatmentName, totalAmount, paymentMethod
            );

        return sendHtmlEmail(toEmail, patientName, subject, html);
    }

    /**
     * Send a reminder email for an appointment scheduled for tomorrow
     */
    public boolean sendAppointmentReminder(
            String toEmail,
            String patientName,
            String aptCode,
            String dentistName,
            String treatmentName,
            String aptDate,
            String aptTime) {

        String subject = "⏰ Reminder: Appointment Tomorrow - " + aptCode;

        String html = """
            <html>
            <body style="font-family: Arial, sans-serif; max-width: 600px;
                         margin: 0 auto; background: #f5f5f5; padding: 20px;">

                <div style="background: #f59e0b; padding: 30px;
                            border-radius: 10px 10px 0 0; text-align: center;">
                    <h1 style="color: white; margin: 0;">🦷 Sunrise Dental Clinic</h1>
                </div>

                <div style="background: white; padding: 30px;
                            border-radius: 0 0 10px 10px;">

                    <h2 style="color: #1a1a2e;">⏰ Appointment Reminder</h2>

                    <p style="color: #555;">Dear <strong>%s</strong>,</p>
                    <p style="color: #555;">
                        This is a friendly reminder that you have an appointment
                        scheduled for <strong>tomorrow</strong>:
                    </p>

                    <div style="background: #fffbeb; border-left: 4px solid #f59e0b;
                                padding: 20px; border-radius: 5px; margin: 20px 0;">
                        <table style="width: 100%%; border-collapse: collapse;">
                            <tr>
                                <td style="padding: 8px 0; color: #888; font-size: 13px; width: 140px;">
                                    📋 Appointment Code
                                </td>
                                <td style="padding: 8px 0; font-weight: bold; color: #f59e0b;">%s</td>
                            </tr>
                            <tr>
                                <td style="padding: 8px 0; color: #888; font-size: 13px;">🩺 Dentist</td>
                                <td style="padding: 8px 0; font-weight: bold;">%s</td>
                            </tr>
                            <tr>
                                <td style="padding: 8px 0; color: #888; font-size: 13px;">💊 Treatment</td>
                                <td style="padding: 8px 0; font-weight: bold;">%s</td>
                            </tr>
                            <tr>
                                <td style="padding: 8px 0; color: #888; font-size: 13px;">📅 Date</td>
                                <td style="padding: 8px 0; font-weight: bold;">%s</td>
                            </tr>
                            <tr>
                                <td style="padding: 8px 0; color: #888; font-size: 13px;">🕐 Time</td>
                                <td style="padding: 8px 0; font-weight: bold;">%s</td>
                            </tr>
                        </table>
                    </div>

                    <p style="color: #555;">
                        Please arrive 10 minutes early. If you need to reschedule,
                        contact us at <strong>011-234-5678</strong>.
                    </p>

                </div>

                <div style="text-align: center; padding: 20px; color: #888; font-size: 12px;">
                    <p>© 2025 Sunrise Dental Clinic, Colombo, Sri Lanka</p>
                    <p>This is an automated reminder. Please do not reply.</p>
                </div>

            </body>
            </html>
            """.formatted(
                patientName, aptCode, dentistName,
                treatmentName, aptDate, aptTime
            );

        return sendHtmlEmail(toEmail, patientName, subject, html);
    }

    /**
     * Send appointment cancellation email
     */
    public boolean sendCancellationNotification(
            String toEmail,
            String patientName,
            String aptCode,
            String aptDate,
            String aptTime) {

        String subject = "❌ Appointment Cancelled - " + aptCode;

        String html = """
            <html>
            <body style="font-family: Arial, sans-serif; max-width: 600px;
                         margin: 0 auto; background: #f5f5f5; padding: 20px;">

                <div style="background: #ef4444; padding: 30px;
                            border-radius: 10px 10px 0 0; text-align: center;">
                    <h1 style="color: white; margin: 0;">🦷 Sunrise Dental Clinic</h1>
                </div>

                <div style="background: white; padding: 30px;
                            border-radius: 0 0 10px 10px;">

                    <h2 style="color: #ef4444;">❌ Appointment Cancelled</h2>

                    <p style="color: #555;">Dear <strong>%s</strong>,</p>
                    <p style="color: #555;">
                        Your appointment <strong>%s</strong> scheduled for
                        <strong>%s at %s</strong> has been cancelled.
                    </p>

                    <p style="color: #555;">
                        To book a new appointment, please call
                        <strong>011-234-5678</strong> or visit our clinic.
                    </p>

                    <p style="color: #555;">
                        We apologize for any inconvenience caused.
                    </p>

                </div>

                <div style="text-align: center; padding: 20px;
                            color: #888; font-size: 12px;">
                    <p>© 2025 Sunrise Dental Clinic, Colombo, Sri Lanka</p>
                </div>

            </body>
            </html>
            """.formatted(patientName, aptCode, aptDate, aptTime);

        return sendHtmlEmail(toEmail, patientName, subject, html);
    }

    /**
     * Send welcome email to newly registered patient
     */
    public boolean sendWelcomeEmail(
            String toEmail,
            String patientName,
            String patientCode) {

        String subject = "🦷 Welcome to Sunrise Dental Clinic!";

        String html = """
            <html>
            <body style="font-family: Arial, sans-serif; max-width: 600px;
                        margin: 0 auto; background: #f5f5f5; padding: 20px;">

                <div style="background: #0ea5e9; padding: 30px;
                            border-radius: 10px 10px 0 0; text-align: center;">
                    <h1 style="color: white; margin: 0;">🦷 Sunrise Dental Clinic</h1>
                    <p style="color: rgba(255,255,255,0.8); margin: 5px 0 0;">
                        Colombo, Sri Lanka
                    </p>
                </div>

                <div style="background: white; padding: 30px;
                            border-radius: 0 0 10px 10px;">

                    <h2 style="color: #1a1a2e;">
                        👋 Welcome to Our Clinic Family!
                    </h2>

                    <p style="color: #555;">Dear <strong>%s</strong>,</p>

                    <p style="color: #555;">
                        Thank you for registering with
                        <strong>Sunrise Dental Clinic</strong>.
                        We are delighted to have you as our patient!
                    </p>

                    <div style="background: #f0f9ff;
                                border-left: 4px solid #0ea5e9;
                                padding: 20px; border-radius: 5px;
                                margin: 20px 0;">
                        <p style="margin: 0; color: #555;">
                            <strong>Your Patient ID:</strong>
                        </p>
                        <p style="font-size: 24px; font-weight: bold;
                                color: #0ea5e9; margin: 8px 0 0;">
                            %s
                        </p>
                        <p style="margin: 8px 0 0; color: #888; font-size: 13px;">
                            Please save this ID for future appointments
                        </p>
                    </div>

                    <div style="background: #f9fafb; padding: 20px;
                                border-radius: 5px; margin: 20px 0;">
                        <h3 style="color: #1a1a2e; margin-top: 0;">
                            📞 Contact Us
                        </h3>
                        <p style="color: #555; margin: 5px 0;">
                            📍 Colombo, Sri Lanka
                        </p>
                        <p style="color: #555; margin: 5px 0;">
                            📞 011-234-5678
                        </p>
                        <p style="color: #555; margin: 5px 0;">
                            🕐 Mon-Sat: 8:00 AM - 6:00 PM
                        </p>
                    </div>

                    <p style="color: #555;">
                        We look forward to providing you with
                        excellent dental care!
                    </p>

                </div>

                <div style="text-align: center; padding: 20px;
                            color: #888; font-size: 12px;">
                    <p>© 2025 Sunrise Dental Clinic, Colombo, Sri Lanka</p>
                </div>

            </body>
            </html>
            """.formatted(patientName, patientCode);

        return sendHtmlEmail(toEmail, patientName, subject, html);
    }
}