package com.disp.celesma.service;

import com.disp.celesma.dto.auth.AuthResponse;
import com.disp.celesma.dto.auth.LoginRequest;
import com.disp.celesma.dto.auth.RegisterRequest;
import com.disp.celesma.model.User;
import com.disp.celesma.security.JwtService;
import com.disp.celesma.security.UserPrincipal;
import com.disp.celesma.service.interfaces.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final IUserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userService.existsByEmail(request.email())) {
            throw new IllegalStateException("Email уже занят");
        }
        if (userService.existsByUsername(request.nickname())) {
            throw new IllegalStateException("Никнейм уже занят");
        }

        var user = User.builder()
                .username(request.nickname())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .firstName(request.firstName())
                .lastName(request.lastName())
                .role("ROLE_USER")
                .registrationDate(LocalDate.now())
                .build();

        userService.save(user);

        var token = jwtService.generateToken(new UserPrincipal(user));
        return new AuthResponse(token, user.getUsername(), user.getEmail(), user.getRole());
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));

        var user = userService.getUserByUsername(request.username());

        var token = jwtService.generateToken(new UserPrincipal(user));
        return new AuthResponse(token, user.getUsername(), user.getEmail(), user.getRole());
    }
}
