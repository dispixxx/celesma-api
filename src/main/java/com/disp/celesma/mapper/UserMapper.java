package com.disp.celesma.mapper;

import com.disp.celesma.dto.user.UserResponseDto;
import com.disp.celesma.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface UserMapper {

    /**
     * User → UserResponseDto (null-безопасный).
     */
    @Mapping(target = "id", source = "id")
    @Mapping(target = "username", source = "username")
    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "lastName", source = "lastName")
    @Mapping(target = "avatarUrl", source = "avatarUrl")
    @Mapping(target = "role", source = "role")
    @Mapping(target = "registrationDate", source = "registrationDate")
    UserResponseDto toResponseDto(User user);
}