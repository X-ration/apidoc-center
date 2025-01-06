package com.adam.apidoc_center.dto;

import com.adam.apidoc_center.domain.Project;
import lombok.Data;

import java.util.List;

@Data
public class ProjectDTO {
    private String name;
    private String description;
    private Project.AccessMode accessMode;
    private String shareUserIds;
    private List<Long> shareUserIdList;
    private List<ProjectDeploymentDTO> deploymentList;
}
