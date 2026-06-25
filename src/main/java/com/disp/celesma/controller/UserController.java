package com.disp.celesma.controller;

import com.disp.celesma.dto.user.UserResponse;
import com.disp.celesma.dto.user.UserUpdateProfileRequest;
import com.disp.celesma.security.UserPrincipal;
import com.disp.celesma.service.interfaces.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;

    @GetMapping("/{username}")
    public ResponseEntity<UserResponse> getByUsername(@PathVariable String username) {
        return ResponseEntity.ok(userService.getUserByUsername(username));
    }

    @PutMapping("/profile")
    public ResponseEntity<UserResponse> updateProfile(
            @RequestBody UserUpdateProfileRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        var user = userService.updateUserProfileAndSave(principal.getUser(), request);
        return ResponseEntity.ok(user);
    }

    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserResponse> uploadAvatar(
            @RequestPart("file") MultipartFile file,
            @AuthenticationPrincipal UserPrincipal principal) {
        var user = userService.updateAvatar(principal.getUser(), file);
        return ResponseEntity.ok(user);
    }
}
