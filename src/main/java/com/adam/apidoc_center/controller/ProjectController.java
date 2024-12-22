package com.adam.apidoc_center.controller;

import com.adam.apidoc_center.common.PagedData;
import com.adam.apidoc_center.dto.ProjectDTO;
import com.adam.apidoc_center.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/projects")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @GetMapping("/viewAll")
    public String viewAll(@RequestParam(required = false) Integer pageNum, @RequestParam(required = false) Integer pageSize, Model model) {
        if(pageNum == null || pageSize == null) {
            pageNum = 0;
            pageSize = 20;
        }
        PagedData<ProjectDTO> pagedData = projectService.getProjectsPaged(pageNum, pageSize);
        model.addAttribute("pagedData", pagedData);
        return "project/viewAll";
    }

    @GetMapping("/view/{projectId}")
    public String viewProject(@PathVariable long projectId) {
        return "project/viewProject";
    }

}