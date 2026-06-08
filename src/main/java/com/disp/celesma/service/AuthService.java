package com.disp.celesma.service;

import com.disp.celesma.dto.auth.AuthResponse;
import com.disp.celesma.dto.auth.LoginRequest;
import com.disp.celesma.dto.auth.RegisterRequest;
import com.disp.celesma.security.JwtService;
import com.disp.celesma.security.UserPrincipal;
import com.disp.celesma.service.interfaces.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class AuthService {

    private final IUserService userService;
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

        var user = userService.createUserAndSave(request);

        var token = jwtService.generateToken(new UserPrincipal(user));
        return new AuthResponse(token, user.getUsername(), user.getEmail(), user.getRole());
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));

        var user = userService.getUserEntityByUsername(request.username());

        var token = jwtService.generateToken(new UserPrincipal(user));
        return new AuthResponse(token, user.getUsername(), user.getEmail(), user.getRole());
    }
}
