package com.adam.apidoc_center.domain;

import com.adam.apidoc_center.common.AbstractAuditable;
import com.adam.apidoc_center.dto.ProjectCreateOrUpdateDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@EqualsAndHashCode(callSuper = false)
@Entity
@Data
@NoArgsConstructor
public class ProjectDeployment extends AbstractAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(name = "project_id")
    private long projectId;
    private String environment;
    private String deploymentUrl;
    private boolean isEnabled;
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "project_id", insertable = false, updatable = false)
    private Project project;

    public ProjectDeployment(long projectId, ProjectCreateOrUpdateDTO.ProjectDeploymentCreateOrUpdateDTO projectDeploymentCreateOrUpdateDTO) {
        this.projectId = projectId;
        this.environment = projectDeploymentCreateOrUpdateDTO.getEnvironment();
        this.deploymentUrl = projectDeploymentCreateOrUpdateDTO.getDeploymentUrl();
        this.isEnabled = true;
    }

}