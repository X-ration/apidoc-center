package com.adam.apidoc_center.security;

import com.adam.apidoc_center.common.SystemConstants;
import com.adam.apidoc_center.domain.Project;
import com.adam.apidoc_center.domain.ProjectSharedUser;
import com.adam.apidoc_center.domain.User;
import com.adam.apidoc_center.service.GroupInterfaceService;
import com.adam.apidoc_center.service.ProjectGroupService;
import com.adam.apidoc_center.service.ProjectService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ProjectAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    private final AuthenticationTrustResolver trustResolver = new AuthenticationTrustResolverImpl();
    @Autowired
    private ProjectService projectService;
    @Autowired
    private ProjectGroupService projectGroupService;
    @Autowired
    private GroupInterfaceService groupInterfaceService;
    private final String PROJECT_ID_VARIABLE_NAME = "projectId";
    private final String GROUP_ID_VARIABLE_NAME = "groupId";
    private final String INTERFACE_ID_VARIABLE_NAME = "interfaceId";
    private final String ACTION_VARIABLE_NAME = "action";
    private final AntPathRequestMatcher projectRequestMatcher = new AntPathRequestMatcher("/project/{"
            + PROJECT_ID_VARIABLE_NAME + "}/{" + ACTION_VARIABLE_NAME + "}");
    private final AntPathRequestMatcher projectCreateGroupRequestMatcher = new AntPathRequestMatcher("/project/{"
            + PROJECT_ID_VARIABLE_NAME + "}/group/create");
    private final AntPathRequestMatcher groupRequestMatcher = new AntPathRequestMatcher("/group/{"
            + GROUP_ID_VARIABLE_NAME + "}/{" + ACTION_VARIABLE_NAME + "}");
    private final AntPathRequestMatcher groupCreateInterfaceRequestMatcher = new AntPathRequestMatcher("/group/{"
            + GROUP_ID_VARIABLE_NAME + "}/interface/create");
    private final AntPathRequestMatcher interfaceRequestMatcher = new AntPathRequestMatcher("/interface/{"
            + INTERFACE_ID_VARIABLE_NAME + "}/{" + ACTION_VARIABLE_NAME + "}");


    @Override
    @Transactional
    public AuthorizationDecision check(Supplier<Authentication> authentication, RequestAuthorizationContext object) {
        String requestURI = object.getRequest().getRequestURI();
        //检查是否是remember-me登录
        log.info("ProjectAuthorizationManager check starts requestURI={} rememberMe={}", requestURI, trustResolver.isRememberMe(authentication.get()));
        Long projectId = null;
        String action = null;
        List<String> registeredActionList = Arrays.asList("view","modify","delete","call","callForm", "follow", "unfollow");
        //不允许匿名访问
        if(authentication.get() instanceof AnonymousAuthenticationToken) {
            return new AuthorizationDecision(false);
        }
        //系统管理员全部放行
        if(hasAdminAuthority(authentication.get())) {
            return new AuthorizationDecision(true);
        }
        User user = SecurityUtil.getUser();
        long userId = user == null ? SystemConstants.INVALID_USER_ID : user.getId();
        String pathPrefix = null;

        if(requestURI.startsWith("/project")) {
            pathPrefix = "/project";
            //任何人都可以创建项目、查看（关注的）项目
            if(requestURI.equals("/project/create") || requestURI.equals("/project/viewAll") || requestURI.equals("/project/viewFollow")) {
                return new AuthorizationDecision(true);
            }
            String projectIdString = null;
            if(projectRequestMatcher.matches(object.getRequest())) {
                projectIdString = resolveVariableValue(projectRequestMatcher, object.getRequest(), PROJECT_ID_VARIABLE_NAME);
                action = resolveVariableValue(projectRequestMatcher, object.getRequest(), ACTION_VARIABLE_NAME);
                if(!registeredActionList.contains(action)) {
                    return new AuthorizationDecision(false);
                }
                //任何人都可以关注、取消关注项目
                else if(action.equals("follow") || action.equals("unfollow")) {
                    return new AuthorizationDecision(true);
                }
            } else if(projectCreateGroupRequestMatcher.matches(object.getRequest())) {
                projectIdString = resolveVariableValue(projectCreateGroupRequestMatcher, object.getRequest(), PROJECT_ID_VARIABLE_NAME);
                action = "create";
            }
            if(projectIdString == null) {
                return new AuthorizationDecision(false);
            } else {
                try {
                    projectId = Long.parseLong(projectIdString);
                } catch (NumberFormatException e) {
                    return new AuthorizationDecision(false);
                }
            }
        } else if(requestURI.startsWith("/group")) {
            pathPrefix = "/group";
            String groupIdString = null;
            if(groupRequestMatcher.matches(object.getRequest())) {
                groupIdString = resolveVariableValue(groupRequestMatcher, object.getRequest(), GROUP_ID_VARIABLE_NAME);
                action = resolveVariableValue(groupRequestMatcher, object.getRequest(), ACTION_VARIABLE_NAME);
                if(!registeredActionList.contains(action)) {
                    return new AuthorizationDecision(false);
                }
            } else if(groupCreateInterfaceRequestMatcher.matches(object.getRequest())) {
                groupIdString = resolveVariableValue(groupCreateInterfaceRequestMatcher, object.getRequest(), GROUP_ID_VARIABLE_NAME);
                action = "create";
            }
            if(groupIdString == null) {
                return new AuthorizationDecision(false);
            } else {
                try {
                    long groupId = Long.parseLong(groupIdString);
                    projectId = projectGroupService.getProjectId(groupId);
                } catch (NumberFormatException e) {
                    return new AuthorizationDecision(false);
                }
            }
        } else if(requestURI.startsWith("/interface")) {
            pathPrefix = "/interface";
            if(interfaceRequestMatcher.matches(object.getRequest())) {
                String interfaceIdString = resolveVariableValue(interfaceRequestMatcher, object.getRequest(), INTERFACE_ID_VARIABLE_NAME);
                action = resolveVariableValue(interfaceRequestMatcher, object.getRequest(), ACTION_VARIABLE_NAME);
                if(!registeredActionList.contains(action)) {
                    return new AuthorizationDecision(false);
                }
                try {
                    long interfaceId = Long.parseLong(interfaceIdString);
                    projectId = groupInterfaceService.getProjectId(interfaceId);
                } catch (NumberFormatException e) {
                    return new AuthorizationDecision(false);
                }
            }
        }

        if(projectId == null || action == null) {
            return new AuthorizationDecision(false);
        }
        Project project = projectService.findById(projectId);
        if(project == null) {
            return new AuthorizationDecision(false);
        }
        //公开项目可自由查看、调用接口
        if((action.equals("view") || (StringUtils.equals(pathPrefix, "/interface") && action.startsWith("call")))
                && project.getAccessMode() == Project.AccessMode.PUBLIC) {
            return new AuthorizationDecision(true);
        }
        List<Long> shareUserIdList = new LinkedList<>();
        if(!CollectionUtils.isEmpty(project.getProjectSharedUserList())) {
            shareUserIdList = project.getProjectSharedUserList().stream()
                    .filter(ProjectSharedUser::isShare)
                    .map(ProjectSharedUser::getUserId)
                    .collect(Collectors.toList());
        }
        //修改、删除、创建或查看私有项目或调用私有项目中的接口都需要管理员用户或创建者或项目分享者权限
        if(project.getCreateUserId() == userId || shareUserIdList.contains(userId)) {
            return new AuthorizationDecision(true);
        } else {
            return new AuthorizationDecision(false);
        }
    }

    private boolean hasAdminAuthority(Authentication authentication) {
        Collection<? extends GrantedAuthority> grantedAuthorityList = authentication.getAuthorities();
        for(GrantedAuthority grantedAuthority: grantedAuthorityList) {
            if(grantedAuthority.getAuthority().equals("ROLE_ADMIN")) {
                return true;
            }
        }
        return false;
    }

    private String resolveVariableValue(AntPathRequestMatcher requestMatcher, HttpServletRequest request, String variableKey) {
        Map<String,String> variableMap = requestMatcher.matcher(request).getVariables();
        return variableMap.get(variableKey);
    }

    public static void main(String[] args) {
        AntPathMatcher matcher = new AntPathMatcher();
        System.out.println(matcher.match("/projects/**", "/projects"));
        System.out.println(matcher.match("/projects/**", "/projects/123"));
    }

}
