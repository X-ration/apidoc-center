package com.adam.apidoc_center.service;

import com.adam.apidoc_center.common.PagedData;
import com.adam.apidoc_center.common.Response;
import com.adam.apidoc_center.common.StringConstants;
import com.adam.apidoc_center.domain.Project;
import com.adam.apidoc_center.domain.ProjectAllowedUser;
import com.adam.apidoc_center.domain.ProjectDeployment;
import com.adam.apidoc_center.dto.ProjectCreateOrUpdateDTO;
import com.adam.apidoc_center.dto.ProjectDetailDisplayDTO;
import com.adam.apidoc_center.dto.ProjectListDisplayDTO;
import com.adam.apidoc_center.dto.ProjectErrorMsg;
import com.adam.apidoc_center.repository.ProjectAllowedUserRepository;
import com.adam.apidoc_center.repository.ProjectDeploymentRepository;
import com.adam.apidoc_center.repository.ProjectRepository;
import com.adam.apidoc_center.security.ExtendedUser;
import com.adam.apidoc_center.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private ProjectDeploymentRepository projectDeploymentRepository;
    @Autowired
    private ProjectAllowedUserRepository projectAllowedUserRepository;

    public PagedData<ProjectListDisplayDTO> getProjectsPaged(int pageNum, int pageSize) {
        Assert.isTrue(pageNum >= 0 && pageSize > 0, "getProjectsPaged param invalid");
        PageRequest pageRequest = PageRequest.of(pageNum, pageSize, Sort.by(Sort.Direction.DESC, "id"));
        Page<Project> page = projectRepository.findAll(pageRequest);
        PagedData<Project> pagedData = PagedData.convert(page, pageRequest);
        return pagedData.map(ProjectListDisplayDTO::convert);
    }

    public ProjectDetailDisplayDTO getProjectDetail(long projectId) {
        Assert.isTrue(projectId > 0, "getProjectDetail projectId<=0");
        Optional<Project> project = projectRepository.findById(projectId);
        return project.map(ProjectDetailDisplayDTO::convert).orElse(null);
    }

    public Response<Void> deleteProject(long projectId) {
        Assert.isTrue(projectId > 0, "deleteProject projectId<=0");
        Optional<Project> projectOptional = projectRepository.findById(projectId);
        if(projectOptional.isEmpty()) {
            return Response.fail(StringConstants.PROJECT_NOT_EXISTS);
        }
        Project project = projectOptional.get();
        ExtendedUser extendedUser = (ExtendedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(project.getCreateUserId() != extendedUser.getUser().getId()) {
            return Response.fail(StringConstants.PROJECT_ONLY_OWNER_CAN_DELETE);
        }
        try {
            projectRepository.deleteById(projectId);
            return Response.success();
        } catch (Exception e) {
            log.error("deleteProjectError", e);
            return Response.fail(StringConstants.PROJECT_DELETE_FAIL);
        }
    }

    @Transactional
    public Response<?> checkAndCreate(ProjectCreateOrUpdateDTO projectCreateDTO) {
        ProjectErrorMsg projectErrorMsg = checkCreateParams(projectCreateDTO);
        if(projectErrorMsg.hasError()) {
            return Response.fail(StringConstants.PROJECT_CREATE_FAIL_CHECK_INPUT, projectErrorMsg);
        }
        //创建项目
        Project project = new Project();
        project.setName(projectCreateDTO.getName());
        project.setDescription(projectCreateDTO.getDescription());
        project.setAccessMode(projectCreateDTO.getAccessMode());
        projectRepository.save(project);
        long projectId = project.getId();
        if(!CollectionUtils.isEmpty(projectCreateDTO.getAllowUserIdList())) {
            List<ProjectAllowedUser> projectAllowedUserList = projectCreateDTO.getAllowUserIdList().stream()
                    .map(userId -> new ProjectAllowedUser(projectId, userId))
                    .collect(Collectors.toList());
            projectAllowedUserRepository.saveAll(projectAllowedUserList);
        }
        if(!CollectionUtils.isEmpty(projectCreateDTO.getDeploymentList())) {
            List<ProjectDeployment> projectDeploymentList = projectCreateDTO.getDeploymentList().stream()
                    .map(deployment -> new ProjectDeployment(projectId, deployment))
                    .collect(Collectors.toList());
            projectDeploymentRepository.saveAll(projectDeploymentList);
        }
        return Response.success(projectId);
    }

    private ProjectErrorMsg checkCreateParams(ProjectCreateOrUpdateDTO projectCreateDTO) {
        ProjectErrorMsg projectErrorMsg = new ProjectErrorMsg();
        if(StringUtils.isBlank(projectCreateDTO.getName())) {
            projectErrorMsg.setName(StringConstants.PROJECT_NAME_BLANK);
        } else if(projectCreateDTO.getName().length() > 32) {
            projectErrorMsg.setName(StringConstants.PROJECT_NAME_LENGTH_EXCEEDED);
        }
        if(StringUtils.isBlank(projectCreateDTO.getDescription())) {
            projectCreateDTO.setDescription(null);
        } else if(projectCreateDTO.getDescription().length() > 200) {
            projectErrorMsg.setDescription(StringConstants.PROJECT_DESCRIPTION_LENGTH_EXCEEDED);
        }
        if(projectCreateDTO.getAccessMode() == null) {
            projectErrorMsg.setAccessMode(StringConstants.PROJECT_ACCESS_MODE_NULL);
        } else if(projectCreateDTO.getAccessMode() == Project.AccessMode.PRIVATE){
            String allowUserIds = projectCreateDTO.getAllowUserIds();
            if(StringUtils.isNotBlank(allowUserIds)) {
                String[] splits = allowUserIds.split(",");
                List<Long> allowUserIdList = new LinkedList<>();
                if (splits.length > 1 || StringUtils.isNotEmpty(splits[0])) {
                    for (String split : splits) {
                        try {
                            long userId = Long.parseLong(split);
                            allowUserIdList.add(userId);
                        } catch (NumberFormatException e) {
                            projectErrorMsg.setAllowUserIds(StringConstants.PROJECT_ALLOW_USER_IDS_INVALID);
                            break;
                        }
                    }
                }
                if (!projectErrorMsg.hasError()) {
                    projectCreateDTO.setAllowUserIdList(allowUserIdList);
                }
            }
        }
        if(!CollectionUtils.isEmpty(projectCreateDTO.getDeploymentList())) {
            projectErrorMsg.setDeploymentList(projectCreateDTO.getDeploymentList().stream()
                    .map(projectDeployment -> {
                        ProjectErrorMsg.ProjectDeploymentErrorMsg projectDeploymentErrorMsg = new ProjectErrorMsg.ProjectDeploymentErrorMsg();
                        if(StringUtils.isBlank(projectDeployment.getEnvironment())) {
                            projectDeploymentErrorMsg.setEnvironment(StringConstants.PROJECT_DEPLOYMENT_ENVIRONMENT_BLANK);
                        } else if(projectDeployment.getEnvironment().length() > 32) {
                            projectDeploymentErrorMsg.setEnvironment(StringConstants.PROJECT_DEPLOYMENT_ENVIRONMENT_LENGTH_EXCEEDED);
                        }
                        if(StringUtils.isBlank(projectDeployment.getDeploymentUrl())) {
                            projectDeploymentErrorMsg.setDeploymentUrl(StringConstants.PROJECT_DEPLOYMENT_URL_BLANK);
                        } else if(projectDeployment.getDeploymentUrl().length() > 256) {
                            projectDeploymentErrorMsg.setDeploymentUrl(StringConstants.PROJECT_DEPLOYMENT_URL_LENGTH_EXCEEDED);
                        } else if(!StringUtil.isHttpOrHttpsUrl(projectDeployment.getDeploymentUrl())) {
                            projectDeploymentErrorMsg.setDeploymentUrl(StringConstants.PROJECT_DEPLOYMENT_URL_INVALID);
                        }
                        return projectDeploymentErrorMsg;
                    }).collect(Collectors.toList()));
        }
        return projectErrorMsg;
    }

}