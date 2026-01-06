package com.community.volunteerhub.service.emailService;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import freemarker.template.Template;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private FreeMarkerConfigurer freemarkerConfigurer;

    public void sendEmail(
            String to,
            String subject,
            String templateName,
            Map<String,Object> model,
            String replyToEmail,
            String fromName
    ) throws Exception {

        Template template =
                freemarkerConfigurer.getConfiguration().getTemplate(templateName);

        String html =
                FreeMarkerTemplateUtils.processTemplateIntoString(template, model);

        MimeMessage message = mailSender.createMimeMessage();

        MimeMessageHelper helper =
                new MimeMessageHelper(message, true);

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(html, true);

        // system mailbox (configure this email in application.properties)
        helper.setFrom("volunteerhub.system@gmail.com", fromName);

        // replies go to organizer
        helper.setReplyTo(replyToEmail);

        mailSender.send(message);
    }
    
    public void sendEventCancellationEmail(
            String to,
            String volunteerName,
            String eventName,
            String eventDate,
            String organizerEmail
    ) {
        try {
            Map<String, Object> model = Map.of(
                    "volunteerName", volunteerName,
                    "eventName", eventName,
                    "eventDate", eventDate
            );

            sendEmail(
                    to,
                    "Event Cancelled: " + eventName,
                    "cancelled.ftl",       // your template name
                    model,
                    organizerEmail,        // reply-to organizer
                    "VolunteerHub System"  // shown in inbox
            );

        } catch (Exception e) {
            System.out.println("Failed to send cancellation email to "
                    + to + ": " + e.getMessage());
        }
    }
}
