package com.disp.celesma.service;

import com.disp.celesma.model.User;
import com.disp.celesma.repository.UserRepository;
import com.disp.celesma.service.interfaces.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + id));
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found by email: " + email));
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean existsById(Long id) {
        return userRepository.existsById(id);
    }

    @Override
    @Transactional
    public User create(String email, String rawPassword, String firstName, String lastName, String nickname) {
        if (userRepository.existsByUsername(nickname))
            throw new IllegalStateException("Никнейм уже занят: " + nickname);
        if (userRepository.existsByEmail(email))
            throw new IllegalStateException("Email уже занят: " + email);

        return userRepository.save(User.builder()
                .username(nickname)
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .role("ROLE_USER")
                .firstName(firstName)
                .lastName(lastName)
                .registrationDate(LocalDate.now())
                .build());
    }

    @Override
    @Transactional
    public User createOAuthUser(String email, String firstName, String lastName, String avatarUrl) {
        String username = email.split("@")[0];
        return userRepository.save(User.builder()
                .username(username)
                .email(email)
                .password(null)
                .role("ROLE_USER")
                .firstName(firstName)
                .lastName(lastName)
                .avatarUrl(avatarUrl)
                .registrationDate(LocalDate.now())
                .build());
    }

    @Override
    public void save(User user) {
        userRepository.save(user);
    }
}
