package com.adam.apidoc_center.dto;

import lombok.Data;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Data
public class ProjectErrorMsg {
    private String name;
    private String description;
    private String accessMode;
    private String allowUserIds;
    private List<ProjectDeploymentErrorMsg> deploymentList;

    public boolean hasError() {
        boolean hasError = name != null || description != null;
        if(hasError) {
            return true;
        }
        if(CollectionUtils.isEmpty(deploymentList)) {
            return false;
        }
        for(ProjectDeploymentErrorMsg projectDeploymentErrorMsg: deploymentList) {
            if(projectDeploymentErrorMsg.environment != null || projectDeploymentErrorMsg.deploymentUrl != null) {
                return true;
            }
        }
        return false;
    }

    @Data
    public static class ProjectDeploymentErrorMsg {
        private String environment;
        private String deploymentUrl;
    }
}