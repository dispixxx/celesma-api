package com.disp.celesma.service;

import com.disp.celesma.dto.auth.LoginRequest;
import com.disp.celesma.dto.auth.RegisterRequest;
import com.disp.celesma.model.User;
import com.disp.celesma.security.JwtService;
import com.disp.celesma.service.interfaces.IUserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock IUserService userService;
    @Mock JwtService jwtService;
    @Mock AuthenticationManager authenticationManager;

    @InjectMocks AuthService authService;

    // RegisterRequest(email, password, firstName, lastName, nickname)
    @Test
    void register_emailTaken_throws() {
        var request = new RegisterRequest("taken@mail.com", "pass", "John", "Doe", "user");
        when(userService.existsByEmail("taken@mail.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Email");
    }

    @Test
    void register_usernameTaken_throws() {
        var request = new RegisterRequest("new@mail.com", "pass", "John", "Doe", "takenUser");
        when(userService.existsByEmail("new@mail.com")).thenReturn(false);
        when(userService.existsByUsername("takenUser")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Никнейм");
    }

    @Test
    void register_success_returnsToken() {
        var request = new RegisterRequest("new@mail.com", "pass", "John", "Doe", "newUser");
        var user = User.builder().id(1L).username("newUser").email("new@mail.com").build();

        when(userService.existsByEmail("new@mail.com")).thenReturn(false);
        when(userService.existsByUsername("newUser")).thenReturn(false);
        when(userService.createUserAndSave(request)).thenReturn(user);
        when(jwtService.generateToken(any())).thenReturn("jwt-token");

        var result = authService.register(request);

        assertThat(result.token()).isEqualTo("jwt-token");
        assertThat(result.username()).isEqualTo("newUser");
    }

    @Test
    void login_success_returnsToken() {
        var request = new LoginRequest("user", "pass");
        var user = User.builder().id(1L).username("user").email("u@mail.com").build();

        when(userService.getUserEntityByUsername("user")).thenReturn(user);
        when(jwtService.generateToken(any())).thenReturn("jwt-token");

        var result = authService.login(request);

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        assertThat(result.token()).isEqualTo("jwt-token");
        assertThat(result.username()).isEqualTo("user");
    }
}
