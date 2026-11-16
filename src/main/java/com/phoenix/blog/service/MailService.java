package com.phoenix.blog.service;

import com.phoenix.blog.exceptions.SpringPostException;
import com.phoenix.blog.model.NotificationEmail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
class MailService {

    private  JavaMailSender mailSender;
    private MailContentBuilder mailContentBuilder;
    private final String sender;

    MailService(JavaMailSender mailSender, MailContentBuilder mailContentBuilder,
                @Value("${spring.mail.username}") String sender) {
        this.mailSender = mailSender;
        this.mailContentBuilder = mailContentBuilder;
        this.sender = sender;
    }

    @Async
    void sendMail(NotificationEmail notificationEmail) {
        MimeMessagePreparator messagePreparator = mimeMessage -> {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
            messageHelper.setFrom(sender);
            messageHelper.setTo(notificationEmail.getRecipient());
            messageHelper.setSubject(notificationEmail.getSubject());
            messageHelper.setText(notificationEmail.getBody());
        };
        try {
            mailSender.send(messagePreparator);
            log.info("Activation email sent");
        } catch (MailException e) {
            log.error("Exception occurred when sending mail", e);
            throw new SpringPostException("Exception occurred when sending mail to " + notificationEmail.getRecipient(), e);
        }
    }

}