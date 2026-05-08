package com.outreach.soultracker.service;

import com.outreach.soultracker.entity.EmailConfig;
import com.outreach.soultracker.entity.EmailTemplate;
import com.outreach.soultracker.repository.EmailConfigRepository;
import com.outreach.soultracker.repository.EmailTemplateRepository;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.Optional;
import java.util.Properties;

@Service
public class DynamicEmailService {

    private final EmailConfigRepository configRepository;
    private final EmailTemplateRepository templateRepository;

    public DynamicEmailService(EmailConfigRepository configRepository, EmailTemplateRepository templateRepository) {
        this.configRepository = configRepository;
        this.templateRepository = templateRepository;
    }

    private JavaMailSenderImpl getJavaMailSender() {
        Optional<EmailConfig> configOpt = configRepository.findAll().stream().findFirst();
        if (configOpt.isEmpty()) {
            throw new RuntimeException("Email configuration is not set up.");
        }

        EmailConfig config = configOpt.get();
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(config.getHost());
        mailSender.setPort(config.getPort());
        mailSender.setUsername(config.getUsername());
        mailSender.setPassword(config.getPassword());

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", config.getProtocol() != null ? config.getProtocol() : "smtp");
        props.put("mail.smtp.auth", String.valueOf(config.isAuth()));
        props.put("mail.smtp.starttls.enable", String.valueOf(config.isStarttls()));
        props.put("mail.debug", "true");

        return mailSender;
    }

    public void sendEmail(String to, String subject, String text) {
        try {
            JavaMailSenderImpl mailSender = getJavaMailSender();
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text, true); // true indicates HTML format
            helper.setFrom(mailSender.getUsername());

            mailSender.send(message);
        } catch (MessagingException | RuntimeException e) {
            e.printStackTrace();
            System.err.println("Failed to send email to " + to + ": " + e.getMessage());
        }
    }

    public void sendEmailFromTemplate(String to, String templateName, Object... args) {
        Optional<EmailTemplate> templateOpt = templateRepository.findByTemplateName(templateName);
        if (templateOpt.isPresent()) {
            EmailTemplate template = templateOpt.get();
            String subject = template.getSubject();
            String body = template.getBody();
            
            // Very basic replacement logic for arguments like {0}, {1}
            for (int i = 0; i < args.length; i++) {
                body = body.replace("{" + i + "}", String.valueOf(args[i]));
            }
            
            sendEmail(to, subject, body);
        } else {
            System.err.println("Email template not found: " + templateName);
        }
    }

    public void sendSignupNotification(String userEmail, String username) {
        sendEmailFromTemplate(userEmail, "SIGNUP", username);
    }

    public void sendAwardNotification(String userEmail, String username, String rankName) {
        sendEmailFromTemplate(userEmail, "AWARD", username, rankName);
    }
}
