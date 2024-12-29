package com.adam.apidoc_center.controller;

import com.adam.apidoc_center.dto.ProjectGroupDetailDisplayDTO;
import com.adam.apidoc_center.service.ProjectGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/group")
public class ProjectGroupController {

    @Autowired
    private ProjectGroupService projectGroupService;

    @GetMapping("/{groupId}/view")
    public String viewGroup(@PathVariable long groupId, Model model) {
        ProjectGroupDetailDisplayDTO projectGroupDetailDisplayDTO = projectGroupService.getGroupDetail(groupId);
        if(projectGroupDetailDisplayDTO != null) {
            model.addAttribute("group", projectGroupDetailDisplayDTO);
            return "project/viewGroup";
        } else {
            return "redirect:/error/404";
        }
    }

}