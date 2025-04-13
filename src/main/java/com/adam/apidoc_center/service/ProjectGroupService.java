package com.adam.apidoc_center.service;

import com.adam.apidoc_center.common.Response;
import com.adam.apidoc_center.common.StringConstants;
import com.adam.apidoc_center.domain.Project;
import com.adam.apidoc_center.domain.ProjectGroup;
import com.adam.apidoc_center.domain.User;
import com.adam.apidoc_center.dto.ProjectGroupDTO;
import com.adam.apidoc_center.dto.ProjectGroupDetailDisplayDTO;
import com.adam.apidoc_center.dto.ProjectGroupErrorMsg;
import com.adam.apidoc_center.dto.UserCoreDTO;
import com.adam.apidoc_center.repository.ProjectGroupRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ProjectGroupService {

    @Autowired
    private ProjectGroupRepository projectGroupRepository;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private UserService userService;
    @Autowired
    private LuceneService luceneService;

    public ProjectGroupDetailDisplayDTO getGroupDetail(long groupId) {
        if(groupId <= 0) {
            return null;
        }
        Optional<ProjectGroup> projectGroupOptional = projectGroupRepository.findById(groupId);
        if(projectGroupOptional.isEmpty()) {
            return null;
        }
        ProjectGroup projectGroup = projectGroupOptional.get();
        ProjectGroupDetailDisplayDTO projectGroupDetailDisplayDTO = new ProjectGroupDetailDisplayDTO(projectGroup);
        processCreatorAndUpdater(projectGroupDetailDisplayDTO);
        return projectGroupDetailDisplayDTO;
    }

    public boolean exists(long groupId) {
        Assert.isTrue(groupId > 0, "exists groupId<=0");
        return projectGroupRepository.existsById(groupId);
    }

    public ProjectGroup findById(long groupId) {
        Assert.isTrue(groupId > 0, "findById groupId<=0");
        return projectGroupRepository.findById(groupId).orElse(null);
    }

    public Long getProjectId(long groupId) {
        Assert.isTrue(groupId > 0, "getProjectId groupId<=0");
        Optional<ProjectGroup> projectGroupOptional = projectGroupRepository.findById(groupId);
        if(projectGroupOptional.isEmpty()) {
            return null;
        }
        ProjectGroup projectGroup = projectGroupOptional.get();
        return projectGroup.getProjectId();
    }

    public Response<Void> deleteGroup(long groupId) {
        if(groupId <= 0) {
            return Response.fail(StringConstants.PROJECT_GROUP_ID_INVALID);
        }
        Optional<ProjectGroup> projectGroupOptional = projectGroupRepository.findById(groupId);
        if(projectGroupOptional.isEmpty()) {
            return Response.fail(StringConstants.PROJECT_GROUP_ID_INVALID);
        }
        ProjectGroup projectGroup = projectGroupOptional.get();
        projectGroupRepository.delete(projectGroup);
        luceneService.deleteGroup(projectGroup);
        return Response.success();
    }

    private void processCreatorAndUpdater(ProjectGroupDetailDisplayDTO projectGroupDetailDisplayDTO) {
        List<Long> userIdList = new LinkedList<>();
        userIdList.add(projectGroupDetailDisplayDTO.getCreateUserId());
        userIdList.add(projectGroupDetailDisplayDTO.getUpdateUserId());
        Map<Long, User> userMap = userService.queryUserMap(userIdList);
        if(userMap.containsKey(projectGroupDetailDisplayDTO.getCreateUserId())) {
            User user = userMap.get(projectGroupDetailDisplayDTO.getCreateUserId());
            UserCoreDTO userCoreDTO = new UserCoreDTO(user);
            projectGroupDetailDisplayDTO.setCreator(userCoreDTO);
        }
        if(userMap.containsKey(projectGroupDetailDisplayDTO.getUpdateUserId())) {
            User user = userMap.get(projectGroupDetailDisplayDTO.getUpdateUserId());
            UserCoreDTO userCoreDTO = new UserCoreDTO(user);
            projectGroupDetailDisplayDTO.setUpdater(userCoreDTO);
        }
    }

    public Response<?> checkAndModify(long groupId, ProjectGroupDTO projectGroupDTO) {
        if(groupId <= 0) {
            return Response.fail(StringConstants.PROJECT_GROUP_ID_INVALID);
        }
        Optional<ProjectGroup> projectGroupOptional = projectGroupRepository.findById(groupId);
        if(projectGroupOptional.isEmpty()) {
            return Response.fail(StringConstants.PROJECT_GROUP_ID_INVALID);
        }
        ProjectGroup projectGroup = projectGroupOptional.get();
        ProjectGroupErrorMsg projectGroupErrorMsg = checkCreateParams(projectGroupDTO);
        if(projectGroupErrorMsg.hasError()) {
            return Response.fail(StringConstants.PROJECT_GROUP_MODIFY_FAIL_CHECK_INPUT, projectGroupErrorMsg);
        }
        projectGroup.setName(projectGroupDTO.getName());
        projectGroupRepository.save(projectGroup);
        luceneService.updateGroup(projectGroup);
        return Response.success();
    }

    @Transactional
    public Response<?> checkAndCreate(long projectId, ProjectGroupDTO projectGroupDTO) {
        if(projectId <= 0) {
            return Response.fail(StringConstants.PROJECT_ID_INVALID);
        }
        Project project = projectService.findById(projectId);
        if(project == null) {
            return Response.fail(StringConstants.PROJECT_ID_INVALID);
        }
        ProjectGroupErrorMsg projectGroupErrorMsg = checkCreateParams(projectGroupDTO);
        if(projectGroupErrorMsg.hasError()) {
            return Response.fail(StringConstants.PROJECT_GROUP_CREATE_FAIL_CHECK_INPUT, projectGroupErrorMsg);
        }
        ProjectGroup projectGroup = new ProjectGroup();
        projectGroup.setProjectId(projectId);
        projectGroup.setName(projectGroupDTO.getName());
        projectGroupRepository.save(projectGroup);
        projectGroup.setProject(project);   //手动set,避免取不出来关联对象
        luceneService.createGroup(projectGroup);
        return Response.success();
    }

    private ProjectGroupErrorMsg checkCreateParams(ProjectGroupDTO projectGroupDTO) {
        ProjectGroupErrorMsg projectGroupErrorMsg = new ProjectGroupErrorMsg();
        if(StringUtils.isBlank(projectGroupDTO.getName())) {
            projectGroupErrorMsg.setName(StringConstants.PROJECT_GROUP_NAME_BLANK);
        } else if(projectGroupDTO.getName().length() > 32) {
            projectGroupErrorMsg.setName(StringConstants.PROJECT_GROUP_NAME_LENGTH_EXCEEDED);
        }
        return projectGroupErrorMsg;
    }

}
