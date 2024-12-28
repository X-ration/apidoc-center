package com.adam.apidoc_center.dto;

import com.adam.apidoc_center.domain.Project;
import com.adam.apidoc_center.util.LocalDateTimeUtil;
import lombok.Data;

import java.util.List;

@Data
public class ProjectDetailDisplayDTO {

    private long id;
    private String name;
    private String description;
    private String accessMode;
    private String createTime;
    private String updateTime;
    private List<UserCoreDTO> allowedUserList;
    private List<ProjectDeploymentDTO> deploymentList;

    public static ProjectDetailDisplayDTO convert(Project project) {
        ProjectDetailDisplayDTO dto = new ProjectDetailDisplayDTO();
        dto.setId(project.getId());
        dto.setName(project.getName());
        dto.setDescription(project.getDescription());
        dto.setAccessMode(project.getAccessMode().getDesc());
        dto.setCreateTime(LocalDateTimeUtil.timeDiffFriendlyDesc(project.getCreateTime()));
        dto.setUpdateTime(LocalDateTimeUtil.timeDiffFriendlyDesc(project.getUpdateTime()));
        return dto;
    }

}