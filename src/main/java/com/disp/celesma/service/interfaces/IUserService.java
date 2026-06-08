package com.disp.celesma.service.interfaces;

import com.disp.celesma.dto.auth.RegisterRequest;
import com.disp.celesma.dto.user.UserResponseDto;
import com.disp.celesma.dto.user.UserUpdateProfileRequest;
import com.disp.celesma.model.User;

public interface IUserService {
    User getUserEntityByUsername(String username);

    UserResponseDto getUserByUsername(String username);

    User getUserEntityById(Long id);

    UserResponseDto getUserById(Long id);

    User getUserByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    //    User createOAuthUserAndSave(String email, String firstName, String lastName, String avatarUrl);
    User save(User user);

    UserResponseDto updateUserProfileAndSave(User user, UserUpdateProfileRequest request);

    User createUserAndSave(RegisterRequest request);
}