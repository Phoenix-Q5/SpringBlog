package com.phoenix.blog.service;

import com.phoenix.blog.model.Users;
import com.phoenix.blog.dto.AuthenticationResponse;
import com.phoenix.blog.dto.LoginRequest;
import com.phoenix.blog.dto.RefreshTokenRequest;
import com.phoenix.blog.dto.RegisterRequest;
import com.phoenix.blog.exceptions.SpringPostException;
import com.phoenix.blog.model.NotificationEmail;
import com.phoenix.blog.model.VerificationToken;
import com.phoenix.blog.dao.UserDAO;
import com.phoenix.blog.dao.VerificationTokenDAO;
import com.phoenix.blog.security.JwtProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class AuthService {

    private PasswordEncoder passwordEncoder;
    private UserDAO userDAO;
    private VerificationTokenDAO verificationTokenDAO;
    private MailService mailService;
    private AuthenticationManager authenticationManager;
    private JwtProvider jwtProvider;
    private RefreshTokenService refreshTokenService;
    private final String publicBaseUrl;

    public AuthService(PasswordEncoder passwordEncoder, UserDAO userDAO,
                       VerificationTokenDAO verificationTokenDAO, MailService mailService,
                       AuthenticationManager authenticationManager, JwtProvider jwtProvider,
                       RefreshTokenService refreshTokenService,
                       @Value("${app.public-base-url}") String publicBaseUrl) {
        this.passwordEncoder = passwordEncoder;
        this.userDAO = userDAO;
        this.verificationTokenDAO = verificationTokenDAO;
        this.mailService = mailService;
        this.authenticationManager = authenticationManager;
        this.jwtProvider = jwtProvider;
        this.refreshTokenService = refreshTokenService;
        this.publicBaseUrl = publicBaseUrl;
    }

    public void signup(RegisterRequest registerRequest) {
        Users users = new Users();
        users.setUsername(registerRequest.getUsername());
        users.setEmail(registerRequest.getEmail());
        users.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        users.setCreated(Instant.now());
        users.setEnabled(false);

        userDAO.save(users);

        String token = generateVerificationToken(users);
        String verificationUrl = UriComponentsBuilder.fromHttpUrl(publicBaseUrl)
                .pathSegment("api", "auth", "accountVerification", token)
                .build().toUriString();
        mailService.sendMail(new NotificationEmail("Activate your account ",
                users.getEmail(), "Thank you for signing up to Phoenix Blog, "+
                "please click on the below url to activate your account : " +
                verificationUrl));
    }

    @Transactional(readOnly = true)
    public Users getCurrentUser() {
        org.springframework.security.core.userdetails.User principal = (org.springframework.security.core.userdetails.User) SecurityContextHolder.
                getContext().getAuthentication().getPrincipal();
        return userDAO.findByUsername(principal.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User name not found: " + principal.getUsername()));
    }

    private void fetchUserAndEnable(VerificationToken verificationToken) {
        String username = verificationToken.getUsers().getUsername();
        Users users = userDAO.findByUsername(username).orElseThrow(() -> new SpringPostException("User not found with name: " + username));
        users.setEnabled(true);
        userDAO.save(users);
    }

    private String generateVerificationToken(Users users) {
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUsers(users);

        verificationTokenDAO.save(verificationToken);
        return token;
    }

    public void verifyAccount(String token) {
        Optional<VerificationToken> verificationToken = verificationTokenDAO.findByToken(token);
        fetchUserAndEnable(verificationToken.orElseThrow(() -> new SpringPostException("Invalid Token")));
    }

    public AuthenticationResponse login(LoginRequest loginRequest) {
        Authentication authenticate = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(),
                loginRequest.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authenticate);
        String token = jwtProvider.generateToken(authenticate);
        return AuthenticationResponse.builder()
                .authenticationToken(token)
                .refreshToken(refreshTokenService.generateRefreshToken().getToken())
                .expiresAt(Instant.now().plusMillis(jwtProvider.getJwtExpirationInMillis()))
                .username(loginRequest.getUsername())
                .build();
    }

    public AuthenticationResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
        refreshTokenService.validateRefreshToken(refreshTokenRequest.getRefreshToken());
        String token = jwtProvider.generateTokenWithUserName(refreshTokenRequest.getUsername());
        return AuthenticationResponse.builder()
                .authenticationToken(token)
                .refreshToken(refreshTokenRequest.getRefreshToken())
                .expiresAt(Instant.now().plusMillis(jwtProvider.getJwtExpirationInMillis()))
                .username(refreshTokenRequest.getUsername())
                .build();
    }

    public boolean isLoggedIn() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return !(authentication instanceof AnonymousAuthenticationToken) && authentication.isAuthenticated();
    }
}