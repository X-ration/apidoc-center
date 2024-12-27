package com.adam.apidoc_center.dto;

import com.adam.apidoc_center.domain.Project;
import lombok.Data;

import java.util.List;

@Data
public class ProjectCreateOrUpdateDTO {
    private String name;
    private String description;
    private Project.AccessMode accessMode;
    private String allowUserIds;
    private List<Long> allowUserIdList;
    private List<ProjectDeploymentCreateOrUpdateDTO> deploymentList;
}
