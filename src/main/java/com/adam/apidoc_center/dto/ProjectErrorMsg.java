package com.adam.apidoc_center.dto;

import lombok.Data;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Data
public class ProjectErrorMsg implements ErrorMsg{
    private String id;
    private String name;
    private String description;
    private String accessMode;
    private String allowUserIds;
    private List<ProjectDeploymentErrorMsg> deploymentList;

    @Override
    public boolean hasError() {
        boolean hasError = name != null || description != null;
        if(hasError) {
            return true;
        }
        if(CollectionUtils.isEmpty(deploymentList)) {
            return false;
        }
        for(ProjectDeploymentErrorMsg projectDeploymentErrorMsg: deploymentList) {
            if(projectDeploymentErrorMsg.hasError()) {
                return true;
            }
        }
        return false;
    }

    @Data
    public static class ProjectDeploymentErrorMsg implements ErrorMsg{
        private String environment;
        private String deploymentUrl;

        @Override
        public boolean hasError() {
            return environment != null || deploymentUrl != null;
        }
    }
}