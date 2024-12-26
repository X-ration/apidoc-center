package com.adam.apidoc_center.controller;

import com.adam.apidoc_center.common.PagedData;
import com.adam.apidoc_center.common.Response;
import com.adam.apidoc_center.common.StringConstants;
import com.adam.apidoc_center.dto.ProjectCreateOrUpdateDTO;
import com.adam.apidoc_center.dto.ProjectDisplayDTO;
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

    @GetMapping("/viewAll")
    public String viewAll(@RequestParam(required = false) Integer pageNum, @RequestParam(required = false) Integer pageSize, Model model) {
        if(pageNum == null || pageSize == null) {
            pageNum = 0;
            pageSize = 10;
        } else if(pageNum < 0) {
            pageNum = 0;
        }
        PagedData<ProjectDisplayDTO> pagedData = projectService.getProjectsPaged(pageNum, pageSize);
        model.addAttribute("pagedData", pagedData);
        return "project/viewAll";
    }

    @GetMapping("/view/{projectId}")
    public String viewProject(@PathVariable long projectId) {
        return "project/viewProject";
    }

    @PostMapping(value = "/create")
    @ResponseBody
    public Response<?> createProject(@RequestBody ProjectCreateOrUpdateDTO projectCreateOrUpdateDTO) {
        if(projectCreateOrUpdateDTO == null) {
            return Response.fail(StringConstants.REQUEST_PARAM_IS_NULL);
        }
        log.debug("createProject dto={}", projectCreateOrUpdateDTO);
        return projectService.checkAndCreate(projectCreateOrUpdateDTO);
    }

}