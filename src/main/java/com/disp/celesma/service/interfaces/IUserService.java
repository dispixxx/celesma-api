package com.disp.celesma.service.interfaces;

import com.disp.celesma.model.User;

public interface IUserService {
    User getUserByUsername(String username);
    User getUserById(Long id);
    User getUserByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsById(Long id);
    User create(String email, String rawPassword, String firstName, String lastName, String nickname);
    User createOAuthUser(String email, String firstName, String lastName, String avatarUrl);
    void save(User user);
}
