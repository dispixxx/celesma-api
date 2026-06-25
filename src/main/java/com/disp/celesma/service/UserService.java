package com.disp.celesma.service;

import com.disp.celesma.dto.auth.RegisterRequest;
import com.disp.celesma.dto.user.UserResponse;
import com.disp.celesma.dto.user.UserUpdateProfileRequest;
import com.disp.celesma.mapper.UserMapper;
import com.disp.celesma.model.User;
import com.disp.celesma.repository.UserRepository;
import com.disp.celesma.s3.service.interfaces.IStorageService;
import com.disp.celesma.service.interfaces.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {

    private final UserRepository userRepository;
    /**
     * Реализация: {@link com.disp.celesma.s3.service.S3StorageService} — s3/service/S3StorageService.java*/
    private final IStorageService storageService;

    private final PasswordEncoder passwordEncoder;

    private final UserMapper userMapper;

    /**
     * Creates a new user from the registration request and persists it to the database.
     * Validates that the username and email are unique before saving.
     *
     * @param request the registration request containing user details (nickname, email, password, firstName, lastName)
     * @return the saved {@link User} entity with encoded password, ROLE_USER role, and current date as registration date
     * @throws IllegalStateException if the nickname or email is already taken
     */
    @Override
    @Transactional
    public User createUserAndSave(RegisterRequest request) {
        // Validate uniqueness of username and email
        if (userRepository.existsByUsername(request.nickname()))
            throw new IllegalStateException("Никнейм уже занят: " + request.nickname());
        if (userRepository.existsByEmail(request.email()))
            throw new IllegalStateException("Email уже занят: " + request.email());

        // Build and save the new user entity with encoded password and default role
        return userRepository.save(User.builder()
                .username(request.nickname())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .firstName(request.firstName())
                .lastName(request.lastName())
                .role("ROLE_USER")
                .registrationDate(LocalDate.now())
                .build());


    }

    @Override
    public UserResponse updateAvatar(User user, MultipartFile file) {
        // Удаляем старую аватарку если есть
        if (user.getAvatarUrl() != null) {
            storageService.deleteAvatar(user.getAvatarUrl());
        }

        String url = storageService.uploadAvatar(file, user.getUsername());
        user.setAvatarUrl(url);
        userRepository.save(user);

        return userMapper.toResponse(user);
    }

    /**
     * Retrieves a {@link User} entity from the database by username.
     *
     * @param username the username to search for
     * @return the found {@link User} entity
     * @throws UsernameNotFoundException if no user exists with the given username
     */
    @Override
    public User getUserEntityByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    /**
     * Retrieves a user by username and returns it as a response DTO.
     *
     * @param username the username to search for
     * @return the {@link UserResponse} mapped from the found user entity
     * @throws UsernameNotFoundException if no user exists with the given username
     */
    @Override
    public UserResponse getUserByUsername(String username) {
        return userMapper.toResponse(getUserEntityByUsername(username));
    }


    @Override
    public User getUserEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + id));
    }

    @Override
    public UserResponse getUserById(Long uid) {
        return userMapper.toResponse(getUserEntityById(uid));

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
/*    @Override
    @Transactional
    public User createOAuthUserAndSave(String email, String firstName, String lastName, String avatarUrl) {
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
    }*/

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    public UserResponse updateUserProfileAndSave(User user, UserUpdateProfileRequest request) {
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setBio(request.bio());
        var saved = save(user);
        return userMapper.toResponse(saved);
    }

}
