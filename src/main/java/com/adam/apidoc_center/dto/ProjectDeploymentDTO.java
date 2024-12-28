package com.adam.apidoc_center.dto;

import com.adam.apidoc_center.domain.ProjectDeployment;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProjectDeploymentDTO {
    private String environment;
    private String deploymentUrl;
    public ProjectDeploymentDTO(ProjectDeployment projectDeployment) {
        this.environment = projectDeployment.getEnvironment();
        this.deploymentUrl = projectDeployment.getDeploymentUrl();
    }
}