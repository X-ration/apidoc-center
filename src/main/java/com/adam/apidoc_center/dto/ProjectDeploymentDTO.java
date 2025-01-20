package com.adam.apidoc_center.dto;

import com.adam.apidoc_center.domain.ProjectDeployment;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProjectDeploymentDTO {
    private long id;
    private String environment;
    private String deploymentUrl;
    public ProjectDeploymentDTO(ProjectDeployment projectDeployment) {
        this.id = projectDeployment.getId();
        this.environment = projectDeployment.getEnvironment();
        this.deploymentUrl = projectDeployment.getDeploymentUrl();
    }
}