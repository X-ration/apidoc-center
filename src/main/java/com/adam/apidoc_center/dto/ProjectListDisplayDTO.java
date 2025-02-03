package com.adam.apidoc_center.dto;

import com.adam.apidoc_center.domain.Project;
import com.adam.apidoc_center.util.LocalDateTimeUtil;
import lombok.Data;

@Data
public class ProjectListDisplayDTO {

    private long id;
    private String name;
    private String description;
    private String accessMode;
    private String createTime;
    private String updateTime;
    private boolean follow;

    public static ProjectListDisplayDTO convert(Project project) {
        ProjectListDisplayDTO dto = new ProjectListDisplayDTO();
        dto.setId(project.getId());
        dto.setName(project.getName());
        if(project.getDescription() != null) {
            if (project.getDescription().length() <= 100) {
                dto.setDescription(project.getDescription());
            } else {
                dto.setDescription(project.getDescription().substring(0, 97) + "...");
            }
        }
        dto.setAccessMode(project.getAccessMode().getDesc());
        dto.setCreateTime(LocalDateTimeUtil.timeDiffFriendlyDesc(project.getCreateTime()));
        dto.setUpdateTime(LocalDateTimeUtil.timeDiffFriendlyDesc(project.getUpdateTime()));
        return dto;
    }

}