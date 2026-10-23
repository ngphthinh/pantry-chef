package org.group12.feature.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import jakarta.mail.internet.MimeMessage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendResetPasswordEmail(String to, String newPassword) {
        try {
            Resource resource = new ClassPathResource(
                    "templates/reset-password.html"
            );

            String html = StreamUtils.copyToString(
                    resource.getInputStream(),
                    StandardCharsets.UTF_8
            );

            html = html.replace(
                    "{{newPassword}}",
                    escapeHtml(newPassword)
            );

            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    true,
                    StandardCharsets.UTF_8.name()
            );

            helper.setTo(to);
            helper.setSubject("PantryChef - Your Password Has Been Reset");
            helper.setText(html, true);

            mailSender.send(message);

        } catch (IOException | jakarta.mail.MessagingException e) {
            throw new IllegalStateException(
                    "Failed to send password reset email", e
            );
        }
    }

    private String escapeHtml(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}