package com.adam.apidoc_center.dto;

import com.adam.apidoc_center.domain.ProjectGroup;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class ProjectGroupDisplayDTO extends ProjectGroupDTO {

    protected long id;
    protected List<GroupInterfaceListDisplayDTO> interfaceList;

    public ProjectGroupDisplayDTO(ProjectGroup projectGroup) {
        this.id = projectGroup.getId();
        this.name = projectGroup.getName();
        if(!CollectionUtils.isEmpty(projectGroup.getGroupInterfaceList())) {
            this.interfaceList = projectGroup.getGroupInterfaceList().stream()
                    .map(groupInterface -> {
                        GroupInterfaceListDisplayDTO dto = new GroupInterfaceListDisplayDTO();
                        dto.setId(groupInterface.getId());
                        dto.setName(groupInterface.getName());
                        return dto;
                    }).collect(Collectors.toList());
        }
    }

}
