package org.example.service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class EmailService {
    private final String FROM_EMAIL = "duonganhdn2000@gmail.com";
    private final String PASSWORD = "zqwu gxwi bomc mfif";

    public boolean sendEmail(String toEmail, String subject, String htmlBody) {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.port", "587");

//        props.put("mail.debug", "true"); // debug trong trường hợp gửi mail lỗi

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM_EMAIL, PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);

            message.setContent(htmlBody, "text/html; charset=UTF-8");

            Transport.send(message);
            System.out.println(">>> [SUCCESS] Email đã được gửi thành công!");
            return true;

        } catch (MessagingException e) {
            System.out.println(">>> Lỗi gửi mail: " + e.getMessage());
            e.printStackTrace();

            return false;
        }
    }
    public String getOtpEmailTemplate(String name, String otp) {
        try {
            InputStream inputStream = getClass().getResourceAsStream("/email_template.html");

            if (inputStream == null) {
                return "Lỗi: Không tìm thấy file template!";
            }

            String template = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

            template = template.replace("{{name}}", name);
            template = template.replace("{{otp}}", otp);

            return template;

        } catch (IOException e) {
            e.printStackTrace();
            return "Lỗi đọc template email!";
        }
    }
}
