package com.adam.apidoc_center.dto;

import com.adam.apidoc_center.domain.ProjectGroup;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class ProjectGroupDisplayDTO extends ProjectGroupDTO {

    protected long id;

    public ProjectGroupDisplayDTO(ProjectGroup projectGroup) {
        this.id = projectGroup.getId();
        this.name = projectGroup.getName();
    }

}
