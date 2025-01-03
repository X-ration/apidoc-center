package com.adam.apidoc_center.controller;

import com.adam.apidoc_center.common.Response;
import com.adam.apidoc_center.common.StringConstants;
import com.adam.apidoc_center.dto.GroupInterfaceDTO;
import com.adam.apidoc_center.dto.ProjectGroupDTO;
import com.adam.apidoc_center.dto.ProjectGroupDetailDisplayDTO;
import com.adam.apidoc_center.service.GroupInterfaceService;
import com.adam.apidoc_center.service.ProjectGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/group")
public class ProjectGroupController {

    @Autowired
    private ProjectGroupService projectGroupService;
    @Autowired
    private GroupInterfaceService groupInterfaceService;

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

    @PostMapping("/{groupId}/delete")
    @ResponseBody
    public Response<Void> deleteGroup(@PathVariable long groupId) {
        return projectGroupService.deleteGroup(groupId);
    }

    @PostMapping("/{groupId}/modify")
    @ResponseBody
    public Response<?> modifyGroup(@PathVariable long groupId, @RequestBody ProjectGroupDTO projectGroupDTO) {
        if(projectGroupDTO == null) {
            return Response.fail(StringConstants.REQUEST_PARAM_IS_NULL);
        }
        return projectGroupService.checkAndModify(groupId, projectGroupDTO);
    }

    @PostMapping("/{groupId}/interface/create")
    @ResponseBody
    public Response<?> createInterface(@PathVariable long groupId, @RequestBody GroupInterfaceDTO groupInterfaceDTO) {
        if(groupInterfaceDTO == null) {
            return Response.fail(StringConstants.REQUEST_PARAM_IS_NULL);
        }
        return groupInterfaceService.checkAndCreate(groupId, groupInterfaceDTO);
    }

}