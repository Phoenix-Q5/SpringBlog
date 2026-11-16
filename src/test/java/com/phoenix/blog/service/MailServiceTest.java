package com.phoenix.blog.service;

import com.phoenix.blog.model.NotificationEmail;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessagePreparator;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class MailServiceTest {

    @Test
    public void sendsFromConfiguredGmailAddress() throws Exception {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        MailService service = new MailService(mailSender, mock(MailContentBuilder.class), "sender@example.com");

        service.sendMail(new NotificationEmail("subject", "reader@example.com", "body"));

        ArgumentCaptor<MimeMessagePreparator> message = ArgumentCaptor.forClass(MimeMessagePreparator.class);
        verify(mailSender).send(message.capture());
        MimeMessage mimeMessage = new JavaMailSenderImpl().createMimeMessage();
        message.getValue().prepare(mimeMessage);
        assertEquals("sender@example.com", ((InternetAddress) mimeMessage.getFrom()[0]).getAddress());
    }
}
