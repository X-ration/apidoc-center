package com.adam.apidoc_center.dto;

import com.adam.apidoc_center.domain.Project;
import lombok.Data;

@Data
public class ProjectDTO {

    private long id;
    private String name;
    private String description;
    private String accessMode;

    public static ProjectDTO convert(Project project) {
        ProjectDTO dto = new ProjectDTO();
        dto.setId(project.getId());
        dto.setName(project.getName());
        dto.setDescription(project.getDescription());
        dto.setAccessMode(project.getAccessMode().getDesc());
        return dto;
    }

}