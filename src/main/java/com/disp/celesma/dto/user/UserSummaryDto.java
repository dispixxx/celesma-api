package com.disp.celesma.dto.user;

import com.disp.celesma.model.User;

public record UserSummaryDto(
        Long id,
        String username,
        String firstName,
        String lastName,
        String avatarUrl
) {
    public static UserSummaryDto from(User user) {
        if (user == null) return null;
        return new UserSummaryDto(
                user.getId(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getAvatarUrl()
        );
    }
}
