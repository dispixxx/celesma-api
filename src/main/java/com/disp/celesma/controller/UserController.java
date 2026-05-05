package com.disp.celesma.controller;

import com.disp.celesma.model.User;
import com.disp.celesma.security.UserPrincipal;
import com.disp.celesma.service.interfaces.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;

    @GetMapping("/{username}")
    public ResponseEntity<User> getByUsername(@PathVariable String username) {
        return ResponseEntity.ok(userService.getUserByUsername(username));
    }

    @PutMapping("/profile")
    public ResponseEntity<User> updateProfile(
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserPrincipal principal) {
        User user = userService.getUserByUsername(principal.getUsername());
        user.setFirstName(body.get("firstName"));
        user.setLastName(body.get("lastName"));
        user.setBio(body.get("bio"));
        userService.save(user);
        return ResponseEntity.ok(user);
    }
}
