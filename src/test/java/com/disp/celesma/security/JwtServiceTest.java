package com.disp.celesma.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", "test-secret-key-that-is-long-enough-32chars!");
        ReflectionTestUtils.setField(jwtService, "expirationMs", 3600000L);
    }

    private UserDetails userDetails(String username) {
        return User.withUsername(username).password("pass").authorities(List.of()).build();
    }

    @Test
    void generateToken_extractUsername_match() {
        var ud = userDetails("alice");
        var token = jwtService.generateToken(ud);

        assertThat(jwtService.extractUsername(token)).isEqualTo("alice");
    }

    @Test
    void isTokenValid_validToken_returnsTrue() {
        var ud = userDetails("bob");
        var token = jwtService.generateToken(ud);

        assertThat(jwtService.isTokenValid(token, ud)).isTrue();
    }

    @Test
    void isTokenValid_wrongUser_returnsFalse() {
        var token = jwtService.generateToken(userDetails("alice"));

        assertThat(jwtService.isTokenValid(token, userDetails("bob"))).isFalse();
    }

    @Test
    void isTokenValid_expiredToken_returnsFalse() {
        ReflectionTestUtils.setField(jwtService, "expirationMs", -1000L);
        var ud = userDetails("alice");
        var token = jwtService.generateToken(ud);

        assertThat(jwtService.isTokenValid(token, ud)).isFalse();
    }
}
