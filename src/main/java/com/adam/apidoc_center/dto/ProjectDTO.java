package com.adam.apidoc_center.dto;

import com.adam.apidoc_center.domain.Project;
import com.adam.apidoc_center.util.LocalDateTimeUtil;
import lombok.Data;

@Data
public class ProjectDTO {

    private long id;
    private String name;
    private String description;
    private String accessMode;
    private String createTime;
    private String updateTime;

    public static ProjectDTO convert(Project project) {
        ProjectDTO dto = new ProjectDTO();
        dto.setId(project.getId());
        dto.setName(project.getName());
        dto.setDescription(project.getDescription());
        dto.setAccessMode(project.getAccessMode().getDesc());
        dto.setCreateTime(LocalDateTimeUtil.timeDiffFriendlyDesc(project.getCreateTime()));
        dto.setUpdateTime(LocalDateTimeUtil.timeDiffFriendlyDesc(project.getUpdateTime()));
        return dto;
    }

}