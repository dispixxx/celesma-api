package com.disp.celesma.dto.project;

import com.disp.celesma.model.enums.ProjectRole;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RoleUpdateRequest {
    @NotNull(message = "Role is required")
    private ProjectRole role;
    @NotNull(message = "Project ID is required")
    private Long ProjectId;
}
