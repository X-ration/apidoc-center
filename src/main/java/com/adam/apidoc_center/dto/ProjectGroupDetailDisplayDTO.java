package com.adam.apidoc_center.dto;

import com.adam.apidoc_center.domain.Project;
import com.adam.apidoc_center.domain.ProjectGroup;
import com.adam.apidoc_center.util.LocalDateTimeUtil;
import lombok.Data;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class ProjectGroupDetailDisplayDTO {

    private long projectId;
    private long id;
    private String name;
    private String createTime;
    private String updateTime;
    private UserCoreDTO creator;
    private UserCoreDTO updater;
    private long createUserId;
    private long updateUserId;
    private List<ProjectGroupDetailListDTO> groupList;

    public ProjectGroupDetailDisplayDTO(ProjectGroup projectGroup) {
        this.projectId = projectGroup.getProjectId();
        this.id = projectGroup.getId();
        this.name = projectGroup.getName();
        this.createTime = LocalDateTimeUtil.timeDiffFriendlyDesc(projectGroup.getCreateTime());
        this.updateTime = LocalDateTimeUtil.timeDiffFriendlyDesc(projectGroup.getUpdateTime());
        this.createUserId = projectGroup.getCreateUserId();
        this.updateUserId = projectGroup.getUpdateUserId();
        Project project = projectGroup.getProject();
        List<ProjectGroup> projectGroupList = project.getProjectGroupList();
        if(!CollectionUtils.isEmpty(projectGroupList)) {
            this.groupList = projectGroupList.stream()
                    .map(ProjectGroupDetailListDTO::new)
                    .collect(Collectors.toList());
        }
    }

    @Data
    public static class ProjectGroupDetailListDTO {
        private long id;
        private String name;
        public ProjectGroupDetailListDTO(ProjectGroup projectGroup) {
            this.id = projectGroup.getId();
            this.name = projectGroup.getName();
        }
    }

}