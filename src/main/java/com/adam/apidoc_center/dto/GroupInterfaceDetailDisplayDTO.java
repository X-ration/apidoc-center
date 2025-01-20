package com.adam.apidoc_center.dto;

import com.adam.apidoc_center.domain.GroupInterface;
import com.adam.apidoc_center.domain.ProjectDeployment;
import com.adam.apidoc_center.util.LocalDateTimeUtil;
import lombok.Data;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class GroupInterfaceDetailDisplayDTO {

    private long id;
    private long projectId;
    private long groupId;
    private String name;
    private String description;
    private String relativePath;
    private String method;
    private GroupInterface.Type type;
    private GroupInterface.ResponseType responseType;
    private String typeDesc;
    private String createTime;
    private String updateTime;
    private long createUserId;
    private long updateUserId;
    private UserCoreDTO creator;
    private UserCoreDTO updater;
    private List<InterfaceHeaderDTO> headerList;
    private List<InterfaceFieldDisplayDTO> fieldList;
    private List<ProjectGroupDisplayDTO> groupList;
    private List<ProjectDeploymentDTO> projectDeploymentList;

    public GroupInterfaceDetailDisplayDTO(GroupInterface groupInterface) {
        this.id = groupInterface.getId();
        this.projectId = groupInterface.getProjectGroup().getProjectId();
        this.groupId = groupInterface.getGroupId();
        this.name = groupInterface.getName();
        this.description = groupInterface.getDescription();
        this.relativePath = groupInterface.getRelativePath();
        this.method = groupInterface.getMethod().name();
        this.type = groupInterface.getType();
        this.typeDesc = groupInterface.getType().getContentType();
        this.responseType = groupInterface.getResponseType();
        this.createTime = LocalDateTimeUtil.timeDiffFriendlyDesc(groupInterface.getCreateTime());
        this.updateTime = LocalDateTimeUtil.timeDiffFriendlyDesc(groupInterface.getUpdateTime());
        this.createUserId = groupInterface.getCreateUserId();
        this.updateUserId = groupInterface.getUpdateUserId();
        if(!CollectionUtils.isEmpty(groupInterface.getInterfaceHeaderList())) {
            this.headerList = groupInterface.getInterfaceHeaderList().stream()
                    .map(InterfaceHeaderDTO::new)
                    .collect(Collectors.toList());
        }
        if(!CollectionUtils.isEmpty(groupInterface.getInterfaceFieldList())) {
            this.fieldList = groupInterface.getInterfaceFieldList().stream()
                    .map(InterfaceFieldDisplayDTO::new)
                    .collect(Collectors.toList());
        }
        if(!CollectionUtils.isEmpty(groupInterface.getProjectGroup().getProject().getProjectGroupList())) {
            this.groupList = groupInterface.getProjectGroup().getProject().getProjectGroupList().stream()
                    .map(ProjectGroupDisplayDTO::new)
                    .collect(Collectors.toList());
        }
        if(!CollectionUtils.isEmpty(groupInterface.getProjectGroup().getProject().getProjectDeploymentList())) {
            this.projectDeploymentList = groupInterface.getProjectGroup().getProject().getProjectDeploymentList().stream()
                    .filter(ProjectDeployment::isEnabled)
                    .map(ProjectDeploymentDTO::new)
                    .collect(Collectors.toList());
        }
    }

}
