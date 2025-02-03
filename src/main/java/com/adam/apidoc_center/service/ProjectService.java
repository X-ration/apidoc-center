package com.adam.apidoc_center.service;

import com.adam.apidoc_center.common.*;
import com.adam.apidoc_center.domain.Project;
import com.adam.apidoc_center.domain.ProjectDeployment;
import com.adam.apidoc_center.domain.ProjectSharedUser;
import com.adam.apidoc_center.domain.User;
import com.adam.apidoc_center.dto.*;
import com.adam.apidoc_center.repository.ProjectDeploymentRepository;
import com.adam.apidoc_center.repository.ProjectRepository;
import com.adam.apidoc_center.repository.ProjectSharedUserRepository;
import com.adam.apidoc_center.security.SecurityUtil;
import com.adam.apidoc_center.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private ProjectDeploymentRepository projectDeploymentRepository;
    @Autowired
    private ProjectSharedUserRepository projectSharedUserRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    public PagedData<ProjectListDisplayDTO> getProjectsPaged(int pageNum, int pageSize) {
        Assert.isTrue(pageNum >= 0 && pageSize > 0, "getProjectsPaged param invalid");
        PageRequest pageRequest = PageRequest.of(pageNum, pageSize, Sort.by(Sort.Direction.DESC, "id"));
        Page<Project> page = projectRepository.findAll(pageRequest);
        PagedData<Project> pagedData = PagedData.convert(page, pageRequest);
        return pagedData.map(ProjectListDisplayDTO::convert).map(projectListDisplayDTO -> {
            long userId = SecurityUtil.getUser().getId();
            String key = CacheConstants.PROJECT_FOLLOW_LIST_PREFIX + userId;
            Double score = redisTemplate.opsForZSet().score(key, projectListDisplayDTO.getId());
            projectListDisplayDTO.setFollow(score != null);
            return projectListDisplayDTO;
        });
    }

    public PagedData<ProjectListDisplayDTO> getFollowedProjectsPaged(int pageNum, int pageSize) {
        Assert.isTrue(pageNum >= 0 && pageSize > 0, "getFollowedProjectsPaged param invalid");
        long userId = SecurityUtil.getUser().getId();
        String key = CacheConstants.PROJECT_FOLLOW_LIST_PREFIX + userId;
        long offset = pageNum == 0 ? 0 : (long) pageNum * pageSize;
        long total = redisTemplate.opsForZSet().zCard(key);
        Set<ZSetOperations.TypedTuple<Object>> tupleSet = redisTemplate.opsForZSet().reverseRangeByScoreWithScores(key, Double.MIN_VALUE, Double.MAX_VALUE, offset, pageSize);
        List<ZSetOperations.TypedTuple<Object>> tupleList = tupleSet.stream().collect(Collectors.toList());
        tupleList.sort((t1,t2) -> -1 * Double.compare(t1.getScore(), t2.getScore()));
        List<Long> sortedProjectIdList = tupleList.stream()
                .map(ZSetOperations.TypedTuple::getValue)
                .map(value -> ((Number) value).longValue())
                .collect(Collectors.toList());
        List<Project> projectList = projectRepository.findProjectsByIdIn(sortedProjectIdList);
        Map<Long, Project> projectMap = new HashMap<>();
        for(Project project: projectList) {
            projectMap.put(project.getId(), project);
        }
        List<Project> sortedProjectList = sortedProjectIdList.stream()
                .map(projectMap::get)
                .collect(Collectors.toList());
        return new PagedData<>(sortedProjectList, pageNum, pageSize, total)
                .map(ProjectListDisplayDTO::convert)
                .map(ProjectListDisplayDTO::setFollow);
    }

    public Response<Void> followProject(long projectId) {
        if(projectId <= 0) {
            return Response.fail(StringConstants.PROJECT_ID_INVALID);
        }
        boolean exists = exists(projectId);
        if(!exists) {
            return Response.fail(StringConstants.PROJECT_ID_INVALID);
        }
        long userId = SecurityUtil.getUser().getId();
        String key = CacheConstants.PROJECT_FOLLOW_LIST_PREFIX + userId;
        double score = System.currentTimeMillis() * 1.0;
        try {
            Boolean result = redisTemplate.opsForZSet().add(key, projectId, score);
            if (result != null && result) {
                return Response.success();
            }
        } catch (Exception e) {
            log.error("followProject fail", e);
        }
        return Response.fail(StringConstants.PROJECT_FOLLOW_FAIL);
    }

    public Response<Void> unfollowProject(long projectId) {
        if(projectId <= 0) {
            return Response.fail(StringConstants.PROJECT_ID_INVALID);
        }
        boolean exists = exists(projectId);
        if(!exists) {
            return Response.fail(StringConstants.PROJECT_ID_INVALID);
        }
        long userId = SecurityUtil.getUser().getId();
        String key = CacheConstants.PROJECT_FOLLOW_LIST_PREFIX + userId;
        try {
            Long result = redisTemplate.opsForZSet().remove(key, projectId);
            if (result != null && result == 1) {
                return Response.success();
            }
        } catch (Exception e) {
            log.error("unfollowProject fail", e);
        }
        return Response.fail(StringConstants.PROJECT_UNFOLLOW_FAIL);
    }

    public ProjectDetailDisplayDTO getProjectDetail(long projectId) {
//        Assert.isTrue(projectId > 0, "getProjectDetail projectId<=0");
        if(projectId <= 0) {
            return null;
        }
        Optional<Project> projectOptional = projectRepository.findById(projectId);
        if(projectOptional.isEmpty()) {
            return null;
        }
        Project project = projectOptional.get();
        ProjectDetailDisplayDTO projectDetailDisplayDTO = ProjectDetailDisplayDTO.convert(project);
        processCreatorAndUpdater(projectDetailDisplayDTO);
        if(!CollectionUtils.isEmpty(project.getProjectSharedUserList())) {
            List<Long> shareUserIdList = project.getProjectSharedUserList().stream()
                    .filter(ProjectSharedUser::isShare)
                    .map(ProjectSharedUser::getUserId)
                    .collect(Collectors.toList());
            Map<Long, User> userMap = userService.queryUserMap(shareUserIdList);
            List<UserCoreDTO> userCoreDTOList = shareUserIdList.stream()
                    .map(userMap::get)
                    .filter(Objects::nonNull)
                    .map(UserCoreDTO::new)
                    .collect(Collectors.toList());
            projectDetailDisplayDTO.setSharedUserList(userCoreDTOList);
        }
        if(!CollectionUtils.isEmpty(project.getProjectDeploymentList())) {
            List<ProjectDeploymentDTO> projectDeploymentDTOList =
                    project.getProjectDeploymentList().stream()
                            .filter(ProjectDeployment::isEnabled)
                            .map(ProjectDeploymentDTO::new)
                            .collect(Collectors.toList());
            projectDetailDisplayDTO.setDeploymentList(projectDeploymentDTOList);
        }
        if(!CollectionUtils.isEmpty(project.getProjectGroupList())) {
            List<ProjectGroupDisplayDTO> projectGroupDisplayDTOList =
                    project.getProjectGroupList().stream()
                            .map(ProjectGroupDisplayDTO::new)
                            .collect(Collectors.toList());
            projectDetailDisplayDTO.setGroupList(projectGroupDisplayDTOList);
        }
        return projectDetailDisplayDTO;
    }

    private void processCreatorAndUpdater(ProjectDetailDisplayDTO projectDetailDisplayDTO) {
        List<Long> userIdList = new LinkedList<>();
        userIdList.add(projectDetailDisplayDTO.getCreateUserId());
        userIdList.add(projectDetailDisplayDTO.getUpdateUserId());
        Map<Long, User> userMap = userService.queryUserMap(userIdList);
        if(userMap.containsKey(projectDetailDisplayDTO.getCreateUserId())) {
            User user = userMap.get(projectDetailDisplayDTO.getCreateUserId());
            UserCoreDTO userCoreDTO = new UserCoreDTO(user);
            projectDetailDisplayDTO.setCreator(userCoreDTO);
        }
        if(userMap.containsKey(projectDetailDisplayDTO.getUpdateUserId())) {
            User user = userMap.get(projectDetailDisplayDTO.getUpdateUserId());
            UserCoreDTO userCoreDTO = new UserCoreDTO(user);
            projectDetailDisplayDTO.setUpdater(userCoreDTO);
        }
    }

    public ProjectDeployment findDeploymentById(long projectDeploymentId) {
        Assert.isTrue(projectDeploymentId > 0, "findDeploymentById projectDeploymentId<=0");
        Optional<ProjectDeployment> projectDeploymentOptional = projectDeploymentRepository.findById(projectDeploymentId);
        return projectDeploymentOptional.orElse(null);
    }

    public Response<Void> deleteProject(long projectId) {
        Assert.isTrue(projectId > 0, "deleteProject projectId<=0");
        Optional<Project> projectOptional = projectRepository.findById(projectId);
        if(projectOptional.isEmpty()) {
            return Response.fail(StringConstants.PROJECT_NOT_EXISTS);
        }
        try {
            projectRepository.deleteById(projectId);  //关联实体(分组)一并删除
            return Response.success();
        } catch (Exception e) {
            log.error("deleteProjectError", e);
            return Response.fail(StringConstants.PROJECT_DELETE_FAIL);
        }
    }

    @Transactional
    public Response<?> checkAndModify(ProjectDTO projectUpdateDTO, long projectId) {
        if(projectId <= 0) {
            return Response.fail(StringConstants.PROJECT_ID_INVALID);
        }
        Optional<Project> projectOptional = projectRepository.findById(projectId);
        if(projectOptional.isEmpty()) {
            return Response.fail(StringConstants.PROJECT_ID_INVALID);
        }
        Project project = projectOptional.get();
        ProjectErrorMsg projectErrorMsg = checkCreateParams(projectUpdateDTO);
        if(projectErrorMsg.hasError()) {
            return Response.fail(StringConstants.PROJECT_MODIFY_FAIL_CHECK_INPUT, projectErrorMsg);
        }
        //修改项目
        project.setName(projectUpdateDTO.getName());
        project.setDescription(projectUpdateDTO.getDescription());
        project.setAccessMode(projectUpdateDTO.getAccessMode());
        if(!CollectionUtils.isEmpty(project.getProjectSharedUserList())) {
            projectSharedUserRepository.deleteAll(project.getProjectSharedUserList());
            project.setProjectSharedUserList(null);
        }
        if(!CollectionUtils.isEmpty(projectUpdateDTO.getShareUserIdList())) {
            List<ProjectSharedUser> projectSharedUserList = projectUpdateDTO.getShareUserIdList().stream()
                    .map(userId -> new ProjectSharedUser(projectId, userId))
                    .collect(Collectors.toList());
            project.setProjectSharedUserList(projectSharedUserList);
        }
        if(!CollectionUtils.isEmpty(project.getProjectDeploymentList())) {
//            List<Long> projectDeploymentIdList = project.getProjectDeploymentList().stream()
//                    .map(ProjectDeployment::getId)
//                    .collect(Collectors.toList());
            //这个方法会报找不到实体的异常
//            projectDeploymentRepository.deleteAllById(projectDeploymentIdList);
            projectDeploymentRepository.deleteAll(project.getProjectDeploymentList());
            project.setProjectDeploymentList(null);
        }
        if(!CollectionUtils.isEmpty(projectUpdateDTO.getDeploymentList())) {
            List<ProjectDeployment> projectDeploymentList = projectUpdateDTO.getDeploymentList().stream()
                    .map(deployment -> new ProjectDeployment(projectId, deployment))
                    .collect(Collectors.toList());
            project.setProjectDeploymentList(projectDeploymentList);
        }
        projectRepository.save(project);
        return Response.success();
    }

    @Transactional
    public Response<?> checkAndCreate(ProjectDTO projectCreateDTO) {
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
        if(!CollectionUtils.isEmpty(projectCreateDTO.getShareUserIdList())) {
            List<ProjectSharedUser> projectSharedUserList = projectCreateDTO.getShareUserIdList().stream()
                    .map(userId -> new ProjectSharedUser(projectId, userId))
                    .collect(Collectors.toList());
            projectSharedUserRepository.saveAll(projectSharedUserList);
        }
        if(!CollectionUtils.isEmpty(projectCreateDTO.getDeploymentList())) {
            List<ProjectDeployment> projectDeploymentList = projectCreateDTO.getDeploymentList().stream()
                    .map(deployment -> new ProjectDeployment(projectId, deployment))
                    .collect(Collectors.toList());
            projectDeploymentRepository.saveAll(projectDeploymentList);
        }
        return Response.success(projectId);
    }

    public Project findById(long projectId) {
        Assert.isTrue(projectId > 0, "findById projectId <= 0");
        Optional<Project> projectOptional = projectRepository.findById(projectId);
        return projectOptional.orElse(null);
    }

    public boolean exists(long projectId) {
        Assert.isTrue(projectId > 0, "exists projectId <= 0");
        return projectRepository.existsById(projectId);
    }

    private ProjectErrorMsg checkCreateParams(ProjectDTO projectCreateDTO) {
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
        }
        String shareUserIds = projectCreateDTO.getShareUserIds();
        if(StringUtils.isNotBlank(shareUserIds)) {
            String[] splits = shareUserIds.split(",");
            List<Long> shareUserIdList = new LinkedList<>();
            if (splits.length > 1 || StringUtils.isNotEmpty(splits[0])) {
                for (String split : splits) {
                    try {
                        long userId = Long.parseLong(split);
                        shareUserIdList.add(userId);
                    } catch (NumberFormatException e) {
                        projectErrorMsg.setShareUserIds(StringConstants.PROJECT_ALLOW_USER_IDS_INVALID);
                        break;
                    }
                }
            }
            if (!projectErrorMsg.hasError()) {
                projectCreateDTO.setShareUserIdList(shareUserIdList);
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