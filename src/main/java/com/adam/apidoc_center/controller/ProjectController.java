package com.adam.apidoc_center.controller;

import com.adam.apidoc_center.common.PagedData;
import com.adam.apidoc_center.common.Response;
import com.adam.apidoc_center.common.StringConstants;
import com.adam.apidoc_center.dto.ProjectDTO;
import com.adam.apidoc_center.dto.ProjectDetailDisplayDTO;
import com.adam.apidoc_center.dto.ProjectGroupDTO;
import com.adam.apidoc_center.dto.ProjectListDisplayDTO;
import com.adam.apidoc_center.service.ProjectGroupService;
import com.adam.apidoc_center.service.ProjectService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


@Controller
@RequestMapping("/project")
@Slf4j
public class ProjectController {

    @Autowired
    private ProjectService projectService;
    @Autowired
    private ProjectGroupService projectGroupService;

    @GetMapping("/viewAll")
    public String viewAll(@RequestParam(required = false) Integer pageNum, @RequestParam(required = false) Integer pageSize, Model model) {
        if(pageNum == null || pageSize == null) {
            pageNum = 0;
            pageSize = 5;
        } else if(pageNum < 0) {
            pageNum = 0;
        }
        PagedData<ProjectListDisplayDTO> pagedData = projectService.getProjectsPaged(pageNum, pageSize);
        model.addAttribute("pagedData", pagedData);
        return "project/viewAll";
    }

    @GetMapping("/viewFollow")
    public String viewFollow(@RequestParam(required = false) Integer pageNum, @RequestParam(required = false) Integer pageSize, Model model) {
        if(pageNum == null || pageSize == null) {
            pageNum = 0;
            pageSize = 5;
        } else if(pageNum < 0) {
            pageNum = 0;
        }
        PagedData<ProjectListDisplayDTO> pagedData = projectService.getFollowedProjectsPaged(pageNum, pageSize);
        model.addAttribute("pagedData", pagedData);
        return "project/viewFollow";
    }

    @GetMapping("/{projectId}/view")
    public String viewProject(@PathVariable long projectId, Model model) {
        ProjectDetailDisplayDTO projectDetailDisplayDTO = projectService.getProjectDetail(projectId);
        if(projectDetailDisplayDTO != null) {
            model.addAttribute("project", projectDetailDisplayDTO);
            return "project/viewProject";
        } else {
            return "redirect:/error/404";
        }
    }

    @PostMapping(value = "/create")
    @ResponseBody
    public Response<?> createProject(@RequestBody ProjectDTO projectDTO) {
        if(projectDTO == null) {
            return Response.fail(StringConstants.REQUEST_PARAM_IS_NULL);
        }
        log.debug("createProject dto={}", projectDTO);
        return projectService.checkAndCreate(projectDTO);
    }

    @PostMapping("/{projectId}/delete")
    @ResponseBody
    public Response<Void> deleteProject(@PathVariable long projectId) {
        return projectService.deleteProject(projectId);
    }

    @PostMapping(value = "/{projectId}/modify")
    @ResponseBody
    public Response<?> modifyProject(@RequestBody ProjectDTO projectDTO, @PathVariable long projectId) {
        if(projectDTO == null) {
            return Response.fail(StringConstants.REQUEST_PARAM_IS_NULL);
        }
        log.debug("modifyProject dto={}", projectDTO);
        return projectService.checkAndModify(projectDTO, projectId);
    }

    @PostMapping("/{projectId}/follow")
    @ResponseBody
    public Response<Void> followProject(@PathVariable long projectId) {
        return projectService.followProject(projectId);
    }

    @PostMapping("/{projectId}/unfollow")
    @ResponseBody
    public Response<Void> unfollowProject(@PathVariable long projectId) {
        return projectService.unfollowProject(projectId);
    }

    @PostMapping("/{projectId}/group/create")
    @ResponseBody
    public Response<?> createGroup(@PathVariable long projectId, @RequestBody ProjectGroupDTO projectGroupDTO) {
        if(projectGroupDTO == null) {
            return Response.fail(StringConstants.REQUEST_PARAM_IS_NULL);
        }
        return projectGroupService.checkAndCreate(projectId, projectGroupDTO);
    }

}