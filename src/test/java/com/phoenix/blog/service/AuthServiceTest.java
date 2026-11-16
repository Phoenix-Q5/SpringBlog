package com.phoenix.blog.service;

import com.phoenix.blog.dao.UserDAO;
import com.phoenix.blog.dao.VerificationTokenDAO;
import com.phoenix.blog.dto.RegisterRequest;
import com.phoenix.blog.model.NotificationEmail;
import com.phoenix.blog.model.VerificationToken;
import com.phoenix.blog.security.JwtProvider;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class AuthServiceTest {

    @Test
    public void signupUsesConfiguredPublicUrlForVerification() {
        assertVerificationUrl("https://api-blog.corevanta.app", "https://api-blog.corevanta.app");
        assertVerificationUrl("https://example.com/blog/", "https://example.com/blog");
    }

    private void assertVerificationUrl(String baseUrl, String expectedBaseUrl) {
        UserDAO userDAO = mock(UserDAO.class);
        VerificationTokenDAO tokenDAO = mock(VerificationTokenDAO.class);
        MailService mailService = mock(MailService.class);
        AuthService service = new AuthService(mock(PasswordEncoder.class), userDAO, tokenDAO, mailService,
                mock(AuthenticationManager.class), mock(JwtProvider.class), mock(RefreshTokenService.class), baseUrl);

        service.signup(new RegisterRequest("reader@example.com", "reader", "password"));

        ArgumentCaptor<VerificationToken> token = ArgumentCaptor.forClass(VerificationToken.class);
        ArgumentCaptor<NotificationEmail> email = ArgumentCaptor.forClass(NotificationEmail.class);
        verify(tokenDAO).save(token.capture());
        verify(mailService).sendMail(email.capture());
        assertTrue(email.getValue().getBody().endsWith(expectedBaseUrl
                + "/api/auth/accountVerification/" + token.getValue().getToken()));
    }
}
