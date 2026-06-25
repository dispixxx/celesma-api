package com.disp.celesma.dto.project;

import com.disp.celesma.model.Project;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * DTO for {@link Project}
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ProjectPreviewResponse(Long id, String name, String description) {
}